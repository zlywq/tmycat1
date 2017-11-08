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

import java.util.Random;


/*
这里是试验一个最简的转账流程。
 */
@Component
@EnableConfigurationProperties({MyAppProperties.class})
public class SimpleDistributedTranService {



    final Logger logger             = LoggerFactory.getLogger(getClass());

    //如果不是static，在另一个类（如TstConfirmTranInSingleMachine）修改bean里的这里的属性，但是没能成功修改，log打印出来还是修改前的值。不知为何，难道被AOC弄成bean后不支持field了~~~...
    public static boolean UnitTestErr_doWholeProcessSimplyWhenFirst_step1TransferAtFromSide_1 = false;
    public static boolean UnitTestErr_doWholeProcessSimplyWhenFirst_step1TransferAtFromSide_2 = false;

    public static boolean UnitTestErr_doWholeProcessSimplyWhenFirst_step2TransferAtToSide_1 = false;
    public static boolean UnitTestErr_doWholeProcessSimplyWhenFirst_step2TransferAtToSide_2 = false;

    public static boolean UnitTestErr_doWholeProcessSimplyWhenFirst_1 = false;
    public static boolean UnitTestErr_doWholeProcessSimplyWhenFirst_2 = false;
    public static boolean UnitTestErr_doWholeProcessSimplyWhenFirst_3 = false;

    public static boolean UnitTestErr_checkAndDealWholeProcess_1 = false;
    public static boolean UnitTestErr_checkAndDealWholeProcess_2 = false;
    public static boolean UnitTestErr_checkAndDealWholeProcess_3 = false;





    @Autowired
    private MyAppProperties properties;



    @Autowired
    SimpleAccountMapper simpleAccountMapper;
    @Autowired
    SimpleAccountTransferStateMapper simpleAccountTransferStateMapper;

    public static final String AccountType = "YuE";
    public static final String SimpleAccountTransferState_didOut = "didOut";
    public static final String SimpleAccountTransferState_didIn = "didIn";
    public static final String SimpleAccountTransferState_finish = "finish";

    public static final int InitAmount = 10000;

    public static void clearAll_UnitTestErr_flags(){
        UnitTestErr_doWholeProcessSimplyWhenFirst_step1TransferAtFromSide_1 = false;
        UnitTestErr_doWholeProcessSimplyWhenFirst_step1TransferAtFromSide_2 = false;
        UnitTestErr_doWholeProcessSimplyWhenFirst_1 = false;
        UnitTestErr_doWholeProcessSimplyWhenFirst_2 = false;
        UnitTestErr_doWholeProcessSimplyWhenFirst_3 = false;

        UnitTestErr_doWholeProcessSimplyWhenFirst_step2TransferAtToSide_1 = false;
        UnitTestErr_doWholeProcessSimplyWhenFirst_step2TransferAtToSide_2 = false;
        //UnitTestErr_doWholeProcessSimplyWhenFirst_step2TransferAtToSide_3 = false;
    }

    public void createUserAccount(long userId, long amount){
        SimpleAccount bean = new SimpleAccount();
        bean.setUserId(userId);
        bean.setAccountType(AccountType);
        bean.setAmount(amount);
        bean.setRowver(1);
        simpleAccountMapper.insertAccount(bean);
    }
    public void initUserAccount(long fromUserId,long toUserId,long amount){
        createUserAccount(fromUserId,amount);
        createUserAccount(toUserId,amount);
    }

    public static class StructIdsTransfer{
        public long fromUserId;
        public long toUserId;
        public long atsId;
        public static StructIdsTransfer generate(){
            long tm = System.currentTimeMillis();
            long fromUserId = tm*1000;
            long toUserId = fromUserId+1;
            long atsId = tm * 1000 + 10;
            StructIdsTransfer o = new StructIdsTransfer();
            o.fromUserId = fromUserId;
            o.toUserId = toUserId;
            o.atsId = atsId;
            return o;
        }
    }

    public long doWholeProcessSimplyWhenFirst_step1TransferAtFromSide_err(long fromUserId, long transferAmount, long toUserId, long precrtAtsId){
        return doWholeProcessSimplyWhenFirst_step1TransferAtFromSide(fromUserId,transferAmount,toUserId,precrtAtsId);
    }

