
package com.example.medicalinterfile.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.medicalinterfile.model.FormData;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import org.springframework.stereotype.Service;

// import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class PdfService {
    public byte[] generatePdf(FormData data, String uscisAddress) {
        try {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            // Header: To, USCIS, Barcodes
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{2f, 3f});
            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.addElement(new Phrase("To,\n" + uscisAddress, new Font(Font.FontFamily.HELVETICA, 10)));
            headerTable.addCell(leftCell);

            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(Rectangle.NO_BORDER);
            PdfPTable barcodeTable = new PdfPTable(2);
            barcodeTable.setWidthPercentage(100);
            // Labels above barcodes
            // Barcode labels and barcodes in a 2-row, 2-column table
            PdfPCell label1 = new PdfPCell(new Phrase("Receipt#", new Font(Font.FontFamily.HELVETICA, 8)));
            label1.setBorder(Rectangle.NO_BORDER);
            label1.setHorizontalAlignment(Element.ALIGN_CENTER);
            barcodeTable.addCell(label1);
            PdfPCell label2 = new PdfPCell(new Phrase("Alien#", new Font(Font.FontFamily.HELVETICA, 8)));
            label2.setBorder(Rectangle.NO_BORDER);
            label2.setHorizontalAlignment(Element.ALIGN_CENTER);
            barcodeTable.addCell(label2);

            Image barcode1 = Image.getInstance(generateBarcode(data.getReceipt(), 140, 32));
            barcode1.scalePercent(100);
            PdfPCell bc1 = new PdfPCell(barcode1, false);
            bc1.setBorder(Rectangle.NO_BORDER);
            bc1.setHorizontalAlignment(Element.ALIGN_CENTER);
            bc1.setPaddingTop(2f);
            barcodeTable.addCell(bc1);
            Image barcode2 = Image.getInstance(generateBarcode(data.getANumber(), 140, 32));
            barcode2.scalePercent(100);
            PdfPCell bc2 = new PdfPCell(barcode2, false);
            bc2.setBorder(Rectangle.NO_BORDER);
            bc2.setHorizontalAlignment(Element.ALIGN_CENTER);
            bc2.setPaddingTop(2f);
            barcodeTable.addCell(bc2);

            // Numbers below barcodes
            PdfPCell num1 = new PdfPCell(new Phrase(data.getReceipt(), new Font(Font.FontFamily.HELVETICA, 8)));
            num1.setBorder(Rectangle.NO_BORDER);
            num1.setHorizontalAlignment(Element.ALIGN_CENTER);
            barcodeTable.addCell(num1);
            PdfPCell num2 = new PdfPCell(new Phrase(data.getANumber(), new Font(Font.FontFamily.HELVETICA, 8)));
            num2.setBorder(Rectangle.NO_BORDER);
            num2.setHorizontalAlignment(Element.ALIGN_CENTER);
            barcodeTable.addCell(num2);
            rightCell.addElement(barcodeTable);
            headerTable.addCell(rightCell);
            document.add(headerTable);

            // Subject and body
            Font subjectFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
            Paragraph subject = new Paragraph("\n\nSubject:  Request to Interfile Form I-693 with Pending Form I-485", subjectFont);
            subject.setSpacingBefore(22f);
            document.add(subject);

            document.add(new Paragraph("\n\nDear Sir/Madam,", new Font(Font.FontFamily.HELVETICA, 10)));
            document.add(new Paragraph("\nMy Form I-485 (Adjustment of Status) is pending. My priority date became current as per the April 2026 Visa Bulletin.", new Font(Font.FontFamily.HELVETICA, 10)));
            document.add(new Paragraph("\nPlease interfile the enclosed Form I-693 (Report of Immigration Medical Examination and Vaccination Record), signed by a civil surgeon, with my pending Form I-485.", new Font(Font.FontFamily.HELVETICA, 10)));


            // Personal Details section (single column, full width)
            PdfPTable personalDetailsTable = new PdfPTable(1);
            personalDetailsTable.setWidthPercentage(100);
            personalDetailsTable.setSpacingBefore(36f); // keep as is for Personal Details
            PdfPCell personalDetailsCell = new PdfPCell();
            personalDetailsCell.setBorder(Rectangle.NO_BORDER);
            personalDetailsCell.addElement(new Phrase("PERSONAL DETAILS", new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
            PdfPTable personalTable = new PdfPTable(2);
            personalTable.setWidthPercentage(100);
            personalTable.setWidths(new float[]{1.3f, 2.0f});
            personalTable.addCell(makeAlignedCell("First Name", true));
            personalTable.addCell(makeAlignedCell(data.getFirstName(), false));
            if (data.getMiddleName() != null && !data.getMiddleName().trim().isEmpty()) {
                personalTable.addCell(makeAlignedCell("Middle Name", true));
                personalTable.addCell(makeAlignedCell(data.getMiddleName(), false));
            }
            personalTable.addCell(makeAlignedCell("Last Name", true));
            personalTable.addCell(makeAlignedCell(data.getLastName(), false));
            personalTable.addCell(makeAlignedCell("Alien#", true));
            personalTable.addCell(makeAlignedCell(data.getANumber(), false));
            personalTable.addCell(makeAlignedCell("Date of Birth", true));
            personalTable.addCell(makeAlignedCell(formatDate(data.getDob()), false));
            // Add Country of Birth below Date of Birth
            personalTable.addCell(makeAlignedCell("Country of Birth", true));
            personalTable.addCell(makeAlignedCell("INDIA", false));
            personalDetailsCell.addElement(personalTable);
            // Combine Personal Details and Case Details side by side
            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);
            detailsTable.setSpacingBefore(36f);
            detailsTable.setWidths(new float[]{0.9f, 1.2f});

            // Personal Details cell (left)
            PdfPCell personalDetailsCell2 = new PdfPCell();
            personalDetailsCell2.setBorder(Rectangle.NO_BORDER);
            personalDetailsCell2.addElement(new Phrase("PERSONAL DETAILS", new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
            PdfPTable personalTable2 = new PdfPTable(2);
            personalTable2.setWidthPercentage(100);
            personalTable2.setWidths(new float[]{1.3f, 2.0f});
            personalTable2.addCell(makeAlignedCell("First Name", true));
            personalTable2.addCell(makeAlignedCell(data.getFirstName(), false));
            if (data.getMiddleName() != null && !data.getMiddleName().trim().isEmpty()) {
                personalTable2.addCell(makeAlignedCell("Middle Name", true));
                personalTable2.addCell(makeAlignedCell(data.getMiddleName(), false));
            }
            personalTable2.addCell(makeAlignedCell("Last Name", true));
            personalTable2.addCell(makeAlignedCell(data.getLastName(), false));
            personalTable2.addCell(makeAlignedCell("Alien#", true));
            personalTable2.addCell(makeAlignedCell(data.getANumber(), false));
            personalTable2.addCell(makeAlignedCell("Date of Birth", true));
            personalTable2.addCell(makeAlignedCell(formatDate(data.getDob()), false));
            personalTable2.addCell(makeAlignedCell("Country of Birth", true));
            personalTable2.addCell(makeAlignedCell("INDIA", false));
            personalDetailsCell2.addElement(personalTable2);
            detailsTable.addCell(personalDetailsCell2);

            // Case Details cell (right)
            PdfPCell caseDetailsCell2 = new PdfPCell();
            caseDetailsCell2.setBorder(Rectangle.NO_BORDER);
            caseDetailsCell2.addElement(new Phrase("CASE DETAILS (I-485)", new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
            PdfPTable caseTable2 = new PdfPTable(2);
            caseTable2.setWidthPercentage(100);
            caseTable2.setHorizontalAlignment(Element.ALIGN_LEFT); // align table to left
            caseTable2.setWidths(new float[]{1.2f, 2.9f}); // make value column narrower to bring value closer
            PdfPCell leftLabel, rightValue;
            float indent = 12f;
            leftLabel = makeAlignedCell("Receipt#", true);
            leftLabel.setHorizontalAlignment(Element.ALIGN_LEFT);
            leftLabel.setPaddingLeft(indent);
            rightValue = makeAlignedCell(data.getReceipt(), false, true);
            rightValue.setHorizontalAlignment(Element.ALIGN_LEFT);
            rightValue.setPaddingLeft(0f); // remove left padding from value
            caseTable2.addCell(leftLabel);
            caseTable2.addCell(rightValue);
            leftLabel = makeAlignedCell("Receipt Date", true);
            leftLabel.setHorizontalAlignment(Element.ALIGN_LEFT);
            leftLabel.setPaddingLeft(indent);
            rightValue = makeAlignedCell(formatDate(data.getReceiptDate()), false, true);
            rightValue.setHorizontalAlignment(Element.ALIGN_LEFT);
            rightValue.setPaddingLeft(0f);
            caseTable2.addCell(leftLabel);
            caseTable2.addCell(rightValue);
            leftLabel = makeAlignedCell("Current Status", true);
            leftLabel.setHorizontalAlignment(Element.ALIGN_LEFT);
            leftLabel.setPaddingLeft(indent);
            rightValue = makeAlignedCell("Pending", false, true);
            rightValue.setHorizontalAlignment(Element.ALIGN_LEFT);
            rightValue.setPaddingLeft(0f);
            caseTable2.addCell(leftLabel);
            caseTable2.addCell(rightValue);
            caseDetailsCell2.addElement(caseTable2);
            detailsTable.addCell(caseDetailsCell2);

            document.add(detailsTable);

            // Contact and Address
            PdfPTable contactTable = new PdfPTable(2);
            contactTable.setWidthPercentage(100);
            contactTable.setSpacingBefore(12f); // reduced spacing so Mailing Address is not too far from Case Details
            contactTable.setWidths(new float[]{2.2f, 2.8f});


            PdfPCell contactCell = new PdfPCell();
            contactCell.setBorder(Rectangle.NO_BORDER);
            contactCell.addElement(new Phrase("CONTACT", new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
            PdfPTable contactInnerTable = new PdfPTable(2);
            contactInnerTable.setWidthPercentage(100);
            contactInnerTable.setWidths(new float[]{0.8f, 2.8f});
            contactInnerTable.addCell(makeAlignedCell("Email", true));
            contactInnerTable.addCell(makeAlignedCell(data.getEmail(), false));
            contactInnerTable.addCell(makeAlignedCell("Phone#", true));
            contactInnerTable.addCell(makeAlignedCell(data.getPhone(), false));


            contactCell.addElement(contactInnerTable);
            contactTable.addCell(contactCell);


            PdfPCell addressCell = new PdfPCell();
            addressCell.setBorder(Rectangle.NO_BORDER);
            addressCell.addElement(new Phrase("MAILING ADDRESS", new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
            // Address block: Street on one line, City State Zip on next line, no labels
            String street = data.getStreetAddress() != null ? data.getStreetAddress() : "";
            StringBuilder cityStateZip = new StringBuilder();
            if (data.getCity() != null && !data.getCity().isEmpty()) {
                cityStateZip.append(data.getCity());
            }
            if (data.getState() != null && !data.getState().isEmpty()) {
                if (cityStateZip.length() > 0) cityStateZip.append(" ");
                cityStateZip.append(data.getState());
            }
            if (data.getZip() != null && !data.getZip().isEmpty()) {
                if (cityStateZip.length() > 0) cityStateZip.append(" ");
                cityStateZip.append(data.getZip());
            }
            Paragraph addressBlock = new Paragraph();
            addressBlock.setSpacingBefore(0f);
            addressBlock.add(new Phrase(street, new Font(Font.FontFamily.HELVETICA, 10)));
            addressBlock.add(Chunk.NEWLINE);
            addressBlock.add(new Phrase(cityStateZip.toString(), new Font(Font.FontFamily.HELVETICA, 10)));
            // Indent the address block for better visual separation
            addressBlock.setIndentationLeft(9f); // Move address a bit to the right
            addressCell.addElement(addressBlock);
            contactTable.addCell(addressCell);
            document.add(contactTable);

            // Signature and Enclosures

            PdfPTable bottomTable = new PdfPTable(2);
            bottomTable.setWidthPercentage(100);
            bottomTable.setSpacingBefore(36f);
            bottomTable.setWidths(new float[]{2.2f, 2.8f});

            // Signature box (empty, with border)
            PdfPCell signatureBoxCell = new PdfPCell();
            signatureBoxCell.setBorder(Rectangle.BOX);
            signatureBoxCell.setFixedHeight(50f);
            signatureBoxCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            bottomTable.addCell(signatureBoxCell);

            // Enclosures cell (same as before)
            PdfPCell enclosuresCell = new PdfPCell();
            enclosuresCell.setBorder(Rectangle.NO_BORDER);
            enclosuresCell.addElement(new Phrase("   Enclosures:", new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
            enclosuresCell.addElement(new Phrase("   1. Copy of I-797C, USCIS i-485 Receipt Notice\n   2. Sealed I-693 Medical Report signed by the Civil Surgeon\n   3. Copy of Passport\n   4. Copy of Birth Certificate", new Font(Font.FontFamily.HELVETICA, 9)));
            bottomTable.addCell(enclosuresCell);

            // Add the label 'Signature' below the box, spanning only the first column
            PdfPCell signatureLabelCell = new PdfPCell(new Phrase("Signature", new Font(Font.FontFamily.HELVETICA, 10)));
            signatureLabelCell.setBorder(Rectangle.NO_BORDER);
            signatureLabelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            signatureLabelCell.setColspan(1);
            bottomTable.addCell(signatureLabelCell);

            // Add an empty cell for the second column to keep the table structure
            PdfPCell emptyCell = new PdfPCell();
            emptyCell.setBorder(Rectangle.NO_BORDER);
            bottomTable.addCell(emptyCell);

            document.add(bottomTable);

            // Date and Place (default to today's date and CITY STATE ZIP)
            PdfPTable datePlaceTable = new PdfPTable(2);
            datePlaceTable.setWidthPercentage(100);
            datePlaceTable.setSpacingBefore(18f);
            datePlaceTable.setWidths(new float[]{2.2f, 2.8f});
            PdfPCell dateCell = new PdfPCell();
            dateCell.setBorder(Rectangle.NO_BORDER);
            //String today = new java.text.SimpleDateFormat("MM/dd/yyyy").format(new java.util.Date());
            String today = "04/01/2026";
            dateCell.addElement(new Phrase("Date:    " + today, new Font(Font.FontFamily.HELVETICA, 10)));
            datePlaceTable.addCell(dateCell);
            PdfPCell placeCell = new PdfPCell();
            placeCell.setBorder(Rectangle.NO_BORDER);
            StringBuilder cityStateZip2 = new StringBuilder();
            if (data.getCity() != null && !data.getCity().isEmpty()) {
                cityStateZip2.append(data.getCity());
            }
            if (data.getState() != null && !data.getState().isEmpty()) {
                if (cityStateZip2.length() > 0) cityStateZip2.append(" ");
                cityStateZip2.append(data.getState());
            }
            if (data.getZip() != null && !data.getZip().isEmpty()) {
                if (cityStateZip2.length() > 0) cityStateZip2.append(" ");
                cityStateZip2.append(data.getZip());
            }
            placeCell.addElement(new Phrase("Place:    " + cityStateZip2.toString(), new Font(Font.FontFamily.HELVETICA, 10)));
            datePlaceTable.addCell(placeCell);
            document.add(datePlaceTable);

            document.close();
            writer.close();

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }


    // Helper for no-border cell
    private PdfPCell makeNoBorderCell(String text, int fontSize, boolean bold) {
        Font font = new Font(Font.FontFamily.HELVETICA, fontSize, bold ? Font.BOLD : Font.NORMAL);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    // Helper for aligned cell: label right-aligned, value left-aligned, two spaces between
    private PdfPCell makeAlignedCell(String text, boolean isLabel) {
        return makeAlignedCell(text, isLabel, false);
    }

    // Overloaded: allow disabling value prefix
    private PdfPCell makeAlignedCell(String text, boolean isLabel, boolean noPrefix) {
        Font font = new Font(Font.FontFamily.HELVETICA, 10, isLabel ? Font.BOLD : Font.NORMAL);
        Phrase phrase;
        if (isLabel || noPrefix) {
            phrase = new Phrase(text != null ? text : "", font);
        } else {
            phrase = new Phrase("  " + (text != null ? text : ""), font);
        }
        PdfPCell cell = new PdfPCell(phrase);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(isLabel ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
        return cell;
    }

    // Helper to format date as MM/dd/yyyy
    private String formatDate(String dateStr) {
        SimpleDateFormat[] inputFormats = new SimpleDateFormat[] {
            new SimpleDateFormat("MM/dd/yyyy"),
            new SimpleDateFormat("yyyy-MM-dd"),
            new SimpleDateFormat("dd/MM/yyyy")
        };
        SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yyyy");
        if (dateStr == null || dateStr.trim().isEmpty()) return "";
        for (SimpleDateFormat fmt : inputFormats) {
            try {
                Date d = fmt.parse(dateStr.trim());
                return outputFormat.format(d);
            } catch (ParseException ignored) {}
        }
        return dateStr; // fallback to original if parsing fails
    }

    private void addRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        labelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        labelCell.setBorderWidth(1f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "", valueFont));
        valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        valueCell.setBorderWidth(1f);
        table.addCell(valueCell);
    }

    private byte[] generateBarcode(String text, int width, int height) throws IOException {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.CODE_128, width, height);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IOException("Failed to generate barcode", e);
        }
    }
}
