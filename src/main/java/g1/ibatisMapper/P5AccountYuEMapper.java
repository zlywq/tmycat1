package g1.ibatisMapper;

import g1.pojo.*;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface P5AccountYuEMapper {



    @Select("SELECT * FROM P5AccountYuE WHERE userId = #{userId} ")
    P5Account getAccountByPk(@Param("userId") long userId);
    @Select("SELECT * FROM P5AccountYuE WHERE userId = #{userId}  FOR UPDATE")
    P5Account getAccountByPkForUpdate(@Param("userId") long userId);




    @Insert("INSERT INTO P5AccountYuE (`userId`,`amount`,`rowver`) VALUES (#{userId},#{amount},#{rowver})")
    int insertAccount(P5Account account);



    @Update("UPDATE P5AccountYuE SET `amount`=`amount`+#{amountDelta}, `rowver`=#{newRowver}  WHERE `userId`=#{userId}  AND `rowver`=#{oldRowver}")
    int updateAccountByDelta(@Param("newRowver") long newRowver, @Param("oldRowver") long oldRowver,
                             @Param("userId") long userId, @Param("amountDelta") long amountDelta);

    @Update("UPDATE P5AccountYuE SET `amount`=#{amount}, `rowver`=#{newRowver} WHERE `userId`=#{userId} AND `rowver`=#{oldRowver}")
    int updateAccountByAmount(@Param("newRowver") long newRowver, @Param("oldRowver") long oldRowver,
                              @Param("userId") long userId, @Param("amount") long amount);





}