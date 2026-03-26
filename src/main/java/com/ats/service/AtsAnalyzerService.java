package com.ats.service;

import com.ats.model.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

@Service
public class AtsAnalyzerService {

    // Strong action verbs ATS systems look for
    private static final List<String> ACTION_VERBS = List.of(
        "built", "designed", "developed", "implemented", "created", "led",
        "managed", "optimized", "improved", "reduced", "increased", "deployed",
        "integrated", "automated", "architected", "delivered", "launched",
        "collaborated", "mentored", "researched", "analyzed", "solved",
        "maintained", "migrated", "refactored", "tested", "validated",
        "configured", "documented", "contributed", "engineered", "streamlined"
    );

    // Common words to ignore when extracting JD keywords
    private static final Set<String> STOP_WORDS = Set.of(
        "the","and","or","of","to","in","a","an","with","for","is","are","be",
        "will","you","we","our","this","that","your","have","on","at","as","by",
        "from","can","not","it","its","which","who","they","their","all","any",
        "has","had","was","were","been","being","do","does","did","may","might",
        "would","should","could","shall","need","must","use","using","used",
        "work","working","strong","good","excellent","required","preferred",
        "experience","years","year","ability","skills","knowledge","understanding",
        "minimum","background","familiarity","proficiency"
    );

    /**
     * Main entry point: analyse the resume and return a full AtsResponse.
     */
    public AtsResponse analyze(AtsRequest request) {
        String resume = request.getResumeText().toLowerCase();
        String jd     = request.getJobDescription() != null ? request.getJobDescription().toLowerCase() : "";
        String role   = resolveRole(request.getTargetRole());

        KeywordBank.RoleConfig config = KeywordBank.ROLES.get(role);
        List<String> targetKeywords   = buildTargetKeywords(jd, config.keywords());

        // --- individual dimension scores ---
        KeywordResult kwResult   = scoreKeywords(resume, targetKeywords);
        int keywordScore         = kwResult.score();
        int quantScore           = scoreQuantifiedAchievements(resume);
        int sectionScore         = scoreSections(resume);
        int verbScore            = scoreActionVerbs(resume);
        int formatScore          = scoreFormat(resume);
        int lengthScore          = scoreLengthDetail(resume);

        // --- weighted overall ---
        int overall = (int) Math.round(
            keywordScore * 0.30 +
            quantScore   * 0.20 +
            sectionScore * 0.20 +
            verbScore    * 0.15 +
            formatScore  * 0.08 +
            lengthScore  * 0.07
        );
        overall = Math.min(99, Math.max(10, overall));

        // --- build response ---
        return AtsResponse.builder()
            .overallScore(overall)
            .scoreLabel(getLabel(overall))
            .scoreColor(getLabelColor(overall))
            .breakdown(buildBreakdown(keywordScore, quantScore, sectionScore, verbScore, formatScore, lengthScore))
            .keywordsFound(kwResult.found().size())
            .keywordsTotal(targetKeywords.size())
            .actionVerbsCount(countActionVerbs(resume))
            .wordCount(countWords(resume))
            .foundKeywords(kwResult.found())
            .missingKeywords(kwResult.missing().stream().limit(20).toList())
            .feedback(buildFeedback(resume, keywordScore, quantScore, sectionScore, verbScore))
            .targetRole(role)
            .roleLabel(config.label())
            .build();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // SCORING METHODS
    // ──────────────────────────────────────────────────────────────────────────

    private KeywordResult scoreKeywords(String resume, List<String> keywords) {
        List<String> found   = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String kw : keywords) {
            if (containsKeyword(resume, kw)) found.add(kw);
            else missing.add(kw);
        }
        int score = keywords.isEmpty() ? 0 : (int) Math.round((found.size() * 100.0) / keywords.size());
        return new KeywordResult(Math.min(100, score), found, missing);
    }

