package cz.snyll.Sunny.controllers;

import cz.snyll.Sunny.domain.Device;
import cz.snyll.Sunny.services.DeviceManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;

@Controller
public class DeviceListController {
    private DeviceManagerService deviceManagerService;

    @Autowired
    public DeviceListController(DeviceManagerService deviceManagerService) {
        this.deviceManagerService = deviceManagerService;
    }

    @GetMapping("/devicelist")
    public String deviceList(Model model, @RequestParam(required = false) boolean success, @RequestParam(required = false) boolean deletesuccess) {
        ArrayList<Device> deviceDataSet = deviceManagerService.LoadAll();
        model.addAttribute("success", success);
        model.addAttribute("deletesuccess", deletesuccess);
        model.addAttribute("devices", deviceDataSet);
        return "devicelist";
    }
}