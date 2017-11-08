package g1.service;

import com.alibaba.fastjson.JSON;
import g1.cfg.MyAppProperties;
import g1.cmn.MyBaseException;
import g1.ibatisMapper.*;
import g1.pojo.*;
import g1.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 这里是试验一个业务数据分4块加上控制数据共5块的转账流程。
 */
@Component
@EnableConfigurationProperties({MyAppProperties.class})
public class P5DistributedTranService {

    final Logger logger             = LoggerFactory.getLogger(getClass());

    @Autowired
    private MyAppProperties properties;



    @Autowired
    P5AccountTransferRequestMapper p5AccountTransferRequestMapper;
    @Autowired
    P5AccountYuEMapper p5AccountYuEMapper;
    @Autowired
    P5AccountJiFenMapper p5AccountJiFenMapper;
    @Autowired
    P5AccountYuEChangeStateMapper p5AccountYuEChangeStateMapper;
    @Autowired
    P5AccountJiFenChangeStateMapper p5AccountJiFenChangeStateMapper;


    public static final String AccountType_YuE = "YuE";
    public static final String AccountType_JiFen = "JiFen";

    public static final String P5AccountState_didOut = "didOut";
    public static final String P5AccountState_didIn = "didIn";
    public static final String P5AccountState_didRollback = "didRollback";

    public static final String P5AccountTransferRequestState_Init = "Init";
    public static final String P5AccountTransferRequestState_toRollback = "toRollback";
    public static final String P5AccountTransferRequestState_finish = "finish";
    public static final String P5AccountTransferRequestState_fail = "fail";


    public static final int InitAmount_YuE = 10000;
    public static final int InitAmount_JiFen = 1000;



    public void createUserAccount(String acntType, long userId, long amount){
        P5Account bean = new P5Account();
        bean.setUserId(userId);
        bean.setAmount(amount);
        bean.setRowver(1);
        if (AccountType_YuE.equals(acntType)){
            p5AccountYuEMapper.insertAccount(bean);
        }else if (AccountType_JiFen.equals(acntType)){
            p5AccountJiFenMapper.insertAccount(bean);
        }else{
            throw new RuntimeException("invalid acntType");
        }
    }
    public void initUserAccount(long fromUserId,long toUserId,long amountYuE, long amountJiFen){
        createUserAccount(AccountType_YuE,fromUserId,amountYuE);
        createUserAccount(AccountType_JiFen,fromUserId,amountJiFen);
        createUserAccount(AccountType_YuE,toUserId,amountYuE);
        createUserAccount(AccountType_JiFen,toUserId,amountJiFen);
    }


    public static class StructIdsTransfer{
        public long fromUserId;
        public long toUserId;
        public long atrId;
        public static StructIdsTransfer generate(){
            long tm = System.currentTimeMillis();
            long fromUserId = tm*1000;
            long toUserId = fromUserId+1;
            long atrId = tm * 1000 + 10;
            StructIdsTransfer o = new StructIdsTransfer();
            o.fromUserId = fromUserId;
            o.toUserId = toUserId;
            o.atrId = atrId;
            return o;
        }
    }




