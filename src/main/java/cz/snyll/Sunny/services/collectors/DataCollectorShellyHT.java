package cz.snyll.Sunny.services.collectors;

import cz.snyll.Sunny.config.ShellyConfiguration;
import cz.snyll.Sunny.domain.EventEntry;
import cz.snyll.Sunny.repositories.InfoDataRepository;
import cz.snyll.Sunny.services.EventEntryManagerService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/***
 * This services receives data from get request made by ShellHT device for measuring temperature and humidity and saves this into database.
 */
@Setter
@Slf4j
@Service
public class DataCollectorShellyHT extends DataCollectorAbstractService {
    @Autowired
    private EventEntryManagerService eventEntryManagerService;

    @Autowired
    private ShellyConfiguration shellyConfiguration;

    public DataCollectorShellyHT(InfoDataRepository infoDataRepository, ShellyConfiguration shellyConfiguration) {
        super(infoDataRepository);
    }
    private String temperature;
    private String humidity;
    private String shellyHtId;

    @Override
    public void CollectData() {
        HashMap<String, Map.Entry<String, String>> dataMap = new HashMap<>();
        HashMap<String, String> shellyMap = this.shellyConfiguration.getShellyHtMap();
        if (shellyMap.get(this.shellyHtId) == null) {
            log.warn("ShellyHT: Unregistered Shelly HT device with id " + this.shellyHtId + " tried to send data. Throwing away.");
            eventEntryManagerService.raiseEvent("ShellyHT: Unregistered Shelly HT device with id \" + this.shellyHtId + \" tried to send data. Throwing away.", EventEntry.EventType.WARNING);
            System.out.println("ShellyHT: Unregistered Shelly HT device with id " + this.shellyHtId + " tried to send data. Throwing away.");
            resetData();
            return;
        }
        if (this.temperature.equals("") || this.humidity.equals("")) {
            log.warn("ShellyHT: Some ShellyHT device reported empty values. Throwing away.");
            eventEntryManagerService.raiseEvent("ShellyHT: Shelly HT device with id \" + this.shellyHtId + \" reported empty values. Throwing away", EventEntry.EventType.WARNING);
            System.out.println("ShellyHT: Some ShellyHT device reported empty values. Throwing away.");
            resetData();
            return;
        }
        dataMap.put("shellyht_temperature_" + shellyMap.get(this.shellyHtId), new AbstractMap.SimpleEntry<String, String>(temperature.toString(), "°C"));
        dataMap.put("shellyht_humidity_"  + shellyMap.get(this.shellyHtId), new AbstractMap.SimpleEntry<String, String>(humidity.toString(), "%"));
        eventEntryManagerService.raiseEvent("ShellyHT: Shelly HT device with id \" + this.shellyHtId + \" reported values: temperature - " + temperature + "; humidity - " + humidity, EventEntry.EventType.INFO);
        this.SaveInfoData(dataMap);
        resetData();
    }

    public void resetData() {
        this.temperature = null;
        this.humidity = null;
        this.shellyHtId = null;
    }
    public void ReceiveGetParameters(Map<String, String[]> parameterMap) {
        for (Map.Entry mapElement : parameterMap.entrySet()) {
            String getKey = (String)mapElement.getKey();
            if (getKey.equals("id")) {
                String[] values = (String[])mapElement.getValue();
                // Shelly HT implementation for temperature and humidity values
                if (values[0].startsWith("shellyht")) {
                    this.setTemperature(parameterMap.get("temp")[0]);
                    this.setHumidity(parameterMap.get("hum")[0]);
                    this.setShellyHtId(values[0].toLowerCase());
                    this.CollectData();
                    break;
                }
            }
        }
    }
}
