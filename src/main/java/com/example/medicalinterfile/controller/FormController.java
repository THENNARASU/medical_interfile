package com.example.medicalinterfile.controller;

import com.example.medicalinterfile.model.FormData;
import com.example.medicalinterfile.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Controller
public class FormController {

    @Autowired
    private PdfService pdfService;



    @GetMapping("/")
    public String showForm(@RequestParam(value = "uscisAddressName", required = false) String uscisAddressName,
                          Model model) {
        FormData formData = new FormData();
        // Convert date fields to yyyy-MM-dd for HTML date input
        formData.setDob(convertToHtmlDate(formData.getDob()));
        formData.setReceiptDate(convertToHtmlDate(formData.getReceiptDate()));

        // Load USCIS addresses
        Map<String, String> uscisAddresses = loadUscisAddresses();
        model.addAttribute("uscisAddresses", uscisAddresses);
        model.addAttribute("selectedUscisAddressName", uscisAddressName);
        model.addAttribute("formData", formData);
        return "form";
    }

    @PostMapping("/generate-pdf")
    @ResponseBody
    public ResponseEntity<byte[]> generatePdf(@ModelAttribute FormData formData,
                                              @RequestParam(value = "uscisAddressName", required = false) String uscisAddressName) {
        // Load address map and get the selected address
        Map<String, String> uscisAddresses = loadUscisAddresses();
        String uscisAddress = uscisAddresses.getOrDefault(uscisAddressName, "USCIS\nNATIONAL BENEFITS CENTER");
        byte[] pdfBytes = pdfService.generatePdf(formData, uscisAddress);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        // Compose filename: firstname lastname medical interfile.pdf
        StringBuilder filename = new StringBuilder();
        if (formData.getFirstName() != null && !formData.getFirstName().trim().isEmpty()) {
            filename.append(formData.getFirstName().trim());
        }
        if (formData.getLastName() != null && !formData.getLastName().trim().isEmpty()) {
            if (filename.length() > 0) filename.append(" ");
            filename.append(formData.getLastName().trim());
        }
        if (filename.length() > 0) {
            filename.append(" medical interfile.pdf");
        } else {
            filename.append("medical_interfile.pdf");
        }
        headers.setContentDispositionFormData("attachment", filename.toString());
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    private Map<String, String> loadUscisAddresses() {
        Map<String, String> map = new LinkedHashMap<>();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("uscis_addresses.dat")) {
            if (is == null) return map;
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            String currentName = null;
            StringBuilder currentAddress = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Name:")) {
                    if (currentName != null) {
                        map.put(currentName, currentAddress.toString().trim());
                    }
                    currentName = line.substring(5).trim();
                    currentAddress = new StringBuilder();
                } else if (!line.startsWith("Address:")) {
                    if (!line.trim().isEmpty()) {
                        if (currentAddress.length() > 0) currentAddress.append("\n");
                        currentAddress.append(line.trim());
                    }
                }
            }
            if (currentName != null) {
                map.put(currentName, currentAddress.toString().trim());
            }
        } catch (Exception e) {
            // ignore, return empty map
        }
        return map;
    }

    private String convertToHtmlDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return "";
        String[] patterns = {"MM/dd/yyyy", "yyyy-MM-dd", "dd/MM/yyyy"};
        for (String pattern : patterns) {
            try {
                java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat(pattern);
                java.util.Date date = inputFormat.parse(dateStr.trim());
                java.text.SimpleDateFormat htmlFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
                return htmlFormat.format(date);
            } catch (Exception ignored) {}
        }
        return "";
    }

}