    @Transactional(rollbackFor=Exception.class)
    public long doWholeProcessSimplyWhenFirst_step1TransferAtFromSide(long fromUserId, long transferAmount, long toUserId, long precrtAtsId){
        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" enter");

        SimpleAccount accountPojo = simpleAccountMapper.getAccountByPkForUpdate(fromUserId,AccountType);
        if (accountPojo.getAmount() < transferAmount){
            throw new RuntimeException("account amount not enough");
        }else{
            SimpleAccountTransferState satsObj = new SimpleAccountTransferState();
            long atsId = precrtAtsId;
            if (precrtAtsId == 0){
                //atsId = System.nanoTime();
                throw new RuntimeException("precrtAtsId == 0");
            }
            satsObj.setAtsId(atsId);
            satsObj.setIdForPart(fromUserId);
            satsObj.setState(SimpleAccountTransferState_didOut);
            satsObj.setFromUserId(fromUserId);
            satsObj.setFromAccountType(AccountType);
            satsObj.setToUserId(toUserId);
            satsObj.setToAccountType(AccountType);
            satsObj.setAmount(transferAmount);
            satsObj.setRowver(1);
            //TODO 发送消息到队列，包含satsObj的内容
            int affectRowCnt = simpleAccountTransferStateMapper.insert(satsObj);
            if (affectRowCnt != 1){
                throw new RuntimeException("insert SimpleAccountTransferState failed");
            }

            logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" UnitTestErr_doWholeProcessSimplyWhenFirst_step1TransferAtFromSide_1="+UnitTestErr_doWholeProcessSimplyWhenFirst_step1TransferAtFromSide_1);
            if (UnitTestErr_doWholeProcessSimplyWhenFirst_step1TransferAtFromSide_1){
                throw new MyBaseException("UnitTestErr_doWholeProcessSimplyWhenFirst_step1TransferAtFromSide_1",0,true);
            }

            affectRowCnt = simpleAccountMapper.updateAccountByDelta(accountPojo.getRowver()+1,accountPojo.getRowver(),
                    accountPojo.getUserId(),AccountType,-1*transferAmount);
            if (affectRowCnt == 0){
                throw new RuntimeException("other tran had modified this SimpleAccount row");
            }

            logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" UnitTestErr_doWholeProcessSimplyWhenFirst_step1TransferAtFromSide_2="+UnitTestErr_doWholeProcessSimplyWhenFirst_step1TransferAtFromSide_2);
            if (UnitTestErr_doWholeProcessSimplyWhenFirst_step1TransferAtFromSide_2){
                throw new MyBaseException("UnitTestErr_doWholeProcessSimplyWhenFirst_step1TransferAtFromSide_2",0,true);
            }
            logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" exit");
            return atsId;
        }
    }
    @Transactional(rollbackFor=Exception.class)
    public void doWholeProcessSimplyWhenFirst_step2TransferAtToSide(long toUserId,long atsId, long transferAmount, long fromUserId){
        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" enter");

        SimpleAccount accountPojo = simpleAccountMapper.getAccountByPkForUpdate(toUserId,AccountType);
        int affectRowCnt = simpleAccountMapper.updateAccountByDelta(accountPojo.getRowver()+1,accountPojo.getRowver(),
                accountPojo.getUserId(),AccountType,transferAmount);
        if (affectRowCnt == 0){
            throw new RuntimeException("other tran had modified this SimpleAccount row");
        }

        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" UnitTestErr_doWholeProcessSimplyWhenFirst_step2TransferAtToSide_1="+UnitTestErr_doWholeProcessSimplyWhenFirst_step2TransferAtToSide_1);
        if (UnitTestErr_doWholeProcessSimplyWhenFirst_step2TransferAtToSide_1){
            throw new MyBaseException("UnitTestErr_doWholeProcessSimplyWhenFirst_step2TransferAtToSide_1",0,true);
        }

        SimpleAccountTransferState satsObj = new SimpleAccountTransferState();
        satsObj.setAtsId(atsId);
        satsObj.setIdForPart(toUserId);
        satsObj.setState(SimpleAccountTransferState_didIn);
        satsObj.setFromUserId(fromUserId);
        satsObj.setFromAccountType(AccountType);
        satsObj.setToUserId(toUserId);
        satsObj.setToAccountType(AccountType);
        satsObj.setAmount(transferAmount);
        satsObj.setRowver(1);
        //TODO 发送消息到队列，包含satsObj的内容
        affectRowCnt = simpleAccountTransferStateMapper.insert(satsObj);
        if (affectRowCnt != 1){
            throw new RuntimeException("insert SimpleAccountTransferState failed");
        }
        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" UnitTestErr_doWholeProcessSimplyWhenFirst_step2TransferAtToSide_2="+UnitTestErr_doWholeProcessSimplyWhenFirst_step2TransferAtToSide_2);
        if (UnitTestErr_doWholeProcessSimplyWhenFirst_step2TransferAtToSide_2){
            throw new MyBaseException("UnitTestErr_doWholeProcessSimplyWhenFirst_step2TransferAtToSide_2",0,true);
        }
    }
    @Transactional(rollbackFor=Exception.class)
    public void doWholeProcessSimplyWhenFirst_step3updateState(long fromUserId,long atsId){
        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" enter");
        SimpleAccountTransferState satsObj = simpleAccountTransferStateMapper.getByPkAndPartKeyForUpdate(atsId,fromUserId);
        if ( SimpleAccountTransferState_didOut.equals(satsObj.getState()) ){
            int affectRowCnt = simpleAccountTransferStateMapper.updateState(satsObj.getRowver()+1,satsObj.getRowver(),
                    SimpleAccountTransferState_finish,atsId,fromUserId);
            if (affectRowCnt == 0){
                throw new RuntimeException("other tran had modified this SimpleAccountTransferState row");
            }
        }else{
            throw new RuntimeException("error for wrong value of state");
        }
    }

    public static class Struct_checkAndDealWholeProcess_step1fromSide{
        public SimpleAccountTransferState satsObj;
        public String retcmd;
    }
    @Transactional(rollbackFor=Exception.class)
    public Struct_checkAndDealWholeProcess_step1fromSide checkAndDealWholeProcess_step1fromSide(long fromUserId,long atsId){
        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" enter");
        SimpleAccountTransferState satsObj = simpleAccountTransferStateMapper.getByPkAndPartKeyForUpdate(atsId,fromUserId);
        Struct_checkAndDealWholeProcess_step1fromSide retObj = new Struct_checkAndDealWholeProcess_step1fromSide();
        retObj.satsObj = satsObj;
        if (satsObj == null){
            retObj.retcmd = "do nothing";
            return  retObj;
        }else{
            if (SimpleAccountTransferState_finish.equals(satsObj.getState())){
                retObj.retcmd = "do nothing";
                return  retObj;
            }else if (SimpleAccountTransferState_didOut.equals(satsObj.getState())){
                retObj.retcmd = "next";
                return  retObj;
            }else{
                throw new RuntimeException("error for wrong state in from side");
            }
        }
    }
    @Transactional(rollbackFor=Exception.class)
    public String checkAndDealWholeProcess_step2toSide(long toUserId,long atsId){
        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" enter");
        SimpleAccountTransferState satsObj = simpleAccountTransferStateMapper.getByPkAndPartKeyForUpdate(atsId,toUserId);
        if (satsObj == null){
            //~~~~~~~~~~~..........需要检查有防并发处理
            doWholeProcessSimplyWhenFirst_step2TransferAtToSide(toUserId,atsId,satsObj.getAmount(), satsObj.getFromUserId());
            return "next";
        }else{
            if (SimpleAccountTransferState_didIn.equals(satsObj.getState())){
                //~~~~~~~~~~~..........需要检查有防并发处理
                return "next";
            }else{
                throw new RuntimeException("error for wrong state in from side");
            }
        }
    }
    @Transactional(rollbackFor=Exception.class)
    public void checkAndDealWholeProcess_step3(long fromUserId,long atsId){
        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" enter");
        doWholeProcessSimplyWhenFirst_step3updateState(fromUserId,atsId);
    }



}
