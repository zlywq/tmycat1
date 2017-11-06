package g1.pojo;


import java.io.Serializable;

public class TblOnlyIntId implements Serializable {


    private long tblId;

    private String name;


    public long getTblId(){
        return tblId;
    }
    public void setTblId(long tblId){
        this.tblId=tblId;
    }



    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name=name;
    }

}
