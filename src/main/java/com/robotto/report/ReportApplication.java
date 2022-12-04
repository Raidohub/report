package com.robotto.report;

import com.robotto.report.infrastructure.properties.ReportProp;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;


@EnableDubbo
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(value = {"com.robotto.report", "com.robotto.base"})
@EnableConfigurationProperties(ReportProp.class)
@MapperScan(value = {"com.robotto.report.infrastructure.persistence.mapper",
		"com.robotto.base.infrastructure.persistence.mapper"})
public class ReportApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReportApplication.class, args);
	}

}
