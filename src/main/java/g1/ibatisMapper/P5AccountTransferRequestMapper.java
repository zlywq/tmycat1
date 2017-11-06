package g1.ibatisMapper;

import g1.pojo.*;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface P5AccountTransferRequestMapper {


    @Select("SELECT * FROM P5AccountTransferRequest WHERE atrId = #{atrId} ")
    P5AccountTransferRequest getByPk(@Param("atrId") long atrId);
    @Select("SELECT * FROM P5AccountTransferRequest WHERE atrId = #{atrId}  FOR UPDATE")
    P5AccountTransferRequest getByPkForUpdate(@Param("atrId") long atrId);




    @Insert("INSERT INTO P5AccountTransferRequest (`atrId`,`fromUserId`,`toUserId`,`state`,`detailJson`,`shuoming`,`amount`,`rowver`) VALUES (#{atrId},#{fromUserId},#{toUserId},#{state},#{detailJson},#{shuoming},#{amount},#{rowver})")
    int insert(P5AccountTransferRequest record);




//    @Update("UPDATE P5AccountTransferRequest SET `fromUserId`=#{fromUserId},`toUserId`=#{toUserId},`state`=#{state},`detailJson`=#{detailJson},`shuoming`=#{shuoming},`amount`=#{amount},`rowver`=#{rowver} WHERE `atrId`=#{atrId}")
//    int updateSimple(P5AccountTransferRequest record);

    @Update("UPDATE P5AccountTransferRequest SET `state`=#{state}, `rowver`=#{newRowver} WHERE `atrId`=#{atrId} AND `rowver`=#{oldRowver}")
    int updateState(@Param("newRowver") long newRowver, @Param("oldRowver") long oldRowver,
                    @Param("state") String state, @Param("atrId") long atrId);












}