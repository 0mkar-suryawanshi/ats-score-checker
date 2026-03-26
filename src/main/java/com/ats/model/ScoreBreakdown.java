package com.ats.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScoreBreakdown {
    private String category;
    private int score;
    private String color;   // hex color for the UI bar
    private String status;  // "good", "warn", "poor"
}
