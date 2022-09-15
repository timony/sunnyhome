package cz.snyll.Sunny.services.collectors;

import cz.snyll.Sunny.config.InfoDataConfiguration;
import cz.snyll.Sunny.config.RemoteDataConfiguration;
import cz.snyll.Sunny.domain.InfoData;
import cz.snyll.Sunny.repositories.InfoDataRepository;
import cz.snyll.Sunny.services.datasenders.RemoteDataSender;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class DataCollectorAbstractService implements DataCollector {
    @Autowired
    private RemoteDataConfiguration remoteDataConfiguration;
    @Autowired
    private InfoDataConfiguration infoDataConfiguration;
    @Autowired(required = false)
    private RemoteDataSender remoteDataSender;
    @Getter
    private InfoDataRepository infoDataRepository;
    @Autowired
    public DataCollectorAbstractService(InfoDataRepository infoDataRepository) {
        this.infoDataRepository = infoDataRepository;
    }

    public abstract void CollectData();

    @Override
    public void SaveInfoData(HashMap<String, Map.Entry<String, String>> map) {
        for (Map.Entry mapElement : map.entrySet()) {
            String dataKey = (String)mapElement.getKey();
            Map.Entry<String, String> dataEntry = (Map.Entry<String, String>)mapElement.getValue();

            String dataValue = dataEntry.getKey();
            String dataUnit = dataEntry.getValue();

            InfoData infoData = this.infoDataRepository.findByDataKey(dataKey);
            if (infoData == null)
                infoData = new InfoData();
            infoData.setDataKey(dataKey);
            infoData.setDataValue(dataValue);
            infoData.setUnits(dataUnit);
            infoData.setDataFreshness(new Date());
            this.infoDataRepository.save(infoData);
            SendDataToRemote(infoData);
        }
    }

    private HashMap<String, Long> infoDataSentMap = new HashMap<>();

    public void SendDataToRemote(InfoData infoData) {
        if (remoteDataSender == null) {
            //System.out.println("REMOTE DATA: remote storage not defined, not sending data to remote.");
            return;
        } else {
            // do not send all data every time its collected
            HashMap<String, String> infoDataRemoteMap = infoDataConfiguration.getRemoteMap();
            String secondsString = infoDataRemoteMap.get(infoData.getDataKey());
            long secondsLong;
            try {
                secondsLong = Long.parseLong(secondsString);
            } catch (Exception e) {
                return;
            }
            if (infoDataSentMap.containsKey(infoData.getDataKey())) {
                // if time elapsed since last data send, then send data otherwise no action
                if (Instant.now().getEpochSecond() >= (infoDataSentMap.get(infoData.getDataKey()) + secondsLong)) {
                    //System.out.println("REMOTE DATA: Time elapsed, sending remote data for " + infoData.getDataKey());
                    remoteDataSender.SendData(infoData);
                    infoDataSentMap.put(infoData.getDataKey(), Instant.now().getEpochSecond());
                } else {
                    //System.out.println("REMOTE DATA: Time not reached for " + infoData.getDataKey() + "; last sent time = " + infoDataSentMap.get(infoData.getDataKey()) + "; compared with = " + Instant.now().plusSeconds(secondsLong).getEpochSecond());
                }
            } else {
                remoteDataSender.SendData(infoData);
                //System.out.println("REMOTE DATA: infoDataSentMap adding key " + infoData.getDataKey());
                infoDataSentMap.put(infoData.getDataKey(), Instant.now().getEpochSecond());
            }
        }
    }
}
