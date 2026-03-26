package com.ats.controller;


import com.ats.model.AtsRequest;
import com.ats.model.AtsResponse;
import com.ats.service.AtsAnalyzerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ats")
@CrossOrigin(origins = "*")
public class AtsApiController {

    private final AtsAnalyzerService analyzerService;

    public AtsApiController(AtsAnalyzerService analyzerService) {
        this.analyzerService = analyzerService;
    }

    /**
     * POST /api/ats/analyze
     * Accepts resume text + optional JD, returns full ATS analysis.
     */
    @PostMapping("/analyze")
    public ResponseEntity<AtsResponse> analyze(@Valid @RequestBody AtsRequest request) {
        AtsResponse response = analyzerService.analyze(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/ats/roles
     * Returns all supported target roles.
     */
    @GetMapping("/roles")
    public ResponseEntity<Map<String, String>> getRoles() {
        return ResponseEntity.ok(Map.of(
            "backend",   "Backend Developer",
            "frontend",  "Frontend Developer",
            "fullstack", "Full Stack Developer",
            "data",      "Data Engineer",
            "devops",    "DevOps Engineer",
            "mobile",    "Mobile Developer"
        ));
    }

    /**
     * GET /api/ats/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "ATS Checker"));
    }
}
