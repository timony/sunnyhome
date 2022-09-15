package cz.snyll.Sunny.controllers;

import cz.snyll.Sunny.domain.EventEntry;
import cz.snyll.Sunny.services.EventEntryManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;

@Controller
public class EventListController {
    EventEntryManagerService eventEntryManagerService;
    @Autowired
    public EventListController(EventEntryManagerService eventEntryManagerService) {
        this.eventEntryManagerService = eventEntryManagerService;
    }

    @GetMapping("/eventlist")
    public String eventList(Model model) {
        ArrayList<EventEntry> eventDataSet = (ArrayList<EventEntry>) eventEntryManagerService.getLastEvents(200);

        model.addAttribute("events", eventDataSet);
        return "eventlist";
    }
}
