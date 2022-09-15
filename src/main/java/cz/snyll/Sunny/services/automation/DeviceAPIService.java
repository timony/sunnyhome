package cz.snyll.Sunny.services.automation;

import cz.snyll.Sunny.domain.Device;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DeviceAPIService {
    private final RestTemplate restTemplate;
    public DeviceAPIService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }
    public boolean TurnOnDevice(Device device) {
        String url = device.getTurnOnApiUrl();
        String response = this.restTemplate.getForObject(url, String.class);
        System.out.println("Device turning on: " + device.getDeviceName() + "; " + response);
        APIResponseProcessor apiResponseProcessor = ResponseProcessorFactory.createResponseProcessor(device.getControlDeviceName());
        if (apiResponseProcessor == null)
            return false;
        return apiResponseProcessor.ProcessResponse(response, true);
    }
    public boolean TurnOffDevice(Device device) {
        String url = device.getTurnOffApiUrl();
        String response = this.restTemplate.getForObject(url, String.class);
        System.out.println("Device turning off: " + device.getDeviceName() + "; " + response);
        APIResponseProcessor apiResponseProcessor = ResponseProcessorFactory.createResponseProcessor(device.getControlDeviceName());
        if (apiResponseProcessor == null)
            return false;
        return apiResponseProcessor.ProcessResponse(response, false);
    }
}
