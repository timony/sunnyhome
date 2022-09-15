package cz.snyll.Sunny.controllers;

import cz.snyll.Sunny.services.DeviceManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ToggleController {
        @Autowired
        private DeviceManagerService deviceManagerService;

        @GetMapping("/toggle")
        public ResponseEntity toggle(@RequestParam(required = true) Long id)
        {
            System.out.println("DEVICE id toggle " + id);
            deviceManagerService.manualToggle(deviceManagerService.LoadDevice(id));
            return new ResponseEntity(HttpStatus.OK);
        }
}
