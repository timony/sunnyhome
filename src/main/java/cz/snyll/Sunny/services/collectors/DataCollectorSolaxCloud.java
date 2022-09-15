package cz.snyll.Sunny.services.collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.snyll.Sunny.config.MainConfiguration;
import cz.snyll.Sunny.config.RestConfig;
import cz.snyll.Sunny.domain.EventEntry;
import cz.snyll.Sunny.repositories.InfoDataRepository;
import cz.snyll.Sunny.services.EventEntryManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

@Service
public class DataCollectorSolaxCloud extends DataCollectorAbstractService {
    @Autowired
    private MainConfiguration mainConfiguration;
    @Autowired
    public DataCollectorSolaxCloud(InfoDataRepository infoDataRepository) {
        super(infoDataRepository);
    }
    @Autowired
    private EventEntryManagerService eventEntryManagerService;
    @Autowired
    private RestConfig restConfig;
    @Override
    public void CollectData() {
        String token = mainConfiguration.getSolaxCloudApitoken();
        String apiUrl = mainConfiguration.getSolaxCloudApiUrl();
        String wifiSn = mainConfiguration.getSolaxWifiSn();
        HashMap<String, Map.Entry<String, String>> dataMap = new HashMap<>();
        RestTemplate restTemplate = restConfig.restTemplate();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiUrl+"getRealtimeInfo.do")
                .queryParam("tokenId", token).queryParam("sn", wifiSn);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);
        HttpEntity<String> response = null;
        try {
            response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    String.class);
            //System.out.println("RESPONSE FROM SOLAX CLOUD: \n" + response);
        } catch (HttpClientErrorException e) {
            System.out.println("Solax API cloud url is probably wrong. Could not load API data.");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode jsonResponse = mapper.readTree(response.getBody().toString()).get("result");
            dataMap.put("solax_solar_panels_power_1", new AbstractMap.SimpleEntry<String, String>(jsonResponse.get("powerdc1").toString(), "W"));
            dataMap.put("solax_solar_panels_power_2", new AbstractMap.SimpleEntry<String, String>(jsonResponse.get("powerdc2").toString(), "W"));
            dataMap.put("solax_battery_remaining_capacity", new AbstractMap.SimpleEntry<String, String>(jsonResponse.get("soc").toString(), "%"));
            dataMap.put("solax_today_self_consumed_yield", new AbstractMap.SimpleEntry<String, String>(jsonResponse.get("yieldtoday").toString(), "kWh"));
            dataMap.put("solax_total_consumption", new AbstractMap.SimpleEntry<String, String>(jsonResponse.get("consumeenergy").toString(), "kWh"));
            dataMap.put("solax_total_feedin_energy", new AbstractMap.SimpleEntry<String, String>(jsonResponse.get("feedinenergy").toString(), "kWh"));
            dataMap.put("solax_total_energy", new AbstractMap.SimpleEntry<String, String>(jsonResponse.get("yieldtotal").toString(), "kWh"));
            dataMap.put("solax_acpower", new AbstractMap.SimpleEntry<String, String>(jsonResponse.get("acpower").toString(), "W"));
            dataMap.put("solax_solar_panels_power_total", new AbstractMap.SimpleEntry<String, String>(Float.toString(Float.parseFloat(dataMap.get("solax_solar_panels_power_1").getKey()) +  Float.parseFloat(dataMap.get("solax_solar_panels_power_2").getKey())), "W"));
            dataMap.put("solax_battery_power", new AbstractMap.SimpleEntry<String, String>(jsonResponse.get("batPower").toString(), "W"));
            dataMap.put("solax_exported_power", new AbstractMap.SimpleEntry<String, String>(Float.toString(Math.negateExact((long)Float.parseFloat(jsonResponse.get("feedinpower").toString()))), "W"));
            Float acPower = Float.parseFloat(jsonResponse.get("acpower").toString());
            Float feedinPower = Float.parseFloat(jsonResponse.get("feedinpower").toString());
            Float currentConsumption = acPower - feedinPower;
            dataMap.put("solax_power_consumption_now", new AbstractMap.SimpleEntry<String, String>(Float.toString(Math.abs(currentConsumption)), "W"));

            //https://www.solaxcloud.com:9443/proxy/api/getRealtimeInfo.do?tokenId=202205280150091994872411&sn=SWC8WLUQYD
            this.SaveInfoData(dataMap);

        } catch (Exception e) {
            eventEntryManagerService.raiseEvent("SOLAX CLOUD: Issue loading Solax Cloud data. Error: " + e.getMessage(), EventEntry.EventType.ERROR, 60);
            return;
        }
    }
}
