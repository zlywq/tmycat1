package g1.ibatisMapper;


import g1.pojo.TblOnlyIntId;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface TblPartByHashMapper {

    @Select("SELECT  *  FROM TblPartByHash WHERE tblId = #{tblId}")
    TblOnlyIntId getById(@Param("tblId") long tblId);



    @Insert("INSERT INTO TblPartByHash (`tblId`,`name`) VALUES (#{tblId},#{name}) ")
    int insert(TblOnlyIntId pojo);


}
