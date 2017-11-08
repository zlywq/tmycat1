package g1.app;

import static g1.service.P5DistributedTranService.*;
import com.alibaba.fastjson.JSON;
import g1.ibatisMapper.*;
import g1.pojo.P5AccountTransferItem;
import g1.pojo.P5AccountTransferRequest;
import g1.service.P5DistributedTranService;
import g1.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppP5DistributedTran {

    final Logger logger             = LoggerFactory.getLogger(getClass());

    @Autowired
    P5DistributedTranService p5DistributedTranService;


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




    public void doWholeProcessSimplyWhenFirst(P5DistributedTranService.StructIdsTransfer idsObj, boolean needInit, long transferAmountYuE, long transferAmountJiFen){
        logger.info("" + Tool.getCurrentClassName() + "." + Tool.getCurrentMethodName() + " enter");
        if (idsObj == null){
            idsObj = P5DistributedTranService.StructIdsTransfer.generate();
        }
        long fromUserId = idsObj.fromUserId;
        long toUserId = idsObj.toUserId;
        if (needInit){
            p5DistributedTranService.initUserAccount(fromUserId,toUserId,InitAmount_YuE,InitAmount_JiFen);
        }
        if (transferAmountYuE == 0){
            transferAmountYuE = InitAmount_YuE / 10;
        }
        if (transferAmountJiFen == 0){
            transferAmountJiFen = InitAmount_JiFen / 10;
        }

        //TODO 先发送消息到队列，包含atrObj的内容
        try {
            p5DistributedTranService.doWholeProcessSimplyWhenFirst_step1createTransferRequest(idsObj.atrId, fromUserId, toUserId, transferAmountYuE, transferAmountJiFen);
            p5DistributedTranService.doWholeProcessSimplyWhenFirst_step2TransferJiFenAtFromSide(idsObj.atrId,fromUserId,transferAmountJiFen);
            p5DistributedTranService.doWholeProcessSimplyWhenFirst_step3TransferYuEAtFromSide(idsObj.atrId,fromUserId,transferAmountYuE);
        }catch (Exception e){
            //throw new RuntimeException(e);
            logger.error("err in doWholeProcessSimplyWhenFirst, prepare steps. ",e);
            //需要回滚
            rollbackWholeProcess(idsObj.atrId);
            return;
        }

        try {
            p5DistributedTranService.doWholeProcessSimplyWhenFirst_step4TransferJiFenAtToSide(idsObj.atrId,toUserId,transferAmountJiFen);
            p5DistributedTranService.doWholeProcessSimplyWhenFirst_step5TransferYuEAtToSide(idsObj.atrId,toUserId,transferAmountYuE);
            p5DistributedTranService.doWholeProcessSimplyWhenFirst_step6updateRequestState(idsObj.atrId);
        }catch (Exception e){
            //不需回滚，只需重试直到成功
            //可以在这里重试几次try中的内容，实在不行，（比如由于网络断开较长时间），再发延时消息重试，调用checkAnddoWholeProcess()
        }
    }
    public void checkAnddoWholeProcess(long atrId){
        logger.info("" + Tool.getCurrentClassName() + "." + Tool.getCurrentMethodName() + " enter");
        try {
            //加一把分布式锁，此锁最好能够重入
            P5AccountTransferRequest atrObj = p5AccountTransferRequestMapper.getByPk(atrId);
            if (atrObj == null){
                //do nothing
                return;
            }
            if ( P5AccountTransferRequestState_Init.equals(atrObj.getState()) ){
                // "next";
            }else if ( P5AccountTransferRequestState_toRollback.equals(atrObj.getState()) ){
                //需要回滚
                rollbackWholeProcess(atrObj.getAtrId());
                return;
            }else if ( P5AccountTransferRequestState_finish.equals(atrObj.getState()) ){
                return ;
            }else if ( P5AccountTransferRequestState_fail.equals(atrObj.getState()) ){
                return ;
            }else{
                throw new RuntimeException("error for wrong value of state");
            }
            List<P5AccountTransferItem> detailList2= JSON.parseArray(atrObj.getDetailJson(),P5AccountTransferItem.class);
            long transferAmountYuE = detailList2.get(0).getAmount();
            long transferAmountJiFen = detailList2.get(1).getAmount();
            try {
                p5DistributedTranService.doWholeProcessSimplyWhenFirst_step2TransferJiFenAtFromSide(atrObj.getAtrId(),atrObj.getFromUserId(),transferAmountJiFen);
                p5DistributedTranService.doWholeProcessSimplyWhenFirst_step3TransferYuEAtFromSide(atrObj.getAtrId(),atrObj.getFromUserId(),transferAmountYuE);
            }catch (Exception e){
                //需要回滚
                rollbackWholeProcess(atrObj.getAtrId());
                return;
            }

            try {
                p5DistributedTranService.doWholeProcessSimplyWhenFirst_step4TransferJiFenAtToSide(atrObj.getAtrId(),atrObj.getToUserId(),transferAmountJiFen);
                p5DistributedTranService.doWholeProcessSimplyWhenFirst_step5TransferYuEAtToSide(atrObj.getAtrId(),atrObj.getToUserId(),transferAmountYuE);
                p5DistributedTranService.doWholeProcessSimplyWhenFirst_step6updateRequestState(atrObj.getAtrId());
            }catch (Exception e){
                //不需回滚，只需重试直到成功
                //可以在这里重试几次try中的内容，实在不行，（比如由于网络断开较长时间），再发延时消息重试，调用本函数
            }
        }catch (Exception e){
            //过程中有异常，需要重试，可以根据具体异常立即重试或者延时重试......
        }finally {
            //释放分布式锁
        }
    }




    public void rollbackWholeProcess(long atrId){
        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" enter");
        try {
            //加一把分布式锁，此锁最好能够重入
            P5AccountTransferRequest atrObj = p5AccountTransferRequestMapper.getByPk(atrId);
            if (atrObj == null){
                return;
            }

            boolean goOn = p5DistributedTranService.rollbackWholeProcess_step1updateStateToRollback(atrId);
            if (!goOn){
                return;
            }
            String nextCmd = p5DistributedTranService.rollbackWholeProcess_step2RollbackJiFenAtFromSide(atrId,atrObj.getFromUserId());
            if (!"next".equals(nextCmd)){
                //目前暂无别的情况
                return;
            }
            nextCmd = p5DistributedTranService.rollbackWholeProcess_step3RollbackYuEAtFromSide(atrId,atrObj.getFromUserId());
            if (!"next".equals(nextCmd)){
                //目前暂无别的情况
                return;
            }
            nextCmd = p5DistributedTranService.rollbackWholeProcess_step4RollbackJiFenAtToSide(atrId,atrObj.getToUserId());
            if (!"next".equals(nextCmd)){
                //目前暂无别的情况
                return;
            }
            nextCmd = p5DistributedTranService.rollbackWholeProcess_step5RollbackYuEAtToSide(atrId,atrObj.getToUserId());
            if (!"next".equals(nextCmd)){
                //目前暂无别的情况
                return;
            }
            p5DistributedTranService.rollbackWholeProcess_step6updateRequestState(atrId);
        }catch (Exception e){
            //过程中有异常，需要重试，可以根据具体异常立即重试或者延时重试......
        }finally {
            //释放分布式锁
        }
    }




}
