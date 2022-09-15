package cz.snyll.Sunny.controllers;

import cz.snyll.Sunny.services.collectors.DataCollectorShellyHT;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class GetCollectorReceiverController {
    @Autowired
    private DataCollectorShellyHT dataCollectorShellyHTService;

    @GetMapping(value = {"/getcollector/", "/getcollector"})
    public void GetCollect(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        dataCollectorShellyHTService.ReceiveGetParameters(parameterMap);
    }
}
