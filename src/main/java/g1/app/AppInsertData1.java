package g1.app;


import com.alibaba.fastjson.JSON;
import g1.cfg.MyAppProperties;
import g1.ibatisMapper.*;
import g1.pojo.*;
import g1.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties({MyAppProperties.class})
public class AppInsertData1 {


    final Logger logger             = LoggerFactory.getLogger(getClass());

    @Autowired
    private MyAppProperties properties;



    @Autowired
    TblPartByModMapper tblPartByModMapper;
    @Autowired
    TblPartByHashMapper tblPartByHashMapper;
    @Autowired
    TblPartByHashKeyMapper tblPartByHashKeyMapper;


    public void main1(String[] args){
//        f1InsertSomeData();
        f2QueryData();
    }


    public void f1InsertSomeData(){
        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" enter,properties="+ JSON.toJSONString(properties,true));

        int n = properties.getInsertRowCnt();

        logger.info("begin tblPartByModMapper doing insert ");
        for (int i = 0; i < n; i++) {
            long id = i;
            String name = "n"+i;
            TblOnlyIntId pojo = new TblOnlyIntId();
            pojo.setTblId(id);
            pojo.setName(name);
            tblPartByModMapper.insert(pojo);
        }

        logger.info("begin tblPartByHashMapper doing insert ");
        for (int i = 0; i < n; i++) {
            long id = i;
            String name = "n"+i;
            TblOnlyIntId pojo = new TblOnlyIntId();
            pojo.setTblId(id);
            pojo.setName(name);
            tblPartByHashMapper.insert(pojo);
        }

        logger.info("begin tblPartByHashKeyMapper doing insert ");
        for (int i = 0; i < n; i++) {
            String key = "k"+ i;
            TblOnlyStrId pojo = new TblOnlyStrId();
            pojo.setKeyId(key);
            tblPartByHashKeyMapper.insert(pojo);
        }

        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" exit");
    }

    public void f2QueryData(){
        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" enter,properties="+ JSON.toJSONString(properties,true));


        int n = properties.getInsertRowCnt();

        logger.info("begin tblPartByModMapper doing select ");
        for (int i = 0; i < n; i++) {
            long id = i;
            TblOnlyIntId pojo = tblPartByModMapper.getById(id);
            if (pojo == null){
                throw new RuntimeException("tblPartByModMapper.getById("+id+") ret null");
            }
        }

        logger.info("begin tblPartByHashMapper doing select ");
        for (int i = 0; i < n; i++) {
            long id = i;
            TblOnlyIntId pojo = tblPartByHashMapper.getById(id);
            if (pojo == null){
                throw new RuntimeException("tblPartByHashMapper.getById("+id+") ret null");
            }
        }

        logger.info("begin tblPartByHashKeyMapper doing select ");
        for (int i = 0; i < n; i++) {
            String key = "k"+ i;
            TblOnlyStrId pojo = tblPartByHashKeyMapper.getById(key);
            if (pojo == null){
                throw new RuntimeException("tblPartByHashKeyMapper.getById("+key+") ret null");
            }
        }

        logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" exit");
    }





}
/*





20171030_215212.000 [main] INFO  AppInsertData1.java:42 - g1.app.AppInsertData1.f1InsertSomeData enter,properties={
	"insertRowCnt":5000
}
20171030_215212.001 [main] INFO  AppInsertData1.java:46 - begin tblPartByModMapper doing insert
20171030_215518.334 [main] INFO  AppInsertData1.java:54 - begin tblPartByHashMapper doing insert
20171030_215823.673 [main] INFO  AppInsertData1.java:62 - begin tblPartByHashKeyMapper doing insert
20171030_220143.608 [main] INFO  AppInsertData1.java:70 - g1.app.AppInsertData1.f1InsertSomeData exit
20171030_220143.609 [main] INFO  AppInsertData1.java:74 - g1.app.AppInsertData1.f2QueryData enter,properties={
	"insertRowCnt":5000
}
20171030_220143.609 [main] INFO  AppInsertData1.java:79 - begin tblPartByModMapper doing select
20171030_220152.950 [main] INFO  AppInsertData1.java:88 - begin tblPartByHashMapper doing select
20171030_220201.083 [main] INFO  AppInsertData1.java:97 - begin tblPartByHashKeyMapper doing select
20171030_220208.593 [main] INFO  AppInsertData1.java:106 - g1.app.AppInsertData1.f2QueryData exit










 */