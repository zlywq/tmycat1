package g1.ibatisMapper;



import g1.pojo.*;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TblPartByModMapper {

    @Select("SELECT  *  FROM TblPartByMod WHERE tblId = #{tblId}")
    TblOnlyIntId getById(@Param("tblId") long tblId);



    @Insert("INSERT INTO TblPartByMod (`tblId`,`name`) VALUES (#{tblId},#{name}) ")
    int insert(TblOnlyIntId pojo);


}
