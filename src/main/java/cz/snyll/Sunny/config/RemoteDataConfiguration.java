package cz.snyll.Sunny.config;

import cz.snyll.Sunny.services.datasenders.InfluxDbDataSender;
import cz.snyll.Sunny.services.datasenders.RemoteDataSender;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.naming.ConfigurationException;

@Data
@Configuration
public class RemoteDataConfiguration {
    private InfluxDbConfiguration influxDbConfiguration;
    private MainConfiguration mainConfiguration;
    @Autowired
    public RemoteDataConfiguration(InfluxDbConfiguration influxDbConfiguration, MainConfiguration mainConfiguration) {
        this.influxDbConfiguration = influxDbConfiguration;
        this.mainConfiguration = mainConfiguration;
    }

    @Bean
    public RemoteDataSender remoteDataSender() throws ConfigurationException {
        String remoteStorage = mainConfiguration.getRemoteDataStorage();
        if (remoteStorage == null)
            return null;
        if (remoteStorage.equals("influxdb")) {
            return new InfluxDbDataSender(this.influxDbConfiguration);
        } else if (remoteStorage.equals("")) {
            return null;
        } else {
            throw new ConfigurationException("invalid remote storage settings " + remoteStorage);
        }
    }
}
