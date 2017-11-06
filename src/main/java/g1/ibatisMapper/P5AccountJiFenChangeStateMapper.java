package g1.ibatisMapper;

import g1.pojo.P5AccountChangeState;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface P5AccountJiFenChangeStateMapper {

    @Select("SELECT * FROM P5AccountJiFenChangeState WHERE acsId = #{acsId} AND idForPart = #{idForPart} FOR UPDATE")
    P5AccountChangeState getByPkAndPartKeyForUpdate(@Param("acsId") long acsId, @Param("idForPart") long idForPart);
    @Select("SELECT * FROM P5AccountJiFenChangeState WHERE acsId = #{acsId} AND idForPart = #{idForPart} ")
    P5AccountChangeState getByPkAndPartKey(@Param("acsId") long acsId, @Param("idForPart") long idForPart);

    @Insert("INSERT INTO P5AccountJiFenChangeState (`acsId`,`idForPart`,`state`,`amount`,`rowver`) VALUES (#{acsId},#{idForPart},#{state},#{amount},#{rowver})")
    int insert(P5AccountChangeState record);




//    @Update("UPDATE P5AccountJiFenChangeState SET `idForPart`=#{idForPart},`state`=#{state},`amount`=#{amount},`rowver`=#{rowver} WHERE `acsId`=#{acsId}")
//    int updateSimple(P5AccountChangeState record);

    @Update("UPDATE P5AccountJiFenChangeState SET `state`=#{state}, `rowver`=#{newRowver} WHERE `acsId`=#{acsId} AND `idForPart`=#{idForPart} AND `rowver`=#{oldRowver}")
    int updateState(@Param("newRowver") long newRowver, @Param("oldRowver") long oldRowver,
                    @Param("state") String state, @Param("acsId") long acsId, @Param("idForPart") long idForPart);












}