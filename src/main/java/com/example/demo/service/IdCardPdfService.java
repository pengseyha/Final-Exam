package com.example.demo.service;

import com.example.demo.model.Profile;
import com.example.demo.model.Template;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.colors.WebColors;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Renders a single {@link Profile} as a portrait ID-card PDF using iText 7. The
 * template colours drive the header band so the PDF matches the HTML preview.
 */
@Service
public class IdCardPdfService {

    private static final PageSize CARD = new PageSize(320f, 500f);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final IdCardService idCardService;
    private final ProfileService profileService;

    public IdCardPdfService(IdCardService idCardService, ProfileService profileService) {
        this.idCardService = idCardService;
        this.profileService = profileService;
    }

    public byte[] render(Profile profile) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PdfWriter writer = new PdfWriter(out);
             PdfDocument pdf = new PdfDocument(writer);
             Document doc = new Document(pdf, CARD)) {

            pdf.setDefaultPageSize(CARD);
            doc.setMargins(0, 0, 0, 0);

            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            Template template = profile.getTemplate();
            Color primary = color(template != null ? template.getPrimaryColor() : null, new DeviceRgb(29, 78, 216));
            Color text = color(template != null ? template.getTextColor() : null, new DeviceRgb(17, 24, 39));

            doc.add(header(profile, template, bold, regular, primary));
            doc.add(body(profile, template, bold, regular, text));
            doc.add(codes(profile, regular, text));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to render ID card PDF for profile " + profile.getId(), e);
        }
        return out.toByteArray();
    }

    private Div header(Profile profile, Template template, PdfFont bold, PdfFont regular, Color primary) {
        Div header = new Div()
                .setBackgroundColor(primary)
                .setWidth(UnitValue.createPercentValue(100))
                .setPadding(14)
                .setFontColor(ColorConstants.WHITE);

        String org = template != null && template.getOrganizationName() != null
                ? template.getOrganizationName() : "ID Card";
        header.add(new Paragraph(org).setFont(bold).setFontSize(15).setMargin(0));

        if (template != null && template.getTagline() != null && !template.getTagline().isBlank()) {
            header.add(new Paragraph(template.getTagline()).setFont(regular).setFontSize(9).setMargin(0));
        }
        header.add(new Paragraph(profile.getType().name() + " IDENTITY CARD")
                .setFont(regular).setFontSize(8).setMarginTop(4).setMargin(0));
        return header;
    }

    private Div body(Profile profile, Template template, PdfFont bold, PdfFont regular, Color text) {
        Div body = new Div().setPadding(16).setFontColor(text);

        if (profileService.hasStoredPhoto(profile)) {
            Image photo = new Image(ImageDataFactory.create(profileService.loadPhoto(profile)));
            photo.setWidth(96).setHeight(120)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER)
                    .setBorder(new com.itextpdf.layout.borders.SolidBorder(
                            color(template != null ? template.getPrimaryColor() : null, ColorConstants.GRAY), 1));
            body.add(photo);
        }

        body.add(new Paragraph(nullToDash(profile.getFullName()))
                .setFont(bold).setFontSize(15).setTextAlignment(TextAlignment.CENTER).setMarginTop(8).setMarginBottom(0));
        if (profile.getTitle() != null) {
            body.add(new Paragraph(profile.getTitle())
                    .setFont(regular).setFontSize(10).setTextAlignment(TextAlignment.CENTER).setMargin(0));
        }

        Table details = new Table(UnitValue.createPercentArray(new float[]{38, 62}))
                .setWidth(UnitValue.createPercentValue(100)).setMarginTop(10);
        addRow(details, bold, regular, "ID No.", profile.getRegistrationNumber());
        addRow(details, bold, regular, "Department", profile.getDepartment());
        addRow(details, bold, regular, "Blood Group", profile.getBloodGroup());
        addRow(details, bold, regular, "Date of Birth", fmt(profile.getDateOfBirth()));
        addRow(details, bold, regular, "Issued", fmt(profile.getIssueDate()));
        addRow(details, bold, regular, "Expires", fmt(profile.getExpiryDate()));
        addRow(details, bold, regular, "Email", profile.getEmail());
        addRow(details, bold, regular, "Phone", profile.getPhone());
        body.add(details);
        return body;
    }

    private Div codes(Profile profile, PdfFont regular, Color text) {
        Div codes = new Div().setPaddingLeft(16).setPaddingRight(16).setFontColor(text);

        Table grid = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                .setWidth(UnitValue.createPercentValue(100));

        Image qr = new Image(ImageDataFactory.create(idCardService.qrPng(profile, 220)));
        qr.setWidth(90).setHeight(90);
        Cell qrCell = new Cell().add(qr).setBorder(Border.NO_BORDER);

        Image barcode = new Image(ImageDataFactory.create(idCardService.barcodePng(profile)));
        barcode.setWidth(160).setAutoScaleHeight(true);
        Cell barcodeCell = new Cell()
                .add(barcode)
                .add(new Paragraph(idCardService.barcodeContent(profile))
                        .setFont(regular).setFontSize(7).setMargin(0))
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE);

        grid.addCell(qrCell);
        grid.addCell(barcodeCell);
        codes.add(grid);

        codes.add(new Paragraph("Scan to verify: " + idCardService.verificationUrl(profile))
                .setFont(regular).setFontSize(6).setMarginTop(4));
        return codes;
    }

    private void addRow(Table table, PdfFont bold, PdfFont regular, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setFont(bold).setFontSize(8))
                .setBorder(Border.NO_BORDER).setPaddingBottom(2));
        table.addCell(new Cell().add(new Paragraph(nullToDash(value)).setFont(regular).setFontSize(8))
                .setBorder(Border.NO_BORDER).setPaddingBottom(2));
    }

    private static String fmt(LocalDate date) {
        return date == null ? null : date.format(DATE);
    }

    private static String nullToDash(String value) {
        return (value == null || value.isBlank()) ? "—" : value;
    }

    private static Color color(String hex, Color fallback) {
        if (hex == null || hex.isBlank()) {
            return fallback;
        }
        try {
            Color parsed = WebColors.getRGBColor(hex);
            return parsed != null ? parsed : fallback;
        } catch (RuntimeException e) {
            return fallback;
        }
    }
}
