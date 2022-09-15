package cz.snyll.Sunny.controllers;

import cz.snyll.Sunny.domain.Trigger;
import cz.snyll.Sunny.repositories.TriggerRepository;
import cz.snyll.Sunny.services.TriggerManagerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TriggerController {

    @Autowired
    private TriggerRepository triggerRepository;
    @Autowired
    private TriggerManagerService triggerManagerService;

    @GetMapping("/trigger")
    public String trigger(Model model, @RequestParam(required = false) Long id) {
        if (id == null)
            id = 0l;
        Trigger trigger = triggerRepository.findById(id).orElseGet(() -> new Trigger());
        model.addAttribute("trigger", trigger);
        return "trigger";
    }

    @GetMapping("/trigger/edit")
    public String triggerEdit(Model model, @RequestParam(required = false) Long id, @RequestParam(required = false) boolean delete) {
        if (delete == true  && id > 0) {
            triggerManagerService.DeleteTriggerById(id);
            return "redirect:../triggerlist?deletesuccess=true";
        } else {
            return "redirect:../triggerlist?deletesuccess=false";
        }
    }

    @PostMapping("/trigger")
    public String triggerSubmit(@Valid @ModelAttribute Trigger trigger, BindingResult result, Model model) {
        model.addAttribute("trigger", trigger);

        if (result.hasErrors()) {
            return "trigger";
        } else {
            triggerManagerService.SaveTrigger(trigger);
            return "redirect:triggerlist?success=true";
        }
    }
}