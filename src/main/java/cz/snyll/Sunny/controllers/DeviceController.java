package cz.snyll.Sunny.controllers;

import cz.snyll.Sunny.domain.Device;
import cz.snyll.Sunny.repositories.DeviceRepository;
import cz.snyll.Sunny.repositories.TriggerRepository;
import cz.snyll.Sunny.services.DeviceManagerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class DeviceController {
    @Autowired
    private DeviceManagerService deviceManagerService;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private TriggerRepository triggerRepository;
    @GetMapping("/device")
    public String device(Model model, @RequestParam(required = false) Long id) {
        if (id == null)
            id = 0l;
        Device device = deviceRepository.findById(id).orElseGet(() -> new Device());
        model.addAttribute("device", device);
        return "device";
    }

    @GetMapping("/device/edit")
    public String deviceEdit(Model model, @RequestParam(required = false) Long id, @RequestParam(required = false) boolean delete) {
        if (delete == true  && id > 0) {
            deviceManagerService.DeleteDeviceById(id);
            return "redirect:../devicelist?deletesuccess=true";
        } else {
            return "redirect:../devicelist?deletesuccess=false";
        }
    }
    @PostMapping("/device")
    public String deviceSubmit(@Valid @ModelAttribute Device device, BindingResult result, Model model) {
        model.addAttribute("device", device);
        if (result.hasErrors()) {
            return "device";
        } else {
            deviceManagerService.SaveDevice(device);
            model.addAttribute("success", true);
            return "redirect:/devicelist?success=true";
        }
    }

}