package g1.pojo;

import java.io.Serializable;


//SELECT `userId`,`accountType`,`amount`,`rowver` FROM SimpleAccount
//INSERT INTO SimpleAccount (`userId`,`accountType`,`amount`,`rowver`) VALUES (#{userId},#{accountType},#{amount},#{rowver})
//UPDATE SimpleAccount SET `amount`=#{amount},`rowver`=#{rowver} WHERE `userId`=#{userId} AND `accountType`=#{accountType}
//DELETE FROM SimpleAccount WHERE `userId`=#{userId} AND `accountType`=#{accountType}


public class SimpleAccount implements Serializable {
	private static final long serialVersionUID = -1739631831920726887L;
	private long userId;
	private String accountType;
	private long amount;
	private long rowver;


	public long getUserId(){
		return userId;
	}
	public void setUserId(long userId){
		this.userId=userId;
	}

	public String getAccountType(){
		return accountType;
	}
	public void setAccountType(String accountType){
		this.accountType=accountType;
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

