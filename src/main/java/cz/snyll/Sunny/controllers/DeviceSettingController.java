package cz.snyll.Sunny.controllers;

import cz.snyll.Sunny.domain.Device;
import cz.snyll.Sunny.domain.DeviceSetting;
import cz.snyll.Sunny.domain.Trigger;
import cz.snyll.Sunny.repositories.DeviceRepository;
import cz.snyll.Sunny.repositories.DeviceSettingRepository;
import cz.snyll.Sunny.repositories.TriggerRepository;
import cz.snyll.Sunny.services.DeviceSettingManagerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;


@Controller
public class DeviceSettingController {
    @Autowired
    private DeviceSettingManagerService deviceSettingManagerService;
    @Autowired
    private DeviceSettingRepository deviceSettingRepository;
    @Autowired
    private TriggerRepository triggerRepository;
    @Autowired
    private DeviceRepository deviceRepository;

    @GetMapping("/devicesetting")
    public String deviceSetting(Model model, @RequestParam(required = false) Long id) {
        if (id == null)
            id = 0l;
        DeviceSetting deviceSetting = deviceSettingRepository.findById(id).orElseGet(() -> new DeviceSetting());
        ArrayList<Trigger> triggers = (ArrayList<Trigger>) triggerRepository.findAll();
        ArrayList<Device> devices = (ArrayList<Device>) deviceRepository.findAll();
        model.addAttribute("deviceSetting", deviceSetting);
        model.addAttribute("triggers", triggers);
        model.addAttribute("devices", devices);
        return "devicesetting";
    }

    @GetMapping("/devicesetting/edit")
    public String deviceSettingEdit(Model model, @RequestParam(required = false) Long id, @RequestParam(required = false) boolean delete) {
        if (delete == true  && id > 0) {
            deviceSettingManagerService.DeleteDeviceSettingById(id);
            return "redirect:../devicesettingslist?deletesuccess=true";
        } else {
            return "redirect:../devicesettingslist?deletesuccess=false";
        }
    }

    @PostMapping("/devicesetting")
    public String deviceSubmit(@Valid @ModelAttribute DeviceSetting deviceSetting, BindingResult result, Model model) {
        try {
            model.addAttribute("deviceSetting", deviceSetting);
            // set the actual trigger from dummy triggers
            if (deviceSetting.getTriggers() != null) {
                HashSet<Trigger> newTriggers = new HashSet<>();
                for (Trigger idTrigger : deviceSetting.getTriggers()) {
                    Trigger newTrigger = triggerRepository.findById(idTrigger.getId()).get();
                    newTriggers.add(newTrigger);
                }
                deviceSetting.setTriggers(newTriggers);
            }
            // check if we have any device selected (required, if not selected go to error)
            if (deviceSetting.getDevice() != null) {
                Device device = deviceRepository.findById(deviceSetting.getDevice().getId()).get();
                deviceSetting.setDevice(device);
            } else {
                //throw new ServerErrorException("No device set for device setting.", null);
            }

            // get list of all triggers for the checkboxes
            ArrayList<Trigger> triggers = (ArrayList<Trigger>) triggerRepository.findAll();
            model.addAttribute("triggers", triggers);
            // get list of all devices for device selection
            ArrayList<Device> devices = (ArrayList<Device>) deviceRepository.findAll();
            model.addAttribute("devices", devices);

            // save the device setting

        } catch (Exception e) {
            e.printStackTrace();
            //throw new ServerErrorException("Some error during device setting submit.", null);
        }
        if (result.hasErrors()) {
            return "devicesetting";
        } else {
            deviceSettingManagerService.SaveDeviceSetting(deviceSetting);
            model.addAttribute("success", true);
            return "redirect:devicesettingslist?success=true";
        }

    }

}