package g1.cfg;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
@EnableConfigurationProperties({DruidProperties1.class})
@MapperScan(basePackages = {"g1.ibatisMapper"})
//@MapperScan(basePackages = {"g1.ibatisMapper"}, sqlSessionFactoryRef = "masterSqlSessionFactory")
public class DataSourceConfiguration1 {





    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DruidProperties1 properties1;


    @Bean(name = "masterDataSource")
    @Primary
    public DataSource masterDataSource()
    {
        DruidProperties properties = properties1;
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(properties.getDriverClass());
        dataSource.setUrl(properties.getUrl());
        dataSource.setUsername(properties.getUsername());
        dataSource.setPassword(properties.getPassword());
        if (properties.getInitialSize() > 0) {
            dataSource.setInitialSize(properties.getInitialSize());
        }
        if (properties.getMinIdle() > 0) {
            dataSource.setMinIdle(properties.getMinIdle());
        }
        if (properties.getMaxActive() > 0) {
            dataSource.setMaxActive(properties.getMaxActive());
        }
        dataSource.setTestOnBorrow(properties.isTestOnBorrow());
        try {
            dataSource.init();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return dataSource;


    }


//    @Primary
//    @Bean(name = "masterDataSource")
//    @ConfigurationProperties(prefix = "datasource.master")
//    public DataSource dataSource() {
//        return DataSourceBuilder.create().build();
//    }


    @Primary
    @Bean(name = "masterTransactionManager")
    public DataSourceTransactionManager transactionManager(@Qualifier("masterDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Autowired
    private Environment env;

    @Primary
    @Bean(name = "masterSqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("masterDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean fb = new SqlSessionFactoryBean();
        fb.setDataSource(dataSource);


        fb.setTypeAliasesPackage("g1.ibatisMapper");
        fb.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:ibatisXmlMapper/primary/*.xml"));//
        //fb.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:ibatisXmlMapper/**/*.xml"));

        // 据说下面2句仅仅用于*.xml文件，如果整个持久层操作不需要使用到xml文件的话（只用注解就可以搞定），则不加
//        String mybatis_typeAliasesPackage = env.getProperty("mybatis.typeAliasesPackage"); //"g1.ibatisMapper"
//        logger.info("mybatis_typeAliasesPackage="+mybatis_typeAliasesPackage);
//        if (!Util1.isStringEmpty(mybatis_typeAliasesPackage)){
//        	fb.setTypeAliasesPackage(mybatis_typeAliasesPackage);// 指定基包
//        }else{
//        	fb.setTypeAliasesPackage("g1.ibatisMapper");
//        }
//        String mybatis_mapperLocations = env.getProperty("mybatis.mapperLocations");
//        logger.info("mybatis_mapperLocations="+mybatis_mapperLocations);
//        if (!Util1.isStringEmpty(mybatis_mapperLocations)){
//        	fb.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(mybatis_mapperLocations));//
//        }

        return fb.getObject();
    }















}
