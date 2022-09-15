package cz.snyll.Sunny.services.datasenders;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import cz.snyll.Sunny.config.InfluxDbConfiguration;
import cz.snyll.Sunny.domain.InfoData;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
@Data
public class InfluxDbDataSender implements RemoteDataSender {
    String token;
    String bucket;
    String org;
    InfluxDBClient client;

    public InfluxDbDataSender(InfluxDbConfiguration influxDbConfiguration) {
        this.token = influxDbConfiguration.getApiToken();
        if (this.token == null)
            return;
        this.bucket = influxDbConfiguration.getBucket();
        this.org = influxDbConfiguration.getOrg();
        this.client = InfluxDBClientFactory.create(influxDbConfiguration.getUrl(), token.toCharArray());
    }

    @Override
    public void SendData(InfoData infoData) {
        String data = "InfoData,dataKey=" + infoData.getDataKey() + " dataValue=" + infoData.getDataValue();
        //System.out.println("INFLUX: Sending remote data: " + data);
        WriteApiBlocking writeApi = this.client.getWriteApiBlocking();
        writeApi.writeRecord(this.bucket, this.org, WritePrecision.NS, data);
    }
}