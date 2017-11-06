package g1;


import com.alibaba.fastjson.JSON;
import g1.pojo.P5AccountTransferItem;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class Test1 {

    @Test
    public void t1(){

        List<P5AccountTransferItem> detailList = new ArrayList<>();
        P5AccountTransferItem item1= new P5AccountTransferItem();
        item1.setFromUserId(1);
        item1.setFromAccountType("a");
        item1.setToUserId(2);
        item1.setToAccountType("a");
        item1.setAmount(3);
        detailList.add(item1);
        P5AccountTransferItem item2= new P5AccountTransferItem();
        item2.setFromUserId(1);
        item2.setFromAccountType("b");
        item2.setToUserId(2);
        item2.setToAccountType("b");
        item2.setAmount(4);
        detailList.add(item2);
        String detailJsonStr = JSON.toJSONString(detailList);
        List<P5AccountTransferItem> detailList2= JSON.parseArray(detailJsonStr,P5AccountTransferItem.class);
        System.out.println("detailList2="+detailList2+" \n "+JSON.toJSONString(detailList2,true));


    }
}
