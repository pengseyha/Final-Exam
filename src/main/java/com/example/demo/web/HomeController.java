package com.example.demo.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Redirects the root URL to the profile list. */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/profiles";
    }
}
