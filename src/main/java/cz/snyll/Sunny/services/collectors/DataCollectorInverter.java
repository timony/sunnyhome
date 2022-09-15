package cz.snyll.Sunny.services.collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.snyll.Sunny.config.MainConfiguration;
import cz.snyll.Sunny.config.RestConfig;
import cz.snyll.Sunny.domain.EventEntry;
import cz.snyll.Sunny.domain.InfoData;
import cz.snyll.Sunny.repositories.InfoDataRepository;
import cz.snyll.Sunny.services.EventEntryManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.AbstractMap;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DataCollectorInverter extends DataCollectorAbstractService {
    private String apiUrl;
    @Autowired
    private EventEntryManagerService eventEntryManagerService;
    @Autowired
    private MainConfiguration mainConfiguration;
    private InfoDataRepository infoDataRepository;
    @Autowired
    public DataCollectorInverter(InfoDataRepository infoDataRepository) {
        super(infoDataRepository);
    }
    @Autowired
    private RestConfig restConfig;
    @Autowired
    private DataCollectorSolaxCloud dataCollectorSolaxCloud;

    @Scheduled(fixedDelay = 5000)
    @Override
    public void CollectData() {
        // if inverter data collection is turned off, ignore this job
        if (this.mainConfiguration.isInverter_collecting() == false)
            return;

        this.apiUrl = this.mainConfiguration.getSolaxLocalUrl();
        HashMap<String, Entry<String, String>> dataMap = new HashMap<>();

// with new version of Solax wifi dongle firmware, calls using java restTemplate are failing, lets try curl
//        RestTemplate restTemplate = restConfig.restTemplate();

//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.add("User-Agent", "curl/7.79.1");

//        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiUrl)
//                .queryParam("optType", "ReadRealTimeData")
//                .queryParam("pwd", "admin");
//
////        HttpEntity<?> entity = new HttpEntity<>(headers);
//
//        try {
//            HttpEntity<String> response = restTemplate.exchange(
//                    builder.toUriString(),
//                    HttpMethod.POST,
//                    null,
//                    String.class);
        try {
            JsonNode jsonResponse = null;
            ObjectMapper mapper = new ObjectMapper();
            try {
                //String command = "bash -c 'curl -d \"optType=ReadRealTimeData&pwd=admin\" -X POST http://5.8.8.8 --max-time 10 2>&1'";
                ProcessBuilder processBuilder = new ProcessBuilder("curl", "-d", "optType=ReadRealTimeData&pwd=admin", "-X", "POST", "http://5.8.8.8", "--max-time", "10", "2>&1");
                Process process = processBuilder.start();
                process.waitFor(15000l, TimeUnit.MILLISECONDS);
                String result = new String(process.getInputStream().readAllBytes());
                System.out.println("curl result: " + result);
                jsonResponse = mapper.readTree(result).get("Data");
            } catch (Exception e) {
                e.printStackTrace();
            }

            int i = 0;
            try {
                if (jsonResponse.isArray()) {
                    for (final JsonNode inverterValue : jsonResponse) {
                        // we must use the weird solax inverter sensor mapping with specific indexes
                        try {
                            float realValue;
                            float value = Float.parseFloat(inverterValue.toString());
                            switch (i) {
                                case 0:
                                    dataMap.put("solax_network_voltage_phase_1", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div10(value)), "V"));
                                    break;
                                case 1:
                                    dataMap.put("solax_network_voltage_phase_2", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div10(value)), "V"));
                                    break;
                                case 2:
                                    dataMap.put("solax_network_voltage_phase_3", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div10(value)), "V"));
                                    break;
                                case 3:
                                    dataMap.put("solax_output_current_phase_1", new AbstractMap.SimpleEntry<String, String>(Float.toString(TwoWayDiv10(value)), "A"));
                                    break;
                                case 4:
                                    dataMap.put("solax_output_current_phase_2", new AbstractMap.SimpleEntry<String, String>(Float.toString(TwoWayDiv10(value)), "A"));
                                    break;
                                case 5:
                                    dataMap.put("solax_output_current_phase_3", new AbstractMap.SimpleEntry<String, String>(Float.toString(TwoWayDiv10(value)), "A"));
                                    break;
                                case 6:
                                    dataMap.put("solax_power_now_phase_1", new AbstractMap.SimpleEntry<String, String>(Float.toString(ToSigned(value)), "W"));
                                    break;
                                case 7:
                                    dataMap.put("solax_power_now_phase_2", new AbstractMap.SimpleEntry<String, String>(Float.toString(ToSigned(value)), "W"));
                                    break;
                                case 8:
                                    dataMap.put("solax_power_now_phase_3", new AbstractMap.SimpleEntry<String, String>(Float.toString(ToSigned(value)), "W"));
                                    break;
                                case 9:
                                    dataMap.put("solax_acpower", new AbstractMap.SimpleEntry<String, String>(Float.toString(ToSigned(value)), "W"));
                                    break;
                                case 10:
                                    dataMap.put("solax_panels_voltage_1", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div10(value)), "V"));
                                    break;
                                case 11:
                                    dataMap.put("solax_panels_voltage_2", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div10(value)), "V"));
                                    break;
                                case 12:
                                    dataMap.put("solax_panels_current_1", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div10(value)), "A"));
                                    break;
                                case 13:
                                    dataMap.put("solax_panels_current_2", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div10(value)), "A"));
                                    break;
                                case 14:
                                    dataMap.put("solax_solar_panels_power_1", new AbstractMap.SimpleEntry<String, String>(inverterValue.toString(), "W"));
                                    break;
                                case 15:
                                    dataMap.put("solax_solar_panels_power_2", new AbstractMap.SimpleEntry<String, String>(inverterValue.toString(), "W"));
                                    break;
                                case 16:
                                    dataMap.put("solax_grid_frequency_phase_1", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div100(value)), "Hz"));
                                    break;
                                case 17:
                                    dataMap.put("solax_grid_frequency_phase_2", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div100(value)), "Hz"));
                                    break;
                                case 18:
                                    dataMap.put("solax_grid_frequency_phase_3", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div100(value)), "Hz"));
                                    break;
                                case 34:
                                    dataMap.put("solax_exported_power", new AbstractMap.SimpleEntry<String, String>(Float.toString(ToSigned(value)), "W"));
                                    break;
                                case 39:
                                    dataMap.put("solax_battery_voltage", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div100(value)), "V"));
                                    break;
                                case 40:
                                    dataMap.put("solax_battery_current", new AbstractMap.SimpleEntry<String, String>(Float.toString(TwoWayDiv100(value)), "A"));
                                    break;
                                case 41:
                                    dataMap.put("solax_battery_power", new AbstractMap.SimpleEntry<String, String>(Float.toString(ToSigned(value)), "W"));
                                    break;
                                case 47:
                                    dataMap.put("solax_power_consumption_now", new AbstractMap.SimpleEntry<String, String>(Float.toString(ToSigned(value)), "W"));
                                    break;
                                case 68:
                                    dataMap.put("solax_total_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(value), "kWh"));
                                    break;
                                case 69:
                                    realValue = ResettingCounter(Float.parseFloat(dataMap.get("solax_total_energy").getKey()), value, true);
                                    dataMap.put("solax_total_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(realValue), "kWh"));
                                    break;
                                case 70:
                                    dataMap.put("solax_today_yield", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div10(value)), "kWh"));
                                    break;
                                case 74:
                                    dataMap.put("solax_total_battery_discharge_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(value), "kWh"));
                                    break;
                                case 75:
                                    realValue = ResettingCounter(Float.parseFloat(dataMap.get("solax_total_battery_discharge_energy").getKey()), value, true);
                                    dataMap.put("solax_total_battery_discharge_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(realValue), "kWh"));
                                    break;
                                case 76:
                                    dataMap.put("solax_total_battery_charge_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(value), "kWh"));
                                    break;
                                case 77:
                                    realValue = ResettingCounter(Float.parseFloat(dataMap.get("solax_total_battery_charge_energy").getKey()), value, true);
                                    dataMap.put("solax_total_battery_charge_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(realValue), "kWh"));
                                    break;
                                case 78:
                                    dataMap.put("solax_today_battery_discharge_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div10(value)), "kWh"));
                                    break;
                                case 79:
                                    dataMap.put("solax_today_battery_charge_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div10(value)), "kWh"));
                                    break;
                                case 80:
                                    dataMap.put("solax_total_panels_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(value), "kWh"));
                                    break;
                                case 81:
                                    realValue = ResettingCounter(Float.parseFloat(dataMap.get("solax_total_panels_energy").getKey()), value, true);
                                    dataMap.put("solax_total_panels_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(realValue), "kWh"));
                                    break;
                                case 82:
                                    dataMap.put("solax_today_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div10(value)), "kWh"));
                                    break;
                                case 86:
                                    dataMap.put("solax_total_feedin_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(value), "kWh"));
                                    break;
                                case 87:
                                    realValue = ResettingCounter(Float.parseFloat(dataMap.get("solax_total_feedin_energy").getKey()), value, false);
                                    dataMap.put("solax_total_feedin_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(realValue), "kWh"));
                                    break;
                                case 88:
                                    dataMap.put("solax_total_consumption", new AbstractMap.SimpleEntry<String, String>(Float.toString(value), "kWh"));
                                    break;
                                case 89:
                                    realValue = ResettingCounter(Float.parseFloat(dataMap.get("solax_total_consumption").getKey()), value, false);
                                    dataMap.put("solax_total_consumption", new AbstractMap.SimpleEntry<String, String>(Float.toString(realValue), "kWh"));
                                    break;
                                case 90:
                                    dataMap.put("solax_today_feedin_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div100(value)), "kWh"));
                                    float solaxTodaySelfConsumedYield = Float.parseFloat(dataMap.get("solax_today_yield").getKey()) - Float.parseFloat(dataMap.get("solax_today_feedin_energy").getKey());
                                    dataMap.put("solax_today_self_consumed_yield", new AbstractMap.SimpleEntry<String, String>(Float.toString(solaxTodaySelfConsumedYield), "kWh"));
                                    break;
                                case 92:
                                    dataMap.put("solax_today_consumption", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div100(value)), "kWh"));
                                    break;
                                case 103:
                                    dataMap.put("solax_battery_remaining_capacity", new AbstractMap.SimpleEntry<String, String>(Float.toString(value), "%"));
                                    break;
                                case 105:
                                    dataMap.put("solax_battery_temperature", new AbstractMap.SimpleEntry<String, String>(Float.toString(value), "°C"));
                                    break;
                                case 106:
                                    dataMap.put("solax_battery_remaining_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div10(value)), "kWh"));
                                    break;
                            }
                        } catch (NumberFormatException e) {
                            log.debug("COLLECTOR: Solax inverter provided wrong JSON response, array does not have integer values.");
                            eventEntryManagerService.raiseEvent("SOLAX: Solax inverter provided wrong JSON response, array does not have integer values.", EventEntry.EventType.ERROR, 60);
                        }
                        i++;
                    }
                }
                float exported_power = 0f;
                if (dataMap.containsKey("solax_exported_power"))
                    exported_power = Float.parseFloat(dataMap.get("solax_exported_power").getKey());
                if (exported_power < 0)
                    dataMap.put("solax_grid_consumption", new AbstractMap.SimpleEntry<>(Integer.toString(Math.round(Math.abs(exported_power))), "W"));
                dataMap.put("solax_solar_panels_power_total", new AbstractMap.SimpleEntry<String, String>(Integer.toString(Integer.parseInt(dataMap.get("solax_solar_panels_power_1").getKey()) +  Integer.parseInt(dataMap.get("solax_solar_panels_power_2").getKey())), "W"));
            } catch (Exception e) {
                log.debug("SOLAX: Solax inverter data not loaded because of an exception: " + e.getMessage());
                eventEntryManagerService.raiseEvent("SOLAX: Solax inverter data not loaded because of an exception: " + e.getMessage(), EventEntry.EventType.ERROR, 60);
            }
        } catch (RestClientException e) {
            eventEntryManagerService.raiseEvent("SOLAX: Solax inverter request threw en exception: " + e.getMessage(), EventEntry.EventType.ERROR, 60);
        }

        // save the data
        this.SaveInfoData(dataMap);
        try {
            Date minus5minutes = new Date(System.currentTimeMillis() - 300 * 1000);
            InfoData infoDataCloud = super.getInfoDataRepository().findByDataKey("solax_solar_panels_power_1");
            if (infoDataCloud != null) {
                Date dataFreshness = infoDataCloud.getDataFreshness();
                if (dataFreshness.before(minus5minutes)) {
                    dataCollectorSolaxCloud.CollectData();
                }
            } else {
                dataCollectorSolaxCloud.CollectData();
            }

        } catch (Exception e) {
            //e.printStackTrace();
            eventEntryManagerService.raiseEvent("SOLAX: Issue with starting backup Solax cloud data gathering. Exception: " + e.getMessage(), EventEntry.EventType.ERROR, 60);
        }
    }
    public float ToSigned(float val) {
        if (val > 32767)
            return (val - 65535);
        return val;
    }

    public float Div10(float val) {
        return val/10f;
    }

    public float Div100(float val) {
        return val/100f;
    }

    public float TwoWayDiv10(float val) {
        val = ToSigned(val);
        return val/10f;
    }
    public float TwoWayDiv100(float val) {
        val = ToSigned(val);
        return val/100f;
    }
    public float ResettingCounter(float val, float resets, boolean div) {
        val += (resets * 65535);
        if (div == true) {
            return Div10(val);
        } else {
            return Div100(val);
        }
    }

}
