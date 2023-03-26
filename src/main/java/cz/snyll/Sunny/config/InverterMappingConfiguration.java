package cz.snyll.Sunny.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;

@Data
@ConfigurationProperties(prefix = "mapping")
public class InverterMappingConfiguration {
    private HashMap<String, Integer> inverterMap;
}