package cz.snyll.Sunny.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
@Data
@ConfigurationProperties(prefix = "weather")
public class WeatherConfiguration {
    private HashMap<String, String> statusMap;
}