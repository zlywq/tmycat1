package g1.pojo;

import java.io.Serializable;


//SELECT `acsId`,`idForPart`,`state``amount`,`rowver` FROM P5AccountChangeState
//INSERT INTO P5AccountChangeState (`acsId`,`idForPart`,`state`,`amount`,`rowver`) VALUES (#{acsId},#{idForPart},#{state},#{amount},#{rowver})
//UPDATE P5AccountChangeState SET `idForPart`=#{idForPart},`state`=#{state},`amount`=#{amount},`rowver`=#{rowver} WHERE `acsId`=#{acsId}
//DELETE FROM P5AccountChangeState WHERE `acsId`=#{acsId}


public class P5AccountChangeState implements Serializable {

	private long acsId; //  here acsId store atrId
	private long idForPart;
//	private long atrId;
	private String state;

	private long amount;
	private long rowver;


	public long getAcsId(){
		return acsId;
	}
	public void setAcsId(long acsId){
		this.acsId=acsId;
	}

	public long getIdForPart(){
		return idForPart;
	}
	public void setIdForPart(long idForPart){
		this.idForPart=idForPart;
	}

//	public long getAtrId(){
//		return atrId;
//	}
//	public void setAtrId(long atrId){
//		this.atrId=atrId;
//	}

	public String getState(){
		return state;
	}
	public void setState(String state){
		this.state=state;
	}


	public long getAmount(){
		return amount;
	}
	public void setAmount(long amount){
		this.amount=amount;
	}

	public long getRowver(){
		return rowver;
	}
	public void setRowver(long rowver){
		this.rowver=rowver;
	}









}

