package cz.snyll.Sunny.services.collectors;


import cz.snyll.Sunny.domain.InfoData;

import java.util.HashMap;
import java.util.Map;

public interface DataCollector {
    void CollectData();
    void SaveInfoData(HashMap<String, Map.Entry<String, String>> map);
    void SendDataToRemote(InfoData infoData);
}