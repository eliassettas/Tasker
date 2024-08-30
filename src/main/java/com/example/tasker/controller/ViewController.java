package com.example.tasker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    // TODO: Find a way to replace this with something more generic that works for non defined paths too
    @GetMapping({"home", "login", "register"})
    public String index() {
        return "forward:/index.html";
    }
}
