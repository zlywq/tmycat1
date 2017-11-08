package g1.app;

import static g1.service.SimpleDistributedTranService.*;
import g1.cmn.MyBaseException;
import g1.service.SimpleDistributedTranService;
import g1.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class AppSimpleDistributedTran {


    final Logger logger             = LoggerFactory.getLogger(getClass());


    @Autowired
    SimpleDistributedTranService simpleDistributedTranService;


    public void doWholeProcessSimplyWhenFirst(SimpleDistributedTranService.StructIdsTransfer idsObj, boolean needInit, long transferAmount){
        if (idsObj == null){
            idsObj = SimpleDistributedTranService.StructIdsTransfer.generate();
        }
        long fromUserId = idsObj.fromUserId;
        long toUserId = idsObj.toUserId;
        if (needInit){
            simpleDistributedTranService.initUserAccount(fromUserId,toUserId,InitAmount);
        }
        if (transferAmount == 0){
            Random random = new Random();
            transferAmount =  random.nextInt(InitAmount/10)+1;
        }
        long precrtAtsId = idsObj.atsId;
        long atsId = simpleDistributedTranService.doWholeProcessSimplyWhenFirst_step1TransferAtFromSide(fromUserId,transferAmount, toUserId, precrtAtsId);
        if (UnitTestErr_doWholeProcessSimplyWhenFirst_1){
            throw new MyBaseException("UnitTestErr_doWholeProcessSimplyWhenFirst_1",0,true);
        }
        simpleDistributedTranService.doWholeProcessSimplyWhenFirst_step2TransferAtToSide(toUserId,atsId,transferAmount, fromUserId);
        if (UnitTestErr_doWholeProcessSimplyWhenFirst_2){
            throw new MyBaseException("UnitTestErr_doWholeProcessSimplyWhenFirst_2",0,true);
        }
        simpleDistributedTranService.doWholeProcessSimplyWhenFirst_step3updateState(fromUserId,atsId);
        if (UnitTestErr_doWholeProcessSimplyWhenFirst_3){
            throw new MyBaseException("UnitTestErr_doWholeProcessSimplyWhenFirst_3",0,true);
        }
    }


    public void checkAndDealWholeProcess(long fromUserId,long atsId){
        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" enter");
        Struct_checkAndDealWholeProcess_step1fromSide step1RetObj = simpleDistributedTranService.checkAndDealWholeProcess_step1fromSide(fromUserId,atsId);
        if (UnitTestErr_checkAndDealWholeProcess_1){
            throw new MyBaseException("UnitTestErr_checkAndDealWholeProcess_1",0,true);
        }
        if ("do nothing".equals(step1RetObj.retcmd)){
            //
        }else if ("next".equals(step1RetObj.retcmd)){
            String cmd_step2 = simpleDistributedTranService.checkAndDealWholeProcess_step2toSide(step1RetObj.satsObj.getToUserId(),atsId);
            if (UnitTestErr_checkAndDealWholeProcess_2){
                throw new MyBaseException("UnitTestErr_checkAndDealWholeProcess_2",0,true);
            }
            if ("next".equals(cmd_step2)){
                simpleDistributedTranService.checkAndDealWholeProcess_step3(fromUserId,atsId);
                if (UnitTestErr_checkAndDealWholeProcess_3){
                    throw new MyBaseException("UnitTestErr_checkAndDealWholeProcess_3",0,true);
                }
            }else{
                throw new RuntimeException("XXX2");
            }
        }else{
            throw new RuntimeException("XXX1");
        }
    }



}
