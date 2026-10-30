package com.accenture.springai_bootcamp_demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
        @NotBlank @Email String customerEmail,
        @NotBlank @Size(max = 200) String subject,
        @NotBlank @Size(max = 8000) String description) {
}
