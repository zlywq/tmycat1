package g1.pojo;

import java.io.Serializable;


public class P5AccountTransferItem implements Serializable {
	private long fromUserId;
	private String fromAccountType;
	private long toUserId;
	private String toAccountType;
	private long amount;


	public long getFromUserId(){
		return fromUserId;
	}
	public void setFromUserId(long fromUserId){
		this.fromUserId=fromUserId;
	}
	public String getFromAccountType(){
		return fromAccountType;
	}
	public void setFromAccountType(String fromAccountType){
		this.fromAccountType=fromAccountType;
	}

	public long getToUserId(){
		return toUserId;
	}
	public void setToUserId(long toUserId){
		this.toUserId=toUserId;
	}
	public String getToAccountType(){
		return toAccountType;
	}
	public void setToAccountType(String toAccountType){
		this.toAccountType=toAccountType;
	}

	public long getAmount(){
		return amount;
	}
	public void setAmount(long amount){
		this.amount=amount;
	}



}

