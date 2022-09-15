package cz.snyll.Sunny.controllers;

import cz.snyll.Sunny.domain.InfoData;
import cz.snyll.Sunny.repositories.InfoDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;

@Controller
public class InfoDataController {

    @Autowired
    private InfoDataRepository infoDataRepository;

    @GetMapping("/infodata")
    public String infoData(Model model) {

        ArrayList<InfoData> infoDataSet = (ArrayList<InfoData>) infoDataRepository.findAll();
        model.addAttribute("infodatas", infoDataSet);
        return "infodata";
    }
}
