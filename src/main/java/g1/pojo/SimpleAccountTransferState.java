package g1.pojo;

import java.io.Serializable;


//SELECT `atsId`,`idForPart`,`state`,`fromUserId`,`fromAccountType`,`toUserId`,`toAccountType`,`amount`,`rowver` FROM SimpleAccountTransferState
//INSERT INTO SimpleAccountTransferState (`atsId`,`idForPart`,`state`,`fromUserId`,`fromAccountType`,`toUserId`,`toAccountType`,`amount`,`rowver`) VALUES (#{atsId},#{idForPart},#{state},#{fromUserId},#{fromAccountType},#{toUserId},#{toAccountType},#{amount},#{rowver})
//UPDATE SimpleAccountTransferState SET `idForPart`=#{idForPart},`state`=#{state},`fromUserId`=#{fromUserId},`fromAccountType`=#{fromAccountType},`toUserId`=#{toUserId},`toAccountType`=#{toAccountType},`amount`=#{amount},`rowver`=#{rowver} WHERE `atsId`=#{atsId}
//DELETE FROM SimpleAccountTransferState WHERE `atsId`=#{atsId}


public class SimpleAccountTransferState implements Serializable {

	private static final long serialVersionUID = -728484115264206811L;
	private long atsId;
	private long idForPart;
	private String state;

	private long fromUserId;
	private String fromAccountType;
	private long toUserId;
	private String toAccountType;
	private long amount;
	private long rowver;


	public long getAtsId(){
		return atsId;
	}
	public void setAtsId(long atsId){
		this.atsId=atsId;
	}

	public long getIdForPart(){
		return idForPart;
	}
	public void setIdForPart(long idForPart){
		this.idForPart=idForPart;
	}

	public String getState(){
		return state;
	}
	public void setState(String state){
		this.state=state;
	}


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

	public long getRowver(){
		return rowver;
	}
	public void setRowver(long rowver){
		this.rowver=rowver;
	}









}

