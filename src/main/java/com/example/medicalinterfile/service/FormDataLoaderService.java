package com.example.medicalinterfile.service;

import com.example.medicalinterfile.model.FormData;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Service
public class FormDataLoaderService {
    public FormData loadFromResource(String filename) {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (is == null) return null;
            props.load(is);
            FormData data = new FormData();
            data.setFirstName(props.getProperty("firstName", ""));
            data.setLastName(props.getProperty("lastName", ""));
            data.setANumber(props.getProperty("aNumber", ""));
            data.setDob(props.getProperty("dob", ""));
            data.setEmail(props.getProperty("email", ""));
            data.setPhone(props.getProperty("phone", ""));
            data.setReceipt(props.getProperty("receipt", ""));
            data.setReceiptDate(props.getProperty("receiptDate", ""));
            data.setStatus(props.getProperty("status", ""));
            data.setStreetAddress(props.getProperty("streetAddress", ""));
            data.setCity(props.getProperty("city", ""));
            data.setState(props.getProperty("state", ""));
            data.setCountry(props.getProperty("country", ""));
            data.setZip(props.getProperty("zip", ""));
            return data;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load form data from resource", e);
        }
    }
}
