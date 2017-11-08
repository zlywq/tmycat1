package g1;

import g1.app.AppSimpleDistributedTran;
import g1.cmn.MyBaseException;
import g1.ibatisMapper.*;
import g1.pojo.*;
import g1.service.SimpleDistributedTranService;
import g1.tool.Tool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import sprbtapp.JustApplication1;



/*


一些试错记录。
----
@RunWith(SpringRunner.class)
@SpringBootTest
@SpringBootApplication( exclude = {TestMycat1Application.class}) //运行错误 The following classes could not be excluded because they are not auto-configuration classes:	- main.TestMycat1Application
----
@RunWith(SpringRunner.class)
@SpringBootTest
@SpringBootConfiguration //有错 Found multiple @SpringBootConfiguration annotated classes //https://segmentfault.com/a/1190000010854538
----
@RunWith(SpringRunner.class)
@SpringBootTest
public class TstConfirmTranInSingleMachine {
    @Configuration //参考https://segmentfault.com/a/1190000010854538使用内嵌@Configuration 的class方式，还是有问题，可以执行，但是发现事务没起作用
    @ComponentScan( basePackages = {"g1"}, //basePackageClasses = {TestMycat1Application.class},
            excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = TestMycat1Application.class) })
    static class Config {
    }
}
----



另外，关于并发访问的测试，TODO

 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {JustApplication1.class})
public class TstConfirmTranInSingleMachine {


    final Logger logger             = LoggerFactory.getLogger(getClass());

    @Autowired
    SimpleDistributedTranService simpleDistributedTranService;
    @Autowired
    AppSimpleDistributedTran appSimpleDistributedTran;

    @Autowired
    SimpleAccountMapper simpleAccountMapper;
    @Autowired
    SimpleAccountTransferStateMapper simpleAccountTransferStateMapper;

    boolean specialTst = false;//false;

/*
这里确认单机事务正常。这里是from方的2条数据操作，应该属于同一个分片，应该在一个事务里面。
 */
    @Test
    public void tst_doWholeProcessSimplyWhenFirst_step1TransferAtFromSide_1(){
        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" enter");

        SimpleDistributedTranService.StructIdsTransfer idsObj = SimpleDistributedTranService.StructIdsTransfer.generate();
        long fromUserId = idsObj.fromUserId;
        long toUserId = idsObj.toUserId;
        long transferAmount = SimpleDistributedTranService.InitAmount / 10;
        simpleDistributedTranService.initUserAccount(fromUserId,toUserId, SimpleDistributedTranService.InitAmount);

        SimpleAccount fromUserAccount1 = simpleAccountMapper.getAccountByPk(fromUserId, SimpleDistributedTranService.AccountType);
        SimpleAccount toUserAccount1 = simpleAccountMapper.getAccountByPk(toUserId, SimpleDistributedTranService.AccountType);

        SimpleDistributedTranService.clearAll_UnitTestErr_flags();
        SimpleDistributedTranService.UnitTestErr_doWholeProcessSimplyWhenFirst_step1TransferAtFromSide_1 = true;
        try {
            if (!specialTst){
                simpleDistributedTranService.doWholeProcessSimplyWhenFirst_step1TransferAtFromSide(fromUserId, transferAmount, toUserId, idsObj.atsId);
            }else{
                simpleDistributedTranService.doWholeProcessSimplyWhenFirst_step1TransferAtFromSide_err(fromUserId, transferAmount, toUserId, idsObj.atsId);
            }
            assert (false);
        }catch (MyBaseException e){
            assert(e.forTest && "UnitTestErr_doWholeProcessSimplyWhenFirst_step1TransferAtFromSide_1".equals(e.getMessage()));
        }
        SimpleAccount fromUserAccount2 = simpleAccountMapper.getAccountByPk(fromUserId, SimpleDistributedTranService.AccountType);
        SimpleAccount toUserAccount2 = simpleAccountMapper.getAccountByPk(toUserId, SimpleDistributedTranService.AccountType);
        SimpleAccountTransferState satsObj2 = simpleAccountTransferStateMapper.getByPkAndPartKey(idsObj.atsId,fromUserId);
        assert (satsObj2 == null);
        assert (fromUserAccount2.getAmount() == fromUserAccount1.getAmount());


        SimpleDistributedTranService.clearAll_UnitTestErr_flags();
        SimpleDistributedTranService.UnitTestErr_doWholeProcessSimplyWhenFirst_step1TransferAtFromSide_2 = true;
        try {
            simpleDistributedTranService.doWholeProcessSimplyWhenFirst_step1TransferAtFromSide(fromUserId, transferAmount, toUserId, idsObj.atsId);
            assert (false);
        }catch (MyBaseException e){
            assert(e.forTest && "UnitTestErr_doWholeProcessSimplyWhenFirst_step1TransferAtFromSide_2".equals(e.getMessage()));
        }
        SimpleAccount fromUserAccount3 = simpleAccountMapper.getAccountByPk(fromUserId, SimpleDistributedTranService.AccountType);
        SimpleAccount toUserAccount3 = simpleAccountMapper.getAccountByPk(toUserId, SimpleDistributedTranService.AccountType);
        SimpleAccountTransferState satsObj3 = simpleAccountTransferStateMapper.getByPkAndPartKey(idsObj.atsId,fromUserId);
        assert (satsObj3 == null);
        assert (fromUserAccount3.getAmount() == fromUserAccount1.getAmount());
    }

    /*
    这里确认单机事务正常。这里是to方的2条数据操作，应该属于同一个分片，应该在一个事务里面。
     */
    @Test
    public void tst_doWholeProcessSimplyWhenFirst_step2TransferAtToSide(){
        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" enter");

        SimpleDistributedTranService.StructIdsTransfer idsObj = SimpleDistributedTranService.StructIdsTransfer.generate();
        long fromUserId = idsObj.fromUserId;
        long toUserId = idsObj.toUserId;
        long amount0 = SimpleDistributedTranService.InitAmount;
        long transferAmount =  amount0 / 10;
        simpleDistributedTranService.initUserAccount(fromUserId,toUserId,amount0);

        SimpleAccount fromUserAccount1 = simpleAccountMapper.getAccountByPk(fromUserId, SimpleDistributedTranService.AccountType);
        SimpleAccount toUserAccount1 = simpleAccountMapper.getAccountByPk(toUserId, SimpleDistributedTranService.AccountType);

        SimpleDistributedTranService.clearAll_UnitTestErr_flags();
        try {
            simpleDistributedTranService.doWholeProcessSimplyWhenFirst_step1TransferAtFromSide(fromUserId, transferAmount, toUserId, idsObj.atsId);
        }catch (MyBaseException e){
            assert (false);
        }
        SimpleAccount fromUserAccount2 = simpleAccountMapper.getAccountByPk(fromUserId, SimpleDistributedTranService.AccountType);
        SimpleAccount toUserAccount2 = simpleAccountMapper.getAccountByPk(toUserId, SimpleDistributedTranService.AccountType);
        SimpleAccountTransferState fromSatsObj2 = simpleAccountTransferStateMapper.getByPkAndPartKey(idsObj.atsId,fromUserId);
        assert ( SimpleDistributedTranService.SimpleAccountTransferState_didOut.equals(fromSatsObj2.getState()) );
        assert ( fromUserAccount2.getAmount() + transferAmount == fromUserAccount1.getAmount() );

        //-----

        SimpleDistributedTranService.clearAll_UnitTestErr_flags();
        SimpleDistributedTranService.UnitTestErr_doWholeProcessSimplyWhenFirst_step2TransferAtToSide_1 = true;
        try {
            simpleDistributedTranService.doWholeProcessSimplyWhenFirst_step2TransferAtToSide(toUserId, idsObj.atsId, transferAmount, fromUserId);
            assert (false);
        }catch (MyBaseException e){
            assert(e.forTest && "UnitTestErr_doWholeProcessSimplyWhenFirst_step2TransferAtToSide_1".equals(e.getMessage()));
        }
        SimpleAccount fromUserAccount10 = simpleAccountMapper.getAccountByPk(fromUserId, SimpleDistributedTranService.AccountType);
        SimpleAccount toUserAccount10 = simpleAccountMapper.getAccountByPk(toUserId, SimpleDistributedTranService.AccountType);
        SimpleAccountTransferState toSatsObj10 = simpleAccountTransferStateMapper.getByPkAndPartKey(idsObj.atsId,toUserId);
        assert (toSatsObj10 == null);
        assert (toUserAccount10.getAmount() == toUserAccount1.getAmount());


        SimpleDistributedTranService.clearAll_UnitTestErr_flags();
        SimpleDistributedTranService.UnitTestErr_doWholeProcessSimplyWhenFirst_step2TransferAtToSide_2 = true;
        try {
            simpleDistributedTranService.doWholeProcessSimplyWhenFirst_step2TransferAtToSide(toUserId, idsObj.atsId, transferAmount, fromUserId);
            assert (false);
        }catch (MyBaseException e){
            assert(e.forTest && "UnitTestErr_doWholeProcessSimplyWhenFirst_step2TransferAtToSide_2".equals(e.getMessage()));
        }
        SimpleAccount fromUserAccount11 = simpleAccountMapper.getAccountByPk(fromUserId, SimpleDistributedTranService.AccountType);
        SimpleAccount toUserAccount11 = simpleAccountMapper.getAccountByPk(toUserId, SimpleDistributedTranService.AccountType);
        SimpleAccountTransferState toSatsObj11 = simpleAccountTransferStateMapper.getByPkAndPartKey(idsObj.atsId,toUserId);
        assert (toSatsObj11 == null);
        assert (toUserAccount11.getAmount() == toUserAccount1.getAmount());


        SimpleDistributedTranService.clearAll_UnitTestErr_flags();
        try {
            simpleDistributedTranService.doWholeProcessSimplyWhenFirst_step2TransferAtToSide(toUserId, idsObj.atsId, transferAmount, fromUserId);
        }catch (MyBaseException e){
            assert (false);
        }
        SimpleAccount fromUserAccount12 = simpleAccountMapper.getAccountByPk(fromUserId, SimpleDistributedTranService.AccountType);
        SimpleAccount toUserAccount12 = simpleAccountMapper.getAccountByPk(toUserId, SimpleDistributedTranService.AccountType);
        SimpleAccountTransferState toSatsObj12 = simpleAccountTransferStateMapper.getByPkAndPartKey(idsObj.atsId,toUserId);
        assert ( SimpleDistributedTranService.SimpleAccountTransferState_didIn.equals(toSatsObj12.getState()) );
        assert (toUserAccount12.getAmount()  == toUserAccount1.getAmount() + transferAmount);
    }


    @Test
    public void tst_wholeProcess_1(){
        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" enter");

        SimpleDistributedTranService.StructIdsTransfer idsObj = SimpleDistributedTranService.StructIdsTransfer.generate();
        long fromUserId = idsObj.fromUserId;
        long toUserId = idsObj.toUserId;
        long amount0 = SimpleDistributedTranService.InitAmount;
        long transferAmount =  amount0 / 10;
        simpleDistributedTranService.initUserAccount(fromUserId,toUserId,amount0);
        SimpleAccount fromUserAccount1 = simpleAccountMapper.getAccountByPk(fromUserId, SimpleDistributedTranService.AccountType);
        SimpleAccount toUserAccount1 = simpleAccountMapper.getAccountByPk(toUserId, SimpleDistributedTranService.AccountType);

        SimpleDistributedTranService.clearAll_UnitTestErr_flags();
        appSimpleDistributedTran.doWholeProcessSimplyWhenFirst(idsObj,false, transferAmount);//模仿由某个操作触发转账操作的第一次的正常处理
        if (true){
            SimpleAccount fromUserAccount2 = simpleAccountMapper.getAccountByPk(fromUserId, SimpleDistributedTranService.AccountType);
            SimpleAccount toUserAccount2 = simpleAccountMapper.getAccountByPk(toUserId, SimpleDistributedTranService.AccountType);
            SimpleAccountTransferState fromSatsObj2 = simpleAccountTransferStateMapper.getByPkAndPartKey(idsObj.atsId,fromUserId);
            SimpleAccountTransferState toSatsObj2 = simpleAccountTransferStateMapper.getByPkAndPartKey(idsObj.atsId,toUserId);
            assert ( SimpleDistributedTranService.SimpleAccountTransferState_finish.equals(fromSatsObj2.getState()) );
            assert ( SimpleDistributedTranService.SimpleAccountTransferState_didIn.equals(toSatsObj2.getState()) );
            assert (fromUserAccount2.getAmount()  == fromUserAccount1.getAmount() - transferAmount);
            assert (toUserAccount2.getAmount()  == toUserAccount1.getAmount() + transferAmount);
        }

        SimpleDistributedTranService.clearAll_UnitTestErr_flags();
        appSimpleDistributedTran.checkAndDealWholeProcess(fromUserId,idsObj.atsId);//模仿收到延时消息后的正常处理，幂等
        if (true){
            SimpleAccount fromUserAccount2 = simpleAccountMapper.getAccountByPk(fromUserId, SimpleDistributedTranService.AccountType);
            SimpleAccount toUserAccount2 = simpleAccountMapper.getAccountByPk(toUserId, SimpleDistributedTranService.AccountType);
            SimpleAccountTransferState fromSatsObj2 = simpleAccountTransferStateMapper.getByPkAndPartKey(idsObj.atsId,fromUserId);
            SimpleAccountTransferState toSatsObj2 = simpleAccountTransferStateMapper.getByPkAndPartKey(idsObj.atsId,toUserId);
            assert ( SimpleDistributedTranService.SimpleAccountTransferState_finish.equals(fromSatsObj2.getState()) );
            assert ( SimpleDistributedTranService.SimpleAccountTransferState_didIn.equals(toSatsObj2.getState()) );
            assert (fromUserAccount2.getAmount()  == fromUserAccount1.getAmount() - transferAmount);
            assert (toUserAccount2.getAmount()  == toUserAccount1.getAmount() + transferAmount);
        }
    }





}
