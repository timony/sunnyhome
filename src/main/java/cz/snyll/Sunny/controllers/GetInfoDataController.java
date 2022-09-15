package cz.snyll.Sunny.controllers;

import cz.snyll.Sunny.domain.InfoData;
import cz.snyll.Sunny.repositories.InfoDataRepository;
import cz.snyll.Sunny.services.DeviceManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GetInfoDataController {
    private InfoDataRepository infoDataRepository;
    private DeviceManagerService deviceManagerService;

    @Autowired
    public GetInfoDataController(InfoDataRepository infoDataRepository) {
        this.infoDataRepository = infoDataRepository;
    }

    @GetMapping("/getinfodata")
    public List<InfoData> getinfodata() {
        return (List<InfoData>) infoDataRepository.findAll();
    }
}