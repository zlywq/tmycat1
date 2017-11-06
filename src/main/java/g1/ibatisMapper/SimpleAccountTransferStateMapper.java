package g1.ibatisMapper;

import g1.pojo.*;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface SimpleAccountTransferStateMapper {

    @Select("SELECT * FROM SimpleAccountTransferState WHERE atsId = #{atsId} AND idForPart = #{idForPart} FOR UPDATE")
    SimpleAccountTransferState getByPkAndPartKeyForUpdate(@Param("atsId") long atsId, @Param("idForPart") long idForPart);
    @Select("SELECT * FROM SimpleAccountTransferState WHERE atsId = #{atsId} AND idForPart = #{idForPart} ")
    SimpleAccountTransferState getByPkAndPartKey(@Param("atsId") long atsId, @Param("idForPart") long idForPart);

    @Insert("INSERT INTO SimpleAccountTransferState (`atsId`,`idForPart`,`state`,`fromUserId`,`fromAccountType`,`toUserId`,`toAccountType`,`amount`,`rowver`) VALUES (#{atsId},#{idForPart},#{state},#{fromUserId},#{fromAccountType},#{toUserId},#{toAccountType},#{amount},#{rowver})")
    int insert(SimpleAccountTransferState record);




//    @Update("UPDATE SimpleAccountTransferState SET `idForPart`=#{idForPart},`state`=#{state},`fromUserId`=#{fromUserId},`fromAccountType`=#{fromAccountType},`toUserId`=#{toUserId},`toAccountType`=#{toAccountType},`amount`=#{amount},`rowver`=#{rowver} WHERE `atsId`=#{atsId}")
//    int updateSimple(SimpleAccountTransferState record);

    @Update("UPDATE SimpleAccountTransferState SET `state`=#{state}, `rowver`=#{newRowver} WHERE `atsId`=#{atsId} AND `idForPart`=#{idForPart} AND `rowver`=#{oldRowver}")
    int updateState(@Param("newRowver") long newRowver, @Param("oldRowver") long oldRowver,
        @Param("state") String state, @Param("atsId") long atsId, @Param("idForPart") long idForPart);












}