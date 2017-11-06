package main;

import g1.ForScanPosition;
import g1.app.*;
import g1.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication( scanBasePackageClasses ={ForScanPosition.class} )
public class TestMycat1Application implements CommandLineRunner {


	final Logger logger             = LoggerFactory.getLogger(getClass());

	@Autowired
	AppInsertData1 appInsertData1;


	public static void main(String[] args) {
		SpringApplication.run(TestMycat1Application.class, args);
	}

	public void run(String... args) throws Exception {
		logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" enter");

		appInsertData1.main1(args);


		logger.info(""+ Tool.getCurrentClassName()+"."+Tool.getCurrentMethodName()+" exit");
	}
}
