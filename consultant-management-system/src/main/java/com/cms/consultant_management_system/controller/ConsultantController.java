package com.cms.consultant_management_system.controller;

import com.cms.consultant_management_system.entity.Consultant;
import com.cms.consultant_management_system.entity.Status;
import com.cms.consultant_management_system.exception.DuplicateEmailException;
import com.cms.consultant_management_system.service.ConsultantService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/consultants")
public class ConsultantController {

    private final ConsultantService service;

    public ConsultantController(ConsultantService service) {
        this.service = service;
    }

    @ModelAttribute("statuses")
    public Status[] statuses() {
        return Status.values();
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "5") int size,
                       Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Consultant> result = service.search(keyword, pageable);

        model.addAttribute("page", result);
        model.addAttribute("consultants", result.getContent());
        model.addAttribute("keyword", keyword);
        return "consultants/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("consultant", new Consultant());
        return "consultants/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("consultant") Consultant consultant,
                         BindingResult binding,
                         RedirectAttributes flash) {
        if (binding.hasErrors()) {
            return "consultants/form";
        }
        try {
            service.create(consultant);
        } catch (DuplicateEmailException e) {
            binding.rejectValue("email", "duplicate", e.getMessage());
            return "consultants/form";
        }
        flash.addFlashAttribute("success", "Consultant added successfully.");
        return "redirect:/consultants";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("consultant", service.findById(id));
        return "consultants/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("consultant") Consultant consultant,
                         BindingResult binding,
                         RedirectAttributes flash) {
        if (binding.hasErrors()) {
            consultant.setId(id);
            return "consultants/form";
        }
        try {
            service.update(id, consultant);
        } catch (DuplicateEmailException e) {
            consultant.setId(id);
            binding.rejectValue("email", "duplicate", e.getMessage());
            return "consultants/form";
        }
        flash.addFlashAttribute("success", "Consultant updated successfully.");
        return "redirect:/consultants";
    }

    // Shows the confirmation page
    @GetMapping("/{id}/delete")
    public String deleteConfirm(@PathVariable Long id, Model model) {
        model.addAttribute("consultant", service.findById(id));
        return "consultants/delete";
    }

    // Performs the deletion
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes flash) {
        service.delete(id);
        flash.addFlashAttribute("success", "Consultant deleted successfully.");
        return "redirect:/consultants";
    }
}