package com.nongthinh.rice_disease_diagnosis_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.nongthinh.rice_disease_diagnosis_service.configuration.ModelValidationProperties;
import com.nongthinh.rice_disease_diagnosis_service.configuration.DiagnosisProperties;

import java.util.TimeZone;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@EnableConfigurationProperties({ModelValidationProperties.class, DiagnosisProperties.class})
public class RiceDiseaseDiagnosisServiceApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(RiceDiseaseDiagnosisServiceApplication.class, args);
	}

}
