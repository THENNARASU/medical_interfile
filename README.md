# Medical Interfile PDF Generator

This project is a Spring Boot web application that allows users to enter personal, case, and contact details via a web form and generates a PDF (with Code 128 barcode) matching the provided template.

## Features
- User form for entering details
- PDF generation with Code 128 barcode
- JDK 1.8 compatible

## How to Run
1. Ensure JDK 1.8 is installed
2. Build with `mvn clean package`
3. Run with `mvn spring-boot:run`

## Libraries Used
- Spring Boot (Web, Thymeleaf)
- iText PDF
- Barcode4J
