package com.example.demo.web;

import com.example.demo.model.BarcodeType;
import com.example.demo.model.Profile;
import com.example.demo.model.ProfileBuilder;
import com.example.demo.model.ProfileType;
import com.example.demo.service.IdCardPdfService;
import com.example.demo.service.IdCardService;
import com.example.demo.service.ProfileService;
import com.example.demo.service.TemplateService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Web UI and media endpoints for student / employee / user profiles:
 * CRUD, instant live preview, photo / QR / barcode images and PDF export.
 */
@Controller
@RequestMapping("/profiles")
public class ProfileController {

    private final ProfileService profileService;
    private final TemplateService templateService;
    private final ProfileBuilder profileBuilder;
    private final CardViewFactory cardViewFactory;
    private final IdCardService idCardService;
    private final IdCardPdfService pdfService;

    public ProfileController(ProfileService profileService,
                             TemplateService templateService,
                             ProfileBuilder profileBuilder,
                             CardViewFactory cardViewFactory,
                             IdCardService idCardService,
                             IdCardPdfService pdfService) {
        this.profileService = profileService;
        this.templateService = templateService;
        this.profileBuilder = profileBuilder;
        this.cardViewFactory = cardViewFactory;
        this.idCardService = idCardService;
        this.pdfService = pdfService;
    }

    // ----- List & search -----

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) ProfileType type,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Page<Profile> profiles = profileService.search(q, type,
                PageRequest.of(page, 10, Sort.by("updatedAt").descending()));
        model.addAttribute("profiles", profiles);
        model.addAttribute("q", q);
        model.addAttribute("type", type);
        model.addAttribute("types", ProfileType.values());
        return "profiles/list";
    }

    // ----- Create -----

    @GetMapping("/new")
    public String newForm(@RequestParam(defaultValue = "STUDENT") ProfileType type, Model model) {
        model.addAttribute("profile", profileBuilder.defaultFor(type));
        populateFormModel(model);
        return "profiles/form";
    }

    @PostMapping
    public String create(@ModelAttribute Profile profile,
                         @RequestParam(required = false) Long templateId,
                         @RequestParam(required = false) MultipartFile photo,
                         RedirectAttributes ra) {
        Profile saved = profileService.create(profile, templateId, photo);
        ra.addFlashAttribute("message", "Created card " + saved.getRegistrationNumber());
        return "redirect:/profiles/" + saved.getId();
    }

    // ----- Edit -----

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("profile", profileService.findById(id));
        populateFormModel(model);
        return "profiles/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                        @ModelAttribute Profile profile,
                        @RequestParam(required = false) Long templateId,
                        @RequestParam(required = false) MultipartFile photo,
                        @RequestParam(defaultValue = "false") boolean removePhoto,
                        RedirectAttributes ra) {
        profileService.update(id, profile, templateId, photo, removePhoto);
        ra.addFlashAttribute("message", "Updated card");
        return "redirect:/profiles/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        profileService.delete(id);
        ra.addFlashAttribute("message", "Deleted profile " + id);
        return "redirect:/profiles";
    }

    // ----- View saved card -----

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        Profile profile = profileService.findById(id);
        model.addAttribute("profile", profile);
        model.addAttribute("card", cardViewFactory.forSavedProfile(profile));
        return "profiles/view";
    }

    // ----- Live preview (unsaved) -----

    @PostMapping("/preview")
    public String preview(@ModelAttribute Profile profile,
                         @RequestParam(required = false) Long templateId,
                         Model model) {
        if (templateId != null) {
            profile.setTemplate(templateService.findById(templateId));
        }
        model.addAttribute("card", cardViewFactory.forPreview(profile));
        return "profiles/card :: card";
    }

    // ----- Media endpoints -----

    @GetMapping("/{id}/photo")
    public ResponseEntity<byte[]> photo(@PathVariable Long id) {
        Profile profile = profileService.findById(id);
        byte[] bytes = profileService.loadPhoto(profile);
        MediaType mediaType = "image/png".equals(profile.getPhotoContentType())
                ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(mediaType).body(bytes);
    }

    @GetMapping("/{id}/qr")
    public ResponseEntity<byte[]> qr(@PathVariable Long id,
                                    @RequestParam(defaultValue = "240") int size) {
        Profile profile = profileService.findById(id);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG)
                .body(idCardService.qrPng(profile, size));
    }

    @GetMapping("/{id}/barcode")
    public ResponseEntity<byte[]> barcode(@PathVariable Long id) {
        Profile profile = profileService.findById(id);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG)
                .body(idCardService.barcodePng(profile));
    }

    @GetMapping("/{id}/pdf")
    public void pdf(@PathVariable Long id, HttpServletResponse response) throws Exception {
        Profile profile = profileService.findById(id);
        byte[] pdf = pdfService.render(profile);
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=id-card-" + profile.getRegistrationNumber() + ".pdf");
        response.setContentLength(pdf.length);
        response.getOutputStream().write(pdf);
    }

    private void populateFormModel(Model model) {
        model.addAttribute("templates", templateService.findAll());
        model.addAttribute("types", ProfileType.values());
        model.addAttribute("barcodeTypes", BarcodeType.values());
    }
}
