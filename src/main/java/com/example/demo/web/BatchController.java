package com.example.demo.web;

import com.example.demo.model.ProfileType;
import com.example.demo.service.BatchIdCardService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Batch ID-card generation: bundles many cards into a single downloadable ZIP,
 * selected either by an explicit list of ids or by profile type.
 */
@Controller
@RequestMapping("/batch")
public class BatchController {

    private final BatchIdCardService batchService;

    public BatchController(BatchIdCardService batchService) {
        this.batchService = batchService;
    }

    @PostMapping
    public void generate(@RequestParam(required = false) List<Long> ids,
                        @RequestParam(required = false) ProfileType type,
                        HttpServletResponse response) throws Exception {
        byte[] zip;
        String name;
        if (ids != null && !ids.isEmpty()) {
            zip = batchService.zipForIds(ids);
            name = "id-cards-selected.zip";
        } else if (type != null) {
            zip = batchService.zipForType(type);
            name = "id-cards-" + type.name().toLowerCase() + ".zip";
        } else {
            zip = batchService.zipAll();
            name = "id-cards-all.zip";
        }
        write(response, zip, name);
    }

    @GetMapping("/type/{type}")
    public void byType(@org.springframework.web.bind.annotation.PathVariable ProfileType type,
                      HttpServletResponse response) throws Exception {
        write(response, batchService.zipForType(type), "id-cards-" + type.name().toLowerCase() + ".zip");
    }

    private void write(HttpServletResponse response, byte[] zip, String name) throws Exception {
        response.setContentType("application/zip");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + name);
        response.setContentLength(zip.length);
        response.getOutputStream().write(zip);
    }
}