    @Transactional(rollbackFor=Exception.class)
    public void doWholeProcessSimplyWhenFirst_step1createTransferRequest(long atrId,
        long fromUserId, long toUserId, long transferAmountYuE, long transferAmountJiFen) {
        logger.info("" + Tool.getCurrentClassName() + "." + Tool.getCurrentMethodName() + " enter");

        P5AccountTransferRequest atrObj = new P5AccountTransferRequest();
        atrObj.setAtrId(atrId);
        atrObj.setFromUserId(fromUserId);
        atrObj.setToUserId(toUserId);
        atrObj.setState(P5AccountTransferRequestState_Init);
        atrObj.setAmount(transferAmountYuE+transferAmountJiFen);
        atrObj.setRowver(1);
        List<P5AccountTransferItem> detailList = new ArrayList<>();
        P5AccountTransferItem item1= new P5AccountTransferItem();
        item1.setFromUserId(fromUserId);
        item1.setFromAccountType(AccountType_YuE);
        item1.setToUserId(toUserId);
        item1.setToAccountType(AccountType_YuE);
        item1.setAmount(transferAmountYuE);
        detailList.add(item1);
        P5AccountTransferItem item2= new P5AccountTransferItem();
        item2.setFromUserId(fromUserId);
        item2.setFromAccountType(AccountType_JiFen);
        item2.setToUserId(toUserId);
        item2.setToAccountType(AccountType_JiFen);
        item2.setAmount(transferAmountJiFen);
        detailList.add(item2);
        String detailJsonStr = JSON.toJSONString(detailList);
        atrObj.setDetailJson(detailJsonStr);
        p5AccountTransferRequestMapper.insert(atrObj);
    }
    //这里虽是本地事务，也可以当作是一个远程调用，在远端那边有其自身的本地事务，注意需要幂等
    @Transactional(rollbackFor=Exception.class)
    public void doWholeProcessSimplyWhenFirst_step2TransferJiFenAtFromSide(long atrId, long fromUserId, long transferAmount){
        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" enter");
        P5AccountChangeState acsObj1 = p5AccountJiFenChangeStateMapper.getByPkAndPartKeyForUpdate(atrId,fromUserId);
        if (acsObj1 == null){
            P5Account accountPojo = p5AccountJiFenMapper.getAccountByPkForUpdate(fromUserId);
            if (accountPojo.getAmount() < transferAmount){
                throw new RuntimeException("account JiFen amount not enough");
            }else{
                P5AccountChangeState acsObj = new P5AccountChangeState();
                acsObj.setAcsId(atrId);//注意是直接使用P5AccountTransferRequest的id
                acsObj.setIdForPart(fromUserId);
                acsObj.setAmount(transferAmount);
                acsObj.setState(P5AccountState_didOut);
                acsObj.setRowver(1);
                int affectRowCnt =  p5AccountJiFenChangeStateMapper.insert(acsObj);
                if (affectRowCnt != 1){
                    throw new RuntimeException("insert P5AccountChangeState JiFen failed");
                }
                affectRowCnt = p5AccountJiFenMapper.updateAccountByDelta(accountPojo.getRowver()+1,accountPojo.getRowver(),
                        accountPojo.getUserId(),-1*transferAmount);
                if (affectRowCnt == 0){
                    throw new RuntimeException("other tran had modified this JiFen Account row");
                }
                logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" exit");
            }
        }else if (P5AccountState_didOut.equals(acsObj1.getState())){
            //do nothing
            return;
        }else{
            throw new RuntimeException("should not go this path, need dev debug");
        }
    }
    //这里虽是本地事务，也可以当作是一个远程调用，在远端那边有其自身的本地事务，注意需要幂等
    @Transactional(rollbackFor=Exception.class)
    public void doWholeProcessSimplyWhenFirst_step3TransferYuEAtFromSide(long atrId, long fromUserId, long transferAmount){
        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" enter");
        P5AccountChangeState acsObj1 = p5AccountYuEChangeStateMapper.getByPkAndPartKeyForUpdate(atrId,fromUserId);
        if (acsObj1 == null){
            P5Account accountPojo = p5AccountYuEMapper.getAccountByPkForUpdate(fromUserId);
            if (accountPojo.getAmount() < transferAmount){
                throw new RuntimeException("account YuE amount not enough");
            }else{
                P5AccountChangeState acsObj = new P5AccountChangeState();
                acsObj.setAcsId(atrId);//注意是直接使用P5AccountTransferRequest的id
                acsObj.setIdForPart(fromUserId);
                acsObj.setAmount(transferAmount);
                acsObj.setState(P5AccountState_didOut);
                acsObj.setRowver(1);
                int affectRowCnt =  p5AccountYuEChangeStateMapper.insert(acsObj);
                if (affectRowCnt != 1){
                    throw new RuntimeException("insert P5AccountChangeState YuE failed");
                }
                affectRowCnt = p5AccountYuEMapper.updateAccountByDelta(accountPojo.getRowver()+1,accountPojo.getRowver(),
                        accountPojo.getUserId(),-1*transferAmount);
                if (affectRowCnt == 0){
                    throw new RuntimeException("other tran had modified this YuE Account row");
                }
                logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" exit");
            }
        }else if (P5AccountState_didOut.equals(acsObj1.getState())){
            //do nothing
            return;
        }else{
            throw new RuntimeException("should not go this path, need dev debug");
        }
    }
    //这里虽是本地事务，也可以当作是一个远程调用，在远端那边有其自身的本地事务，注意需要幂等
    @Transactional(rollbackFor=Exception.class)
    public void doWholeProcessSimplyWhenFirst_step4TransferJiFenAtToSide(long atrId, long toUserId, long transferAmount){
        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" enter");
        P5AccountChangeState acsObj1 = p5AccountJiFenChangeStateMapper.getByPkAndPartKeyForUpdate(atrId,toUserId);
        if (acsObj1 == null){
            P5Account accountPojo = p5AccountJiFenMapper.getAccountByPkForUpdate(toUserId);
            P5AccountChangeState acsObj = new P5AccountChangeState();
            acsObj.setAcsId(atrId);//注意是直接使用P5AccountTransferRequest的id
            acsObj.setIdForPart(toUserId);
            acsObj.setAmount(transferAmount);
            acsObj.setState(P5AccountState_didIn);
            acsObj.setRowver(1);
            int affectRowCnt =  p5AccountJiFenChangeStateMapper.insert(acsObj);
            if (affectRowCnt != 1){
                throw new RuntimeException("insert P5AccountChangeState JiFen failed");
            }
            affectRowCnt = p5AccountJiFenMapper.updateAccountByDelta(accountPojo.getRowver()+1,accountPojo.getRowver(),
                    accountPojo.getUserId(), transferAmount);
            if (affectRowCnt == 0){
                throw new RuntimeException("other tran had modified this JiFen Account row");
            }
            logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" exit");
        }else if (P5AccountState_didIn.equals(acsObj1.getState())){
            //do nothing
            return;
        }else{
            throw new RuntimeException("should not go this path, need dev debug");
        }
    }
    //这里虽是本地事务，也可以当作是一个远程调用，在远端那边有其自身的本地事务，注意需要幂等
    @Transactional(rollbackFor=Exception.class)
    public void doWholeProcessSimplyWhenFirst_step5TransferYuEAtToSide(long atrId, long toUserId, long transferAmount){
        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" enter");
        P5AccountChangeState acsObj1 = p5AccountYuEChangeStateMapper.getByPkAndPartKeyForUpdate(atrId,toUserId);
        if (acsObj1 == null){
            P5Account accountPojo = p5AccountYuEMapper.getAccountByPkForUpdate(toUserId);

            P5AccountChangeState acsObj = new P5AccountChangeState();
            acsObj.setAcsId(atrId);//注意是直接使用P5AccountTransferRequest的id
            acsObj.setIdForPart(toUserId);
            acsObj.setAmount(transferAmount);
            acsObj.setState(P5AccountState_didIn);
            acsObj.setRowver(1);
            int affectRowCnt =  p5AccountYuEChangeStateMapper.insert(acsObj);
            if (affectRowCnt != 1){
                throw new RuntimeException("insert P5AccountChangeState YuE failed");
            }
            affectRowCnt = p5AccountYuEMapper.updateAccountByDelta(accountPojo.getRowver()+1,accountPojo.getRowver(),
                    accountPojo.getUserId(), transferAmount);
            if (affectRowCnt == 0){
                throw new RuntimeException("other tran had modified this YuE Account row");
            }
            logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" exit");
        }else if (P5AccountState_didIn.equals(acsObj1.getState())){
            //do nothing
            return;
        }else{
            throw new RuntimeException("should not go this path, need dev debug");
        }
    }
    @Transactional(rollbackFor=Exception.class)
    public void doWholeProcessSimplyWhenFirst_step6updateRequestState(long atrId){
        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" enter");
        P5AccountTransferRequest atrObj = p5AccountTransferRequestMapper.getByPkForUpdate(atrId);
        if ( P5AccountTransferRequestState_Init.equals(atrObj.getState()) ){
            int affectRowCnt = p5AccountTransferRequestMapper.updateState(atrObj.getRowver()+1,atrObj.getRowver(),
                    P5AccountTransferRequestState_finish,atrId);
            if (affectRowCnt == 0){
                throw new RuntimeException("other tran had modified this P5AccountTransferRequest row");
            }
        }else if ( P5AccountTransferRequestState_finish.equals(atrObj.getState()) ){
            //do nothing
        }else{
            throw new RuntimeException("should not go this path, need dev debug");
        }
    }

    @Transactional(rollbackFor=Exception.class)
    public boolean rollbackWholeProcess_step1updateStateToRollback(long atrId){
        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" enter");
        boolean goOnRollback = true;
        P5AccountTransferRequest atrObj = p5AccountTransferRequestMapper.getByPkForUpdate(atrId);
        if (atrObj == null){
            goOnRollback = false;
        }else{
            if ( P5AccountTransferRequestState_Init.equals(atrObj.getState()) ){
                int affectRowCnt = p5AccountTransferRequestMapper.updateState(atrObj.getRowver()+1,atrObj.getRowver(),
                        P5AccountTransferRequestState_toRollback,atrId);
                if (affectRowCnt == 0){
                    throw new RuntimeException("other tran had modified this P5AccountTransferRequest row");
                }
            }else if ( P5AccountTransferRequestState_toRollback.equals(atrObj.getState()) ){
                //do nothing and go on
            }else if ( P5AccountTransferRequestState_finish.equals(atrObj.getState()) ){
                //do nothing and finish
                goOnRollback = false;
            }else if ( P5AccountTransferRequestState_fail.equals(atrObj.getState()) ){
                //do nothing and finish
                goOnRollback = false;
            }else{
                throw new RuntimeException("error for wrong value of state");
            }
        }
        return goOnRollback;
    }
    @Transactional(rollbackFor=Exception.class)
    public String rollbackWholeProcess_step2RollbackJiFenAtFromSide(long atrId, long fromUserId){
        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" enter");
        P5AccountChangeState acsObj = p5AccountJiFenChangeStateMapper.getByPkAndPartKeyForUpdate(atrId,fromUserId);
        if (acsObj == null){
            //说明正常流程的第2步都未成功，后面的步骤基本都不用做了，除了最后的修改状态。但是，如果不是严格按照顺序执行那些步骤，而是可能并发执行，则需要全部检查回滚。
            //也可以考虑加一条state=didRollback的 P5AccountChangeState 记录，以防万一
            return "next";//以防万一，都检查回滚 // return "last";
        }else if (P5AccountState_didRollback.equals(acsObj.getState())) {
            //说明这一步的rollback已经做过了，下一步
            return "next";
        }else if (P5AccountState_didOut.equals(acsObj.getState())){
            //需要在这里rollback
            P5Account accountPojo = p5AccountYuEMapper.getAccountByPkForUpdate(fromUserId);
            int affectRowCnt = p5AccountJiFenChangeStateMapper.updateState(acsObj.getRowver()+1,acsObj.getRowver(),
                    P5AccountState_didRollback,acsObj.getAcsId(),acsObj.getIdForPart());
            if (affectRowCnt == 0){
                throw new RuntimeException("other tran had modified this P5AccountChangeState row");
            }
            affectRowCnt = p5AccountJiFenMapper.updateAccountByDelta(accountPojo.getRowver()+1,accountPojo.getRowver(),
                    accountPojo.getUserId(),acsObj.getAmount());
            if (affectRowCnt == 0){
                throw new RuntimeException("other tran had modified this P5Account row");
            }
            return "next";
        }else{
            throw new RuntimeException("invalid state for P5AccountChangeState");
        }
    }
    @Transactional(rollbackFor=Exception.class)
    public String rollbackWholeProcess_step3RollbackYuEAtFromSide(long atrId, long fromUserId){
        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" enter");
        P5AccountChangeState acsObj = p5AccountYuEChangeStateMapper.getByPkAndPartKeyForUpdate(atrId,fromUserId);
        if (acsObj == null){
            //说明正常流程的第3步都未成功，后面的步骤基本都不用做了，除了最后的修改状态。但是，如果不是严格按照顺序执行那些步骤，而是可能并发执行，则需要全部检查回滚。
            //也可以考虑加一条state=didRollback的 P5AccountChangeState 记录，以防万一
            return "next";//以防万一，都检查回滚 //return "last";
        }else if (P5AccountState_didRollback.equals(acsObj.getState())) {
            //说明这一步的rollback已经做过了，下一步
            return "next";
        }else if (P5AccountState_didOut.equals(acsObj.getState())){
            //需要在这里rollback
            P5Account accountPojo = p5AccountYuEMapper.getAccountByPkForUpdate(fromUserId);
            int affectRowCnt = p5AccountYuEChangeStateMapper.updateState(acsObj.getRowver()+1,acsObj.getRowver(),
                    P5AccountState_didRollback,acsObj.getAcsId(),acsObj.getIdForPart());
            if (affectRowCnt == 0){
                throw new RuntimeException("other tran had modified this P5AccountChangeState row");
            }
            affectRowCnt = p5AccountYuEMapper.updateAccountByDelta(accountPojo.getRowver()+1,accountPojo.getRowver(),
                    accountPojo.getUserId(),acsObj.getAmount());
            if (affectRowCnt == 0){
                throw new RuntimeException("other tran had modified this P5Account row");
            }
            return "next";
        }else{
            throw new RuntimeException("invalid state for P5AccountChangeState");
        }
    }
    @Transactional(rollbackFor=Exception.class)
    public String rollbackWholeProcess_step4RollbackJiFenAtToSide(long atrId, long toUserId){
        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" enter");
        P5AccountChangeState acsObj = p5AccountJiFenChangeStateMapper.getByPkAndPartKeyForUpdate(atrId,toUserId);
        if (acsObj == null){
            //说明正常流程的第4步都未成功，后面的步骤基本都不用做了，除了最后的修改状态。但是，如果不是严格按照顺序执行那些步骤，而是可能并发执行，则需要全部检查回滚。
            //也可以考虑加一条state=didRollback的 P5AccountChangeState 记录，以防万一
            return "next";//以防万一，都检查回滚 // return "last";
        }else if (P5AccountState_didRollback.equals(acsObj.getState())) {
            //说明这一步的rollback已经做过了，下一步
            return "next";
        }else if (P5AccountState_didIn.equals(acsObj.getState())){
            //需要在这里rollback
            P5Account accountPojo = p5AccountJiFenMapper.getAccountByPkForUpdate(toUserId);
            int affectRowCnt = p5AccountJiFenChangeStateMapper.updateState(acsObj.getRowver()+1,acsObj.getRowver(),
                    P5AccountState_didRollback,acsObj.getAcsId(),acsObj.getIdForPart());
            if (affectRowCnt == 0){
                throw new RuntimeException("other tran had modified this P5AccountChangeState row");
            }
            affectRowCnt = p5AccountJiFenMapper.updateAccountByDelta(accountPojo.getRowver()+1,accountPojo.getRowver(),
                    accountPojo.getUserId(),-1*acsObj.getAmount());
            if (affectRowCnt == 0){
                throw new RuntimeException("other tran had modified this P5Account row");
            }
            return "next";
        }else{
            throw new RuntimeException("invalid state for P5AccountChangeState");
        }
    }
    @Transactional(rollbackFor=Exception.class)
    public String rollbackWholeProcess_step5RollbackYuEAtToSide(long atrId, long toUserId){
        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" enter");
        P5AccountChangeState acsObj = p5AccountYuEChangeStateMapper.getByPkAndPartKeyForUpdate(atrId,toUserId);
        if (acsObj == null){
            //说明正常流程的第5步都未成功，后面的步骤基本都不用做了，除了最后的修改状态。但是，如果不是严格按照顺序执行那些步骤，而是可能并发执行，则需要全部检查回滚。
            //也可以考虑加一条state=didRollback的 P5AccountChangeState 记录，以防万一
            return "next";//以防万一，都检查回滚 // return "last";
        }else if (P5AccountState_didRollback.equals(acsObj.getState())) {
            //说明这一步的rollback已经做过了，下一步
            return "next";
        }else if (P5AccountState_didIn.equals(acsObj.getState())){
            //需要在这里rollback
            P5Account accountPojo = p5AccountYuEMapper.getAccountByPkForUpdate(toUserId);
            int affectRowCnt = p5AccountYuEChangeStateMapper.updateState(acsObj.getRowver()+1,acsObj.getRowver(),
                    P5AccountState_didRollback,acsObj.getAcsId(),acsObj.getIdForPart());
            if (affectRowCnt == 0){
                throw new RuntimeException("other tran had modified this P5AccountChangeState row");
            }
            affectRowCnt = p5AccountYuEMapper.updateAccountByDelta(accountPojo.getRowver()+1,accountPojo.getRowver(),
                    accountPojo.getUserId(),-1*acsObj.getAmount());
            if (affectRowCnt == 0){
                throw new RuntimeException("other tran had modified this P5Account row");
            }
            return "next";
        }else{
            throw new RuntimeException("invalid state for P5AccountChangeState");
        }
    }
    @Transactional(rollbackFor=Exception.class)
    public void rollbackWholeProcess_step6updateRequestState(long atrId){
        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" enter");
        P5AccountTransferRequest atrObj = p5AccountTransferRequestMapper.getByPkForUpdate(atrId);
        if ( P5AccountTransferRequestState_toRollback.equals(atrObj.getState()) ){
            int affectRowCnt = p5AccountTransferRequestMapper.updateState(atrObj.getRowver()+1,atrObj.getRowver(),
                    P5AccountTransferRequestState_fail,atrId);
            if (affectRowCnt == 0){
                throw new RuntimeException("other tran had modified this P5AccountTransferRequest row");
            }
        }else if ( P5AccountTransferRequestState_fail.equals(atrObj.getState()) ){
            //do nothing
        }else{
            throw new RuntimeException("error for wrong value of state");
        }
    }





}











