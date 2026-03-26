package com.ats.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AtsResponse {

    private int overallScore;
    private String scoreLabel;       // "Excellent", "Good", "Needs Work", etc.
    private String scoreColor;       // hex

    private List<ScoreBreakdown> breakdown;

    // Stats summary
    private int keywordsFound;
    private int keywordsTotal;
    private long actionVerbsCount;
    private int wordCount;

    // Keyword lists
    private List<String> foundKeywords;
    private List<String> missingKeywords;

    // Feedback
    private List<FeedbackItem> feedback;

    // Role analyzed
    private String targetRole;
    private String roleLabel;
}
