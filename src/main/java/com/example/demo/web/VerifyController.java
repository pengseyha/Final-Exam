package com.example.demo.web;

import com.example.demo.model.Profile;
import com.example.demo.service.ProfileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Public verification landing page reached by scanning a card's QR code
 * ({@code /verify/<uuid>}).
 */
@Controller
public class VerifyController {

    private final ProfileService profileService;
    private final CardViewFactory cardViewFactory;

    public VerifyController(ProfileService profileService, CardViewFactory cardViewFactory) {
        this.profileService = profileService;
        this.cardViewFactory = cardViewFactory;
    }

    @GetMapping("/verify/{uuid}")
    public String verify(@PathVariable String uuid, Model model) {
        Profile profile = profileService.findByUuid(uuid);
        model.addAttribute("profile", profile);
        model.addAttribute("card", cardViewFactory.forSavedProfile(profile));
        return "verify";
    }
}
