package cz.snyll.Sunny.services.collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.snyll.Sunny.domain.Device;
import cz.snyll.Sunny.domain.EventEntry;
import cz.snyll.Sunny.services.EventEntryManagerService;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

public class ShellyPlugSInfoCollector implements DeviceInfoCollector {
    private final RestTemplate restTemplate;
    private JsonNode statusJSON;
    private Device device;

    private EventEntryManagerService eventEntryManagerService;

    public ShellyPlugSInfoCollector(Device device, EventEntryManagerService eventEntryManagerService) {
        this.device = device;
        this.eventEntryManagerService = eventEntryManagerService;
        this.restTemplate = new RestTemplateBuilder().setConnectTimeout(Duration.ofMillis(5000)).setReadTimeout(Duration.ofMillis(5000)).build();
        try {
            String url = "http://" + device.getDeviceIP() + "/status";
            String response = this.restTemplate.getForObject(url, String.class);
            ObjectMapper mapper = new ObjectMapper();
            this.statusJSON = mapper.readTree(response);
        } catch (Exception e) {
            eventEntryManagerService.raiseEvent("DEVICE INFO: " + device.getDeviceName() + " - Could not retrieve API data. Check IP address of this device.", EventEntry.EventType.WARNING, 10);
        }
    }

    @Override
    public float getCurrentConsumption() {
        try {
            float consumption = Float.parseFloat(this.statusJSON.get("meters").get(0).get("power").toString());
            return consumption;
        } catch (Exception e) {
            eventEntryManagerService.raiseEvent("DEVICE INFO: " + device.getDeviceName() + " -Could not parse consumption.", EventEntry.EventType.WARNING, 10);
            return -1;
        }
    }

    @Override
    public float getTotalConsumption() {
        try {
            float consumption = Float.parseFloat(this.statusJSON.get("meters").get(0).get("total").toString()) * 0.000017f;
            return consumption;
        } catch (Exception e) {
            eventEntryManagerService.raiseEvent("DEVICE INFO: " + device.getDeviceName() + " - Could not parse total consumption.", EventEntry.EventType.WARNING, 10);
            return -1;
        }
    }

    @Override
    public float getTodayConsumption() {
        try {
            float consumption = Float.parseFloat(this.statusJSON.get("meters").get(0).get("total").toString()) * 0.000017f;
            return consumption - device.getDeviceStatus().getLastDayTotalConsumption();
        } catch (Exception e) {
            eventEntryManagerService.raiseEvent("DEVICE INFO: " + device.getDeviceName() + " - Could not parse total consumption.", EventEntry.EventType.WARNING, 10);
            return -1;
        }
    }
    @Override
    public boolean getActualStatus() {
        try {
            boolean isOn = this.statusJSON.get("relays").get(0).get("ison").asBoolean();
            return isOn;
        } catch (Exception e) {
            eventEntryManagerService.raiseEvent("DEVICE INFO: " + device.getDeviceName() + " - Could not parse actual status.", EventEntry.EventType.WARNING, 10);
            return false;
        }
    }

}
