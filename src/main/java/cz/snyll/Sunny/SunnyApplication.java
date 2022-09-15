package cz.snyll.Sunny;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan
@ConfigurationPropertiesScan("cz.snyll.Sunny.config")
@EnableConfigurationProperties
public class SunnyApplication {
	private static final Logger LOGGER=LoggerFactory.getLogger(SunnyApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(SunnyApplication.class, args);
	}
}
