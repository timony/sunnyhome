package cz.snyll.Sunny.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "main")
public class MainConfiguration {
    private float totalPanelPowerInstalled;
    private String solaxLocalUrl;
    private String remoteDataStorage;
    private String solaxCloudApitoken;
    private String solaxWifiSn;
    private String solaxCloudApiUrl;
    private boolean automation;
    private String userName;
    private String password;
    private boolean inverter_collecting;
}