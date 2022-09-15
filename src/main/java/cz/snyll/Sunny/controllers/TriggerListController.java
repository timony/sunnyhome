package cz.snyll.Sunny.controllers;

import cz.snyll.Sunny.domain.Trigger;
import cz.snyll.Sunny.services.TriggerManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;

@Controller
public class TriggerListController {
    private TriggerManagerService triggerManagerService;

    @Autowired
    public TriggerListController(TriggerManagerService triggerManagerService) {
        this.triggerManagerService = triggerManagerService;
    }

    @GetMapping("/triggerlist")
    public String triggerlist(Model model, @RequestParam(required = false) boolean success, @RequestParam(required = false) boolean deletesuccess) {
        ArrayList<Trigger> triggerDataSet = triggerManagerService.LoadAll();
        model.addAttribute("success", success);
        model.addAttribute("deletesuccess", deletesuccess);
        model.addAttribute("triggers", triggerDataSet);
        return "triggerlist";
    }
}
