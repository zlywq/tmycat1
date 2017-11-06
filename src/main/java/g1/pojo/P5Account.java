package g1.pojo;

import java.io.Serializable;


//SELECT `userId`,`amount`,`rowver` FROM P5Account
//INSERT INTO P5Account (`userId`,`amount`,`rowver`) VALUES (#{userId},#{amount},#{rowver})
//UPDATE P5Account SET `amount`=#{amount},`rowver`=#{rowver} WHERE `userId`=#{userId}
//DELETE FROM P5Account WHERE `userId`=#{userId}


public class P5Account implements Serializable {

	private long userId;
	private long amount;
	private long rowver;


	public long getUserId(){
		return userId;
	}
	public void setUserId(long userId){
		this.userId=userId;
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

