package com.antigravity.board.controller;

import com.antigravity.board.ai.AiDrawingService;
import com.antigravity.board.ai.Point;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiDrawingService aiDrawingService;

    @PostMapping("/suggest-shape")
    public ResponseEntity<Map<String, String>> suggestShape(@RequestBody List<Point> points) {
        String shape = aiDrawingService.suggestShape(points);
        return ResponseEntity.ok(Map.of("suggestedShape", shape));
    }

    @PostMapping("/straighten-line")
    public ResponseEntity<Map<String, Object>> autoStraightenLine(@RequestBody List<Point> points) {
        Map<String, Object> result = aiDrawingService.autoStraightenLine(points);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/detect-circle")
    public ResponseEntity<Map<String, Object>> detectCircle(@RequestBody List<Point> points) {
        Map<String, Object> result = aiDrawingService.detectCircle(points);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/generate-diagram")
    public ResponseEntity<Map<String, Object>> generateDiagram(@RequestBody Map<String, String> request) {
        String prompt = request.getOrDefault("prompt", "");
        Map<String, Object> result = aiDrawingService.generateDiagramFromText(prompt);
        return ResponseEntity.ok(result);
    }
}
