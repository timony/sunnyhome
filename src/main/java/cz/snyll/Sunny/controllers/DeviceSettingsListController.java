package cz.snyll.Sunny.controllers;

import cz.snyll.Sunny.domain.DeviceSetting;
import cz.snyll.Sunny.services.DeviceSettingManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;

@Controller
public class DeviceSettingsListController {
    private DeviceSettingManagerService deviceSettingManagerService;

    @Autowired
    public DeviceSettingsListController(DeviceSettingManagerService deviceSettingManagerService) {
        this.deviceSettingManagerService = deviceSettingManagerService;
    }

    @GetMapping("/devicesettingslist")
    public String deviceSettingsList(Model model, @RequestParam(required = false) boolean success, @RequestParam(required = false) boolean deletesuccess) {
        ArrayList<DeviceSetting> deviceSettingDataSet = deviceSettingManagerService.LoadAll();
        model.addAttribute("success", success);
        model.addAttribute("deletesuccess", deletesuccess);
        model.addAttribute("devicesettings", deviceSettingDataSet);
        return "devicesettingslist";
    }
}