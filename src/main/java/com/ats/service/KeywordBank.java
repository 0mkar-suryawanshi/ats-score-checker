package com.ats.service;

import java.util.List;
import java.util.Map;

/**
 * Centralised keyword bank for each target role.
 * Add/remove keywords here to tune scoring accuracy.
 */
public class KeywordBank {

    public static final Map<String, RoleConfig> ROLES = Map.of(

        "backend", new RoleConfig("Backend Developer", List.of(
            "java", "spring boot", "spring", "rest api", "restful", "microservices",
            "mysql", "postgresql", "hibernate", "jpa", "spring data jpa",
            "maven", "gradle", "junit", "docker", "git", "sql", "jdbc",
            "servlet", "json", "http", "crud", "api", "backend", "database",
            "kotlin", "multithreading", "exception handling", "design patterns"
        )),

        "frontend", new RoleConfig("Frontend Developer", List.of(
            "react", "javascript", "typescript", "html", "css", "vue", "angular",
            "webpack", "npm", "responsive", "ui", "ux", "accessibility",
            "dom", "redux", "tailwind", "sass", "figma", "component",
            "state management", "hooks", "rest api", "git", "jest"
        )),

        "fullstack", new RoleConfig("Full Stack Developer", List.of(
            "react", "node", "javascript", "java", "spring", "rest api",
            "html", "css", "database", "sql", "git", "docker",
            "aws", "mongodb", "postgresql", "typescript", "agile",
            "microservices", "frontend", "backend", "ci/cd"
        )),

        "data", new RoleConfig("Data Engineer", List.of(
            "python", "sql", "spark", "hadoop", "etl", "pipeline",
            "kafka", "airflow", "aws", "gcp", "bigquery", "pandas",
            "numpy", "data warehouse", "machine learning", "analytics",
            "tableau", "power bi", "scala", "pyspark", "databricks"
        )),

        "devops", new RoleConfig("DevOps Engineer", List.of(
            "docker", "kubernetes", "ci/cd", "jenkins", "aws", "terraform",
            "ansible", "linux", "bash", "git", "prometheus", "grafana",
            "nginx", "helm", "pipeline", "cloud", "azure", "gcp",
            "infrastructure", "deployment", "monitoring", "scripting"
        )),

        "mobile", new RoleConfig("Mobile Developer", List.of(
            "android", "ios", "kotlin", "swift", "react native", "flutter",
            "dart", "java", "api", "firebase", "rest", "gradle",
            "xcode", "mobile", "app", "ui", "sdk", "mvvm",
            "retrofit", "coroutines", "jetpack compose"
        ))
    );

    public record RoleConfig(String label, List<String> keywords) {}
}
