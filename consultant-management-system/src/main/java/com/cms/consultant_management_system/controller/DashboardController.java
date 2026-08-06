package com.cms.consultant_management_system.controller;

import com.cms.consultant_management_system.service.ConsultantService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final ConsultantService service;

    public DashboardController(ConsultantService service) {
        this.service = service;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("total",        service.countTotal());
        model.addAttribute("newThisMonth", service.countNewThisMonth());
        model.addAttribute("active",       service.countActive());
        model.addAttribute("inactive",     service.countInactive());
        model.addAttribute("latest",       service.findLatest(3));
        return "dashboard";
    }
}