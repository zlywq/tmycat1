package g1.cfg;


import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "myapp")
public class MyAppProperties {




    private int insertRowCnt ;



    public int getInsertRowCnt() {
        return this.insertRowCnt;
    }
    public void setInsertRowCnt(int insertRowCnt) {
        this.insertRowCnt = insertRowCnt;
    }

















}
