package g1;

import static g1.service.P5DistributedTranService.*;

import g1.app.AppP5DistributedTran;
import g1.ibatisMapper.*;
import g1.pojo.*;
import g1.service.P5DistributedTranService;
import g1.tool.Tool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import sprbtapp.JustApplication1;

import static g1.service.P5DistributedTranService.InitAmount_JiFen;
import static g1.service.P5DistributedTranService.InitAmount_YuE;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {JustApplication1.class})
public class TstP5DistributedTran {



    final Logger logger             = LoggerFactory.getLogger(getClass());

    @Autowired
    P5DistributedTranService p5DistributedTranService;
    @Autowired
    AppP5DistributedTran appP5DistributedTran;


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



    @Test
    public void tst_wholeProcess_1(){
        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" enter");

        StructIdsTransfer idsObj = StructIdsTransfer.generate();
        long fromUserId = idsObj.fromUserId;
        long toUserId = idsObj.toUserId;
        p5DistributedTranService.initUserAccount(fromUserId,toUserId,InitAmount_YuE,InitAmount_JiFen);
        long transferAmountYuE = InitAmount_YuE / 10;
        long transferAmountJiFen = InitAmount_JiFen / 10;


        P5Account fromUserAccountYuE1 = p5AccountYuEMapper.getAccountByPk(fromUserId);
        P5Account fromUserAccountJiFen1 = p5AccountJiFenMapper.getAccountByPk(fromUserId);
        P5Account toUserAccountYuE1 = p5AccountYuEMapper.getAccountByPk(toUserId);
        P5Account toUserAccountJiFen1 = p5AccountJiFenMapper.getAccountByPk(toUserId);
        appP5DistributedTran.doWholeProcessSimplyWhenFirst(idsObj,false,transferAmountYuE,transferAmountJiFen);//模仿由某个操作触发转账操作的第一次的正常处理
        if (true){
            P5Account fromUserAccountYuE2 = p5AccountYuEMapper.getAccountByPk(fromUserId);
            P5Account fromUserAccountJiFen2 = p5AccountJiFenMapper.getAccountByPk(fromUserId);
            P5Account toUserAccountYuE2 = p5AccountYuEMapper.getAccountByPk(toUserId);
            P5Account toUserAccountJiFen2 = p5AccountJiFenMapper.getAccountByPk(toUserId);
            P5AccountTransferRequest atrObj2 = p5AccountTransferRequestMapper.getByPk(idsObj.atrId);
            P5AccountChangeState fromAcsYuE2 = p5AccountYuEChangeStateMapper.getByPkAndPartKey(idsObj.atrId,fromUserId);
            P5AccountChangeState fromAcsJiFen2 = p5AccountJiFenChangeStateMapper.getByPkAndPartKey(idsObj.atrId,fromUserId);
            P5AccountChangeState toAcsYuE2 = p5AccountYuEChangeStateMapper.getByPkAndPartKey(idsObj.atrId,toUserId);
            P5AccountChangeState toAcsJiFen2 = p5AccountJiFenChangeStateMapper.getByPkAndPartKey(idsObj.atrId,toUserId);
            assert ( P5AccountTransferRequestState_finish.equals(atrObj2.getState()) );
            assert ( P5AccountState_didOut.equals(fromAcsYuE2.getState()));
            assert ( P5AccountState_didOut.equals(fromAcsJiFen2.getState()));
            assert ( P5AccountState_didIn.equals(toAcsYuE2.getState()));
            assert ( P5AccountState_didIn.equals(toAcsJiFen2.getState()));
            assert (fromUserAccountYuE2.getAmount()  == fromUserAccountYuE1.getAmount() - transferAmountYuE);
            assert (fromUserAccountJiFen2.getAmount()  == fromUserAccountJiFen1.getAmount() - transferAmountJiFen);
            assert (toUserAccountYuE2.getAmount()  == toUserAccountYuE1.getAmount() + transferAmountYuE);
            assert (toUserAccountJiFen2.getAmount()  == toUserAccountJiFen1.getAmount() + transferAmountJiFen);
        }

        appP5DistributedTran.checkAnddoWholeProcess(idsObj.atrId);//模仿收到延时消息后的正常处理，幂等
        if (true){
            P5Account fromUserAccountYuE2 = p5AccountYuEMapper.getAccountByPk(fromUserId);
            P5Account fromUserAccountJiFen2 = p5AccountJiFenMapper.getAccountByPk(fromUserId);
            P5Account toUserAccountYuE2 = p5AccountYuEMapper.getAccountByPk(toUserId);
            P5Account toUserAccountJiFen2 = p5AccountJiFenMapper.getAccountByPk(toUserId);
            P5AccountTransferRequest atrObj2 = p5AccountTransferRequestMapper.getByPk(idsObj.atrId);
            P5AccountChangeState fromAcsYuE2 = p5AccountYuEChangeStateMapper.getByPkAndPartKey(idsObj.atrId,fromUserId);
            P5AccountChangeState fromAcsJiFen2 = p5AccountJiFenChangeStateMapper.getByPkAndPartKey(idsObj.atrId,fromUserId);
            P5AccountChangeState toAcsYuE2 = p5AccountYuEChangeStateMapper.getByPkAndPartKey(idsObj.atrId,toUserId);
            P5AccountChangeState toAcsJiFen2 = p5AccountJiFenChangeStateMapper.getByPkAndPartKey(idsObj.atrId,toUserId);
            assert ( P5AccountTransferRequestState_finish.equals(atrObj2.getState()) );
            assert ( P5AccountState_didOut.equals(fromAcsYuE2.getState()));
            assert ( P5AccountState_didOut.equals(fromAcsJiFen2.getState()));
            assert ( P5AccountState_didIn.equals(toAcsYuE2.getState()));
            assert ( P5AccountState_didIn.equals(toAcsJiFen2.getState()));
            assert (fromUserAccountYuE2.getAmount()  == fromUserAccountYuE1.getAmount() - transferAmountYuE);
            assert (fromUserAccountJiFen2.getAmount()  == fromUserAccountJiFen1.getAmount() - transferAmountJiFen);
            assert (toUserAccountYuE2.getAmount()  == toUserAccountYuE1.getAmount() + transferAmountYuE);
            assert (toUserAccountJiFen2.getAmount()  == toUserAccountJiFen1.getAmount() + transferAmountJiFen);
        }
    }











}
