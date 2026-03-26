package com.ats.model;

import com.ats.model.AtsRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AtsRequest {

    @NotBlank(message = "Resume text must not be empty")
    @Size(min = 50, max = 10000, message = "Resume must be between 50 and 10000 characters")
    private String resumeText;

    // Optional job description for targeted keyword matching
    private String jobDescription;

    // Target role: backend, frontend, fullstack, data, devops, mobile
    private String targetRole = "backend";
}
