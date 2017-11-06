package g1.ibatisMapper;

import g1.pojo.P5Account;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface P5AccountJiFenMapper {



    @Select("SELECT * FROM P5AccountJiFen WHERE userId = #{userId} ")
    P5Account getAccountByPk(@Param("userId") long userId);
    @Select("SELECT * FROM P5AccountJiFen WHERE userId = #{userId}  FOR UPDATE")
    P5Account getAccountByPkForUpdate(@Param("userId") long userId);




    @Insert("INSERT INTO P5AccountJiFen (`userId`,`amount`,`rowver`) VALUES (#{userId},#{amount},#{rowver})")
    int insertAccount(P5Account account);



    @Update("UPDATE P5AccountJiFen SET `amount`=`amount`+#{amountDelta}, `rowver`=#{newRowver}  WHERE `userId`=#{userId}  AND `rowver`=#{oldRowver}")
    int updateAccountByDelta(@Param("newRowver") long newRowver, @Param("oldRowver") long oldRowver,
                             @Param("userId") long userId, @Param("amountDelta") long amountDelta);

    @Update("UPDATE P5AccountJiFen SET `amount`=#{amount}, `rowver`=#{newRowver} WHERE `userId`=#{userId} AND `rowver`=#{oldRowver}")
    int updateAccountByAmount(@Param("newRowver") long newRowver, @Param("oldRowver") long oldRowver,
                              @Param("userId") long userId, @Param("amount") long amount);





}