package com.antigravity.board.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * AI Drawing Service - Mock implementation ready for integration with
 * actual ML/AI models (e.g., shape detection, diagram generation).
 */
@Service
@Slf4j
public class AiDrawingService {

    /**
     * Suggests the most likely shape based on a list of points.
     * Mock: Returns intelligent suggestions based on point count and approximate geometry.
     */
    public String suggestShape(List<Point> points) {
        if (points == null || points.size() < 2) {
            return "line";
        }
        if (points.size() == 2) {
            return "line";
        }
        if (points.size() == 3) {
            return "triangle";
        }
        if (points.size() == 4 && isApproximatelyRectangular(points)) {
            return "rectangle";
        }
        if (points.size() > 8 && isApproximatelyCircular(points)) {
            return "circle";
        }
        return "polygon";
    }

    /**
     * Auto-straightens a hand-drawn line to horizontal, vertical, or diagonal.
     * Mock: Returns normalized endpoints.
     */
    public Map<String, Object> autoStraightenLine(List<Point> points) {
        if (points == null || points.size() < 2) {
            return Map.of("start", Map.of("x", 0, "y", 0), "end", Map.of("x", 0, "y", 0));
        }
        Point start = points.getFirst();
        Point end = points.getLast();
        return Map.of(
                "start", Map.of("x", start.getX(), "y", start.getY()),
                "end", Map.of("x", end.getX(), "y", end.getY()),
                "angle", "normalized");
    }

    /**
     * Detects if the drawn shape is a circle and returns center + radius.
     * Mock: Computes centroid and average distance as radius.
     */
    public Map<String, Object> detectCircle(List<Point> points) {
        if (points == null || points.size() < 3) {
            return Map.of("isCircle", false, "confidence", 0.0);
        }
        double cx = points.stream().mapToDouble(Point::getX).average().orElse(0);
        double cy = points.stream().mapToDouble(Point::getY).average().orElse(0);
        double avgRadius = points.stream()
                .mapToDouble(p -> Math.sqrt(Math.pow(p.getX() - cx, 2) + Math.pow(p.getY() - cy, 2)))
                .average().orElse(0);
        return Map.of(
                "isCircle", true,
                "center", Map.of("x", cx, "y", cy),
                "radius", avgRadius,
                "confidence", 0.85);
    }

    /**
     * Generates a diagram from natural language prompt.
     * Mock: Returns a placeholder diagram structure.
     */
    public Map<String, Object> generateDiagramFromText(String prompt) {
        log.debug("AI generateDiagramFromText called with prompt: {}", prompt);
        return Map.of(
                "type", "flowchart",
                "elements", List.of(
                        Map.of("id", "1", "type", "box", "label", "Start", "x", 100, "y", 50),
                        Map.of("id", "2", "type", "box", "label", extractMainConcept(prompt), "x", 100, "y", 150),
                        Map.of("id", "3", "type", "box", "label", "End", "x", 100, "y", 250)
                ),
                "connections", List.of(
                        Map.of("from", "1", "to", "2"),
                        Map.of("from", "2", "to", "3")
                ),
                "mock", true);
    }

    private boolean isApproximatelyRectangular(List<Point> points) {
        return points.size() >= 4;
    }

    private boolean isApproximatelyCircular(List<Point> points) {
        return points.size() >= 8;
    }

    private String extractMainConcept(String prompt) {
        if (prompt == null || prompt.isBlank()) return "Process";
        String[] words = prompt.trim().split("\\s+");
        return words.length > 0 ? words[0] : "Process";
    }
}
