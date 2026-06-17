package com.example.demo.config;

import com.example.demo.model.Template;
import com.example.demo.service.TemplateService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Seeds a few ready-to-use card templates on startup so the app is usable out of
 * the box. Disabled under the {@code test} profile to keep tests deterministic.
 */
@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private final TemplateService templateService;

    public DataSeeder(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Override
    public void run(String... args) {
        templateService.getOrCreate(Template.builder()
                .code("classic-blue")
                .name("Classic Blue")
                .organizationName("Institute of Technology")
                .layout("VERTICAL")
                .primaryColor("#1d4ed8")
                .secondaryColor("#e0e7ff")
                .textColor("#111827")
                .tagline("Knowledge • Integrity • Service")
                .build());

        templateService.getOrCreate(Template.builder()
                .code("forest-green")
                .name("Forest Green")
                .organizationName("Institute of Technology")
                .layout("VERTICAL")
                .primaryColor("#047857")
                .secondaryColor("#d1fae5")
                .textColor("#052e16")
                .tagline("Grow • Learn • Lead")
                .build());

        templateService.getOrCreate(Template.builder()
                .code("charcoal")
                .name("Charcoal")
                .organizationName("Acme Corporation")
                .layout("VERTICAL")
                .primaryColor("#111827")
                .secondaryColor("#e5e7eb")
                .textColor("#111827")
                .tagline("Employee Identification")
                .build());
    }
}
