package cz.snyll.Sunny.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
class LoginController {
    @RequestMapping("/login")
    public String login() {
        return "login";
    }
}
