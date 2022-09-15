package cz.snyll.Sunny.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@Data
@ConfigurationProperties(prefix = "influxdb")
@ConfigurationPropertiesScan
public class InfluxDbConfiguration {
    private String apiToken;
    private String bucket;
    private String org;
    private String url;
}