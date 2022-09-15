package cz.snyll.Sunny.controllers;

import cz.snyll.Sunny.config.MainConfiguration;
import cz.snyll.Sunny.domain.InfoData;
import cz.snyll.Sunny.repositories.InfoDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Set;

@Controller
public class IndexController {

    @Autowired
    private InfoDataRepository infoDataRepository;
    @Autowired
    private MainConfiguration mainConfiguration;
    @GetMapping("/")
    public String index(Model model) {

        Set<InfoData> infoDataSet = infoDataRepository.findByPrefix("solax");
        for (InfoData infoData : infoDataSet ) {
            model.addAttribute(infoData.getDataKey(), infoData.prettyValue());
            model.addAttribute(infoData.getDataKey()+"_number", infoData.floatValue());
            model.addAttribute("total_power_installed", mainConfiguration.getTotalPanelPowerInstalled());
        }
        return "index";
    }
}
