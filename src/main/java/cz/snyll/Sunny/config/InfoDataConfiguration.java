package cz.snyll.Sunny.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
@Data
@ConfigurationProperties(prefix = "infodata")
public class InfoDataConfiguration {
    private HashMap<String, String> humanMap;
    private HashMap<String, String> remoteMap;
}
