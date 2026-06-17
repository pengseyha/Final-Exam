package com.example.demo.web;

import com.example.demo.model.Template;
import com.example.demo.service.TemplateService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Web UI for managing reusable card templates (themes).
 */
@Controller
@RequestMapping("/templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("templates", templateService.search(q));
        model.addAttribute("q", q);
        return "templates/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("template", Template.builder().build());
        return "templates/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("template", templateService.findById(id));
        return "templates/form";
    }

    @PostMapping
    public String create(@ModelAttribute Template template, RedirectAttributes ra) {
        templateService.create(template);
        ra.addFlashAttribute("message", "Created template " + template.getCode());
        return "redirect:/templates";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Template template, RedirectAttributes ra) {
        templateService.update(id, template);
        ra.addFlashAttribute("message", "Updated template");
        return "redirect:/templates";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        templateService.delete(id);
        ra.addFlashAttribute("message", "Deleted template " + id);
        return "redirect:/templates";
    }
}
