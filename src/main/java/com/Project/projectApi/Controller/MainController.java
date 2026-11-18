package com.Project.projectApi.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String inicio() {
        return "inicio";
    }

    @GetMapping("/initAdmin")
    public String administrador() {
        return "redirect:/admin";
    }

    @GetMapping("/initCliente")
    public String cliente() {
        return "redirect:/cliente";
    }

}
