package com.example.demo.service;

import com.example.demo.config.IdCardProperties;
import com.example.demo.model.Profile;
import org.springframework.stereotype.Service;

/**
 * Derives the verifiable content embedded on a card — the public verification URL
 * (encoded in the QR code) and the barcode payload — and produces the QR/barcode
 * images for a given profile.
 */
@Service
public class IdCardService {

    private final IdCardProperties properties;
    private final QrCodeService qrCodeService;
    private final BarcodeService barcodeService;

    public IdCardService(IdCardProperties properties,
                         QrCodeService qrCodeService,
                         BarcodeService barcodeService) {
        this.properties = properties;
        this.qrCodeService = qrCodeService;
        this.barcodeService = barcodeService;
    }

    /** Public verification URL for the card, e.g. {@code https://host/verify/<uuid>}. */
    public String verificationUrl(Profile profile) {
        String base = properties.getBaseUrl();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/verify/" + profile.getUuid();
    }

    /** The payload encoded in the linear barcode (the registration number). */
    public String barcodeContent(Profile profile) {
        return profile.getRegistrationNumber();
    }

    public byte[] qrPng(Profile profile, int size) {
        return qrCodeService.pngFor(verificationUrl(profile), size);
    }

    public byte[] barcodePng(Profile profile) {
        return barcodeService.pngFor(barcodeContent(profile), profile.getBarcodeType());
    }
}
