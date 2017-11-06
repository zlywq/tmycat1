package g1.ibatisMapper;


import g1.pojo.*;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface TblPartByHashKeyMapper {

    @Select("SELECT  *  FROM TblPartByHashKey WHERE keyId = #{keyId}")
    TblOnlyStrId getById(@Param("keyId") String keyId);



    @Insert("INSERT INTO TblPartByHashKey (`keyId`) VALUES (#{keyId}) ")
    int insert(TblOnlyStrId pojo);


}