    private int scoreQuantifiedAchievements(String resume) {
        // Look for numbers with % or followed by metrics-adjacent words
        Pattern numPattern = Pattern.compile("\\d+\\s*(%|x|\\+|k|m|b| percent| users| records| endpoints| modules| apis| services| products|ms|gb|tb)", Pattern.CASE_INSENSITIVE);
        long matches = numPattern.matcher(resume).results().count();
        if (matches >= 4) return 100;
        if (matches == 3) return 80;
        if (matches == 2) return 60;
        if (matches == 1) return 40;
        return 10;
    }

    private int scoreSections(String resume) {
        boolean hasObjective  = matches(resume, "objective|summary|profile|about");
        boolean hasSkills     = matches(resume, "skills|technologies|tech stack|technical");
        boolean hasEducation  = matches(resume, "education|bachelor|master|degree|university|college|b\\.?tech|b\\.?e\\b|bca|bvoc");
        boolean hasProjects   = matches(resume, "project");
        boolean hasContact    = matches(resume, "email|@|phone|\\+91|\\+1|linkedin|github");
        boolean hasCerts      = matches(resume, "certification|certified|course|training");

        int count = (hasObjective?1:0) + (hasSkills?1:0) + (hasEducation?1:0)
                  + (hasProjects?1:0) + (hasContact?1:0) + (hasCerts?1:0);
        return Math.min(100, count * 17);
    }

    private int scoreActionVerbs(String resume) {
        long count = countActionVerbs(resume);
        if (count >= 8) return 100;
        if (count >= 6) return 85;
        if (count >= 4) return 70;
        if (count >= 2) return 50;
        if (count == 1) return 30;
        return 10;
    }

    private int scoreFormat(String resume) {
        int len = resume.length();
        if (len > 200 && len < 6000) return 90;
        if (len > 100) return 65;
        return 40;
    }

