package g1.ibatisMapper;

import g1.pojo.*;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface SimpleAccountMapper {



    @Select("SELECT * FROM SimpleAccount WHERE userId = #{userId} AND `accountType`=#{accountType}")
    SimpleAccount getAccountByPk(@Param("userId") long userId, @Param("accountType") String accountType);
    @Select("SELECT * FROM SimpleAccount WHERE userId = #{userId} AND `accountType`=#{accountType} FOR UPDATE")
    SimpleAccount getAccountByPkForUpdate(@Param("userId") long userId, @Param("accountType") String accountType);

    @Select("SELECT  *  FROM SimpleAccount WHERE userId = #{userId}")
    List<SimpleAccount> getAccountsByUser(@Param("userId") long userId);



    @Insert("INSERT INTO SimpleAccount (`userId`,`accountType`,`amount`,`rowver`) VALUES (#{userId},#{accountType},#{amount},#{rowver})")
    int insertAccount(SimpleAccount account);



    @Update("UPDATE SimpleAccount SET `amount`=`amount`+#{amountDelta}, `rowver`=#{newRowver}  WHERE `userId`=#{userId} AND `accountType`=#{accountType} AND `rowver`=#{oldRowver}")
    int updateAccountByDelta(@Param("newRowver") long newRowver, @Param("oldRowver") long oldRowver,
        @Param("userId") long userId, @Param("accountType") String accountType,@Param("amountDelta") long amountDelta);

    @Update("UPDATE SimpleAccount SET `amount`=#{amount}, `rowver`=#{newRowver} WHERE `userId`=#{userId} AND `accountType`=#{accountType} AND `rowver`=#{oldRowver}")
    int updateAccountByAmount(@Param("newRowver") long newRowver, @Param("oldRowver") long oldRowver,
        @Param("userId") long userId, @Param("accountType") String accountType, @Param("amount") long amount);





}