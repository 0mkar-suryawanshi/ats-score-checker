package com.ats.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FeedbackItem {
    private String type;   // "good", "warn", "error"
    private String message;
}