    private int scoreLengthDetail(String resume) {
        int words = countWords(resume);
        if (words >= 200 && words <= 700) return 95;
        if (words >= 120) return 75;
        if (words >= 60)  return 55;
        return 30;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // FEEDBACK BUILDER
    // ──────────────────────────────────────────────────────────────────────────

    private List<FeedbackItem> buildFeedback(String resume, int kwScore, int quantScore, int sectionScore, int verbScore) {
        List<FeedbackItem> items = new ArrayList<>();
        long verbs = countActionVerbs(resume);

        if (quantScore < 50)
            items.add(new FeedbackItem("error",
                "No measurable results found. Add numbers: \"built 5 endpoints\", \"managed 200+ records\", \"reduced load time by 30%\"."));

        if (kwScore < 60)
            items.add(new FeedbackItem("error",
                "Low keyword match (" + kwScore + "%). Add missing skills from the keyword list to your Skills section."));

        if (verbs < 3)
            items.add(new FeedbackItem("warn",
                "Only " + verbs + " strong action verb(s) detected. Use: Built, Designed, Implemented, Integrated, Optimized."));

        if (!matches(resume, "objective|summary|profile"))
            items.add(new FeedbackItem("warn",
                "No objective or summary section detected. Add a 2–3 line keyword-rich summary at the top."));

        if (!matches(resume, "email|@|phone|\\+91|\\+1|linkedin|github"))
            items.add(new FeedbackItem("warn",
                "Contact details look incomplete. Ensure email, phone, LinkedIn, and GitHub are present."));

        if (countWords(resume) < 150)
            items.add(new FeedbackItem("warn",
                "Resume seems short (" + countWords(resume) + " words). Add more detail to projects and experience."));

        if (countWords(resume) > 800)
            items.add(new FeedbackItem("warn",
                "Resume may be too long (" + countWords(resume) + " words). Aim for 300–600 words for a fresher profile."));

        // Positive feedback
        if (matches(resume, "skills|technologies|tech stack"))
            items.add(new FeedbackItem("good",
                "Skills section detected with clear categorization — good ATS parseability."));

        if (matches(resume, "project"))
            items.add(new FeedbackItem("good",
                "Project experience found — great for freshers. Keep the tech stack clearly listed per project."));

        if (verbs >= 4)
            items.add(new FeedbackItem("good",
                verbs + " strong action verbs detected. Keep leading every bullet with an action verb."));

        if (matches(resume, "education|bachelor|degree|university|college"))
            items.add(new FeedbackItem("good",
                "Education section detected. Ensure institution name, degree, and graduation year are correct."));

        return items;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // BREAKDOWN BUILDER
    // ──────────────────────────────────────────────────────────────────────────

    private List<ScoreBreakdown> buildBreakdown(int kw, int quant, int section, int verb, int format, int length) {
        return List.of(
            new ScoreBreakdown("Keyword Match",            kw,      barColor(kw),      status(kw)),
            new ScoreBreakdown("Quantified Achievements",  quant,   barColor(quant),   status(quant)),
            new ScoreBreakdown("Section Completeness",     section, barColor(section), status(section)),
            new ScoreBreakdown("Action Verbs & Impact",    verb,    barColor(verb),    status(verb)),
            new ScoreBreakdown("Formatting",               format,  barColor(format),  status(format)),
            new ScoreBreakdown("Length & Detail",          length,  barColor(length),  status(length))
        );
    }

    // ──────────────────────────────────────────────────────────────────────────
    // UTILITY
    // ──────────────────────────────────────────────────────────────────────────

    private List<String> buildTargetKeywords(String jd, List<String> roleKeywords) {
        if (jd.isBlank()) return roleKeywords;

        // Extract high-frequency content words from the JD
        Map<String, Long> freq = Arrays.stream(jd.split("[^a-z0-9]+"))
            .filter(w -> w.length() > 3 && !STOP_WORDS.contains(w))
            .collect(Collectors.groupingBy(w -> w, Collectors.counting()));

        List<String> jdKeywords = freq.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .map(Map.Entry::getKey)
            .limit(20)
            .toList();

        // Merge JD keywords with top role keywords (deduplicated)
        Set<String> merged = new LinkedHashSet<>(jdKeywords);
        merged.addAll(roleKeywords.subList(0, Math.min(10, roleKeywords.size())));
        return new ArrayList<>(merged).subList(0, Math.min(28, merged.size()));
    }

    private boolean containsKeyword(String text, String keyword) {
        String escaped = Pattern.quote(keyword.toLowerCase());
        return Pattern.compile("\\b" + escaped + "\\b").matcher(text).find();
    }

    private boolean matches(String text, String regex) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text).find();
    }

    private long countActionVerbs(String resume) {
        return ACTION_VERBS.stream()
            .filter(v -> containsKeyword(resume, v))
            .count();
    }

    private int countWords(String text) {
        return text.isBlank() ? 0 : text.trim().split("\\s+").length;
    }

    private String resolveRole(String role) {
        if (role == null || !KeywordBank.ROLES.containsKey(role.toLowerCase())) return "backend";
        return role.toLowerCase();
    }

    private String getLabel(int score) {
        if (score >= 85) return "Excellent";
        if (score >= 70) return "Good — ATS Ready";
        if (score >= 50) return "Needs Work";
        return "Low — Needs Improvement";
    }

    private String getLabelColor(int score) {
        if (score >= 85) return "#4dffc3";
        if (score >= 70) return "#7c6fff";
        if (score >= 50) return "#ffb347";
        return "#ff6b6b";
    }

    private String barColor(int score) {
        if (score >= 70) return "#4dffc3";
        if (score >= 40) return "#ffb347";
        return "#ff6b6b";
    }

    private String status(int score) {
        if (score >= 70) return "good";
        if (score >= 40) return "warn";
        return "poor";
    }

    // ──────────────────────────────────────────────────────────────────────────
    // INNER RECORD
    // ──────────────────────────────────────────────────────────────────────────

    private record KeywordResult(int score, List<String> found, List<String> missing) {}
}
