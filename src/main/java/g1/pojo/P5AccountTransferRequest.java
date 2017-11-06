package g1.pojo;

import java.io.Serializable;


//SELECT `atrId`,`fromUserId`,`toUserId`,`state`,`detailJson`,`shuoming`,`amount`,`rowver` FROM P5AccountTransferRequest
//INSERT INTO P5AccountTransferRequest (`atrId`,`fromUserId`,`toUserId`,`state`,`detailJson`,`shuoming`,`amount`,`rowver`) VALUES (#{atrId},#{fromUserId},#{toUserId},#{state},#{detailJson},#{shuoming},#{amount},#{rowver})
//UPDATE P5AccountTransferRequest SET `fromUserId`=#{fromUserId},`toUserId`=#{toUserId},`state`=#{state},`detailJson`=#{detailJson},`shuoming`=#{shuoming},`amount`=#{amount},`rowver`=#{rowver} WHERE `atrId`=#{atrId}
//DELETE FROM P5AccountTransferRequest WHERE `atrId`=#{atrId}


public class P5AccountTransferRequest implements Serializable {

	private long atrId;
	private long fromUserId;
	private long toUserId;
	private String state;

	private String detailJson;
	private String shuoming;
	private long amount;
	private long rowver;


	public long getAtrId(){
		return atrId;
	}
	public void setAtrId(long atrId){
		this.atrId=atrId;
	}
	public long getFromUserId(){
		return fromUserId;
	}
	public void setFromUserId(long fromUserId){
		this.fromUserId=fromUserId;
	}
	public long getToUserId(){
		return toUserId;
	}
	public void setToUserId(long toUserId){
		this.toUserId=toUserId;
	}
	public String getState(){
		return state;
	}
	public void setState(String state){
		this.state=state;
	}

	public String getDetailJson(){
		return detailJson;
	}
	public void setDetailJson(String detailJson){
		this.detailJson=detailJson;
	}
	public String getShuoming(){
		return shuoming;
	}
	public void setShuoming(String shuoming){
		this.shuoming=shuoming;
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

