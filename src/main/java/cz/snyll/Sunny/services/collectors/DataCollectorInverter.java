package cz.snyll.Sunny.services.collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.snyll.Sunny.config.InverterMappingConfiguration;
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
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DataCollectorInverter extends DataCollectorAbstractService {
    private String localPassword;
    private String localUrl;
    @Autowired
    private EventEntryManagerService eventEntryManagerService;
    @Autowired
    private MainConfiguration mainConfiguration;
    @Autowired
    private InverterMappingConfiguration inverterMappingConfiguration;
    private InfoDataRepository infoDataRepository;

    @Autowired
    public DataCollectorInverter(InfoDataRepository infoDataRepository) {
        super(infoDataRepository);
    }

    @Autowired
    private RestConfig restConfig;
    @Autowired
    private DataCollectorSolaxCloud dataCollectorSolaxCloud;
    private HashMap<String, Integer> inverterMap;

    @Scheduled(fixedDelay = 5000)
    @Override
    public void CollectData() {
        // if inverter data collection is turned off, ignore this job
        if (this.mainConfiguration.isInverter_collecting() == false)
            return;
        this.inverterMap = this.inverterMappingConfiguration.getInverterMap();
        this.localUrl = this.mainConfiguration.getSolaxLocalUrl();
        this.localPassword = this.mainConfiguration.getSolaxLocalPassword();
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
                ProcessBuilder processBuilder = new ProcessBuilder("curl", "-d", "optType=ReadRealTimeData&pwd=" + this.localPassword, "-X", "POST", this.localUrl, "--max-time", "20", "2>&1");
                Process process = processBuilder.start();
                process.waitFor(5000l, TimeUnit.MILLISECONDS);
                String result = new String(process.getInputStream().readAllBytes());

                jsonResponse = mapper.readTree(result).get("Data");
                //String result = "{\"sn\":\"SWC8WLUQYD\",\"ver\":\"2.034.03\",\"type\":14,\"Data\":[2413,2424,2422,10,10,13,55,118,243,416,2089,1957,19,0,404,0,5000,5000,5000,2,0,0,0,0,0,0,0,0,0,0,0,0,0,1,12,0,0,0,0,22640,0,0,2253,0,0,1,47,404,256,4608,5396,5638,100,0,47,0,0,0,0,0,0,0,0,0,0,0,0,0,1255,0,81,0,0,0,345,0,471,0,10,23,1380,0,93,0,0,0,350,0,9247,0,32,0,9247,0,0,0,0,0,0,0,0,0,1,99,1,26,89,256,2352,1568,0,350,231,212,40,40,5,1107,513,8481,8481,0,0,0,0,4036,4020,25184,1,21302,19778,18515,12598,16693,12354,13369,21302,13620,12594,14640,14640,12354,13104,21302,13620,12594,14640,14640,12354,12848,0,0,0,0,0,0,0,0,0,0,0,0,0,0,257,257,769,1025,0,22640,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],               \"Information\":[8.000,14,\"H34T08HB316116\",1,1.10,0.00,1.09,1.08,0.00,1]}";
                //String result =   "{\"sn\":\"SRDGN4LPLK\",\"ver\":\"3.006.04\",\"type\":16,\"Data\":[2393,2404,2378,23,19,20,604,465,447,4968,7869,0,17,9,0,890,725,0,4982,4995,4985,2,555,0,145,20000,30,33,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,63428,65535,1347,0,16140,0,1519,0,1,4,7,0,11248,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],\"Information\":[20.000,16,\"MPT20TIA703024\",8,1.18,0.00,1.15,1.01,0.00,1]}";
                jsonResponse = mapper.readTree(result).get("Data");
            } catch (Exception e) {
                e.printStackTrace();
            }

            int i = 0;
            if (jsonResponse == null) {
                log.debug("SOLAX: Solax inverter data not loaded - inverter not reachable or not returning response.");
            } else {
                try {

                    if (jsonResponse.isArray()) {
                        for (final JsonNode inverterValue : jsonResponse) {
                            // we must use the weird solax inverter sensor mapping with specific indexes
                            try {
                                float realValue;
                                float value = Float.parseFloat(inverterValue.toString());

                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_network_voltage_phase_1"), -1))
                                    dataMap.put("solax_network_voltage_phase_1", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div10(value)), "V"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_network_voltage_phase_2"), -1))
                                    dataMap.put("solax_network_voltage_phase_2", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div10(value)), "V"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_network_voltage_phase_3"), -1))
                                    dataMap.put("solax_network_voltage_phase_3", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div10(value)), "V"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_output_current_phase_1"), -1))
                                    dataMap.put("solax_output_current_phase_1", new AbstractMap.SimpleEntry<String, String>(Float.toString(TwoWayDiv10(value)), "A"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_output_current_phase_2"), -1))
                                    dataMap.put("solax_output_current_phase_2", new AbstractMap.SimpleEntry<String, String>(Float.toString(TwoWayDiv10(value)), "A"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_output_current_phase_3"), -1))
                                    dataMap.put("solax_output_current_phase_3", new AbstractMap.SimpleEntry<String, String>(Float.toString(TwoWayDiv10(value)), "A"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_power_now_phase_1"), -1))
                                    dataMap.put("solax_power_now_phase_1", new AbstractMap.SimpleEntry<String, String>(Float.toString(ToSigned(value)), "W"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_power_now_phase_2"), -1))
                                    dataMap.put("solax_power_now_phase_2", new AbstractMap.SimpleEntry<String, String>(Float.toString(ToSigned(value)), "W"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_power_now_phase_3"), -1))
                                    dataMap.put("solax_power_now_phase_3", new AbstractMap.SimpleEntry<String, String>(Float.toString(ToSigned(value)), "W"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_acpower"), -1))
                                    dataMap.put("solax_acpower", new AbstractMap.SimpleEntry<String, String>(Float.toString(ToSigned(value)), "W"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_panels_voltage_1"), -1)) {
                                    dataMap.put("solax_panels_voltage_1", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div10(value)), "V"));
                                    System.out.println("PANELS VOLTAGE 1: " + Objects.requireNonNullElse(this.inverterMap.get("solax_panels_voltage_1"), -1) + ", " + dataMap.get("solax_panels_voltage_1"));
                                }
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_panels_voltage_2"), -1))
                                    dataMap.put("solax_panels_voltage_2", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div10(value)), "V"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_panels_voltage_3"), -1))
                                    dataMap.put("solax_panels_voltage_3", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div10(value)), "V"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_panels_current_1"), -1))
                                    dataMap.put("solax_panels_current_1", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div10(value)), "A"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_panels_current_2"), -1))
                                    dataMap.put("solax_panels_current_2", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div10(value)), "A"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_panels_current_3"), -1))
                                    dataMap.put("solax_panels_current_3", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div10(value)), "A"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_solar_panels_power_1"), -1))
                                    dataMap.put("solax_solar_panels_power_1", new AbstractMap.SimpleEntry<String, String>(inverterValue.toString(), "W"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_solar_panels_power_2"), -1))
                                    dataMap.put("solax_solar_panels_power_2", new AbstractMap.SimpleEntry<String, String>(inverterValue.toString(), "W"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_solar_panels_power_3"), -1))
                                    dataMap.put("solax_solar_panels_power_3", new AbstractMap.SimpleEntry<String, String>(inverterValue.toString(), "W"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_grid_frequency_phase_1"), -1))
                                    dataMap.put("solax_grid_frequency_phase_1", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div100(value)), "Hz"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_grid_frequency_phase_2"), -1))
                                    dataMap.put("solax_grid_frequency_phase_2", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div100(value)), "Hz"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_grid_frequency_phase_3"), -1))
                                    dataMap.put("solax_grid_frequency_phase_3", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div100(value)), "Hz"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_exported_power"), -1))
                                    dataMap.put("solax_exported_power", new AbstractMap.SimpleEntry<String, String>(Float.toString(ToSigned(value)), "W"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_battery_voltage"), -1))
                                    dataMap.put("solax_battery_voltage", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div100(value)), "V"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_battery_current"), -1))
                                    dataMap.put("solax_battery_current", new AbstractMap.SimpleEntry<String, String>(Float.toString(TwoWayDiv100(value)), "A"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_battery_power"), -1))
                                    dataMap.put("solax_battery_power", new AbstractMap.SimpleEntry<String, String>(Float.toString(ToSigned(value)), "W"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_power_consumption_now"), -1))
                                    dataMap.put("solax_power_consumption_now", new AbstractMap.SimpleEntry<String, String>(Float.toString(ToSigned(value)), "W"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_total_energy"), -1))
                                    dataMap.put("solax_total_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(value), "kWh"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_total_energy_reset_counter"), -1)) {
                                    realValue = ResettingCounter(Float.parseFloat(dataMap.get("solax_total_energy").getKey()), value, true);
                                    dataMap.put("solax_total_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(realValue), "kWh"));
                                }
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_today_yield"), -1))
                                    dataMap.put("solax_today_yield", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div10(value)), "kWh"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_total_battery_discharge_energy"), -1))
                                    dataMap.put("solax_total_battery_discharge_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(value), "kWh"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_total_battery_discharge_energy_reset_counter"), -1)) {
                                    realValue = ResettingCounter(Float.parseFloat(dataMap.get("solax_total_battery_discharge_energy").getKey()), value, true);
                                    dataMap.put("solax_total_battery_discharge_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(realValue), "kWh"));
                                }
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_total_battery_charge_energy"), -1))
                                    dataMap.put("solax_total_battery_charge_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(value), "kWh"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_total_battery_charge_energy_reset_counter"), -1)) {
                                    realValue = ResettingCounter(Float.parseFloat(dataMap.get("solax_total_battery_charge_energy").getKey()), value, true);
                                    dataMap.put("solax_total_battery_charge_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(realValue), "kWh"));
                                }
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_today_battery_discharge_energy"), -1))
                                    dataMap.put("solax_today_battery_discharge_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div10(value)), "kWh"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_today_battery_charge_energy"), -1))
                                    dataMap.put("solax_today_battery_charge_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div10(value)), "kWh"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_total_panels_energy"), -1))
                                    dataMap.put("solax_total_panels_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(value), "kWh"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_total_panels_energy_reset_counter"), -1)) {
                                    realValue = ResettingCounter(Float.parseFloat(dataMap.get("solax_total_panels_energy").getKey()), value, true);
                                    dataMap.put("solax_total_panels_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(realValue), "kWh"));
                                }
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_today_energy"), -1))
                                    dataMap.put("solax_today_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div10(value)), "kWh"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_total_feedin_energy"), -1))
                                    dataMap.put("solax_total_feedin_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(value), "kWh"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_total_feedin_energy_reset_counter"), -1)) {
                                    realValue = ResettingCounter(Float.parseFloat(dataMap.get("solax_total_feedin_energy").getKey()), value, false);
                                    dataMap.put("solax_total_feedin_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(realValue), "kWh"));
                                }
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_total_consumption"), -1))
                                    dataMap.put("solax_total_consumption", new AbstractMap.SimpleEntry<String, String>(Float.toString(value), "kWh"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_total_consumption_reset_counter"), -1)) {
                                    realValue = ResettingCounter(Float.parseFloat(dataMap.get("solax_total_consumption").getKey()), value, false);
                                    dataMap.put("solax_total_consumption", new AbstractMap.SimpleEntry<String, String>(Float.toString(realValue), "kWh"));
                                }
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_today_feedin_energy"), -1)) {
                                    dataMap.put("solax_today_feedin_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div100(value)), "kWh"));
                                    float solaxTodaySelfConsumedYield = Float.parseFloat(dataMap.get("solax_today_yield").getKey()) - Float.parseFloat(dataMap.get("solax_today_feedin_energy").getKey());
                                    dataMap.put("solax_today_self_consumed_yield", new AbstractMap.SimpleEntry<String, String>(Float.toString(solaxTodaySelfConsumedYield), "kWh"));
                                }
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_today_consumption"), -1))
                                    dataMap.put("solax_today_consumption", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div100(value)), "kWh"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_battery_remaining_capacity"), -1))
                                    dataMap.put("solax_battery_remaining_capacity", new AbstractMap.SimpleEntry<String, String>(Float.toString(value), "%"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_battery_temperature"), -1))
                                    dataMap.put("solax_battery_temperature", new AbstractMap.SimpleEntry<String, String>(Float.toString(value), "°C"));
                                if (i == Objects.requireNonNullElse(this.inverterMap.get("solax_battery_remaining_energy"), -1))
                                    dataMap.put("solax_battery_remaining_energy", new AbstractMap.SimpleEntry<String, String>(Float.toString(Div10(value)), "kWh"));
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
                    dataMap.put("solax_solar_panels_power_total", new AbstractMap.SimpleEntry<String, String>(Integer.toString(Integer.parseInt(dataMap.get("solax_solar_panels_power_1").getKey()) + Integer.parseInt(dataMap.get("solax_solar_panels_power_2").getKey())), "W"));
                    if (this.inverterMap.get("solax_power_consumption_now") == null) {
                        float consumptionCounted = ToSigned(Float.parseFloat(dataMap.get("solax_acpower").getKey())) - ToSigned(Float.parseFloat(dataMap.get("solax_exported_power").getKey()));
                        dataMap.put("solax_power_consumption_now", new AbstractMap.SimpleEntry<String, String>(Float.toString(consumptionCounted), "W"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.debug("SOLAX: Solax inverter data not loaded because of an exception: " + e.getMessage());
                    eventEntryManagerService.raiseEvent("SOLAX: Solax inverter data not loaded because of an exception: " + e.getMessage(), EventEntry.EventType.ERROR, 60);
                }
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
        return val / 10f;
    }

    public float Div100(float val) {
        return val / 100f;
    }

    public float TwoWayDiv10(float val) {
        val = ToSigned(val);
        return val / 10f;
    }

    public float TwoWayDiv100(float val) {
        val = ToSigned(val);
        return val / 100f;
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
