package com.antigravity.board.controller;

import com.antigravity.board.dto.DrawingEventDto;
import com.antigravity.board.entity.DrawingEvent;
import com.antigravity.board.repository.DrawingEventRepository;
import com.antigravity.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/boards/{boardId}/drawings")
@RequiredArgsConstructor
public class DrawingController {

    private final DrawingEventRepository drawingEventRepository;
    private final BoardService boardService;

    @GetMapping
    public ResponseEntity<List<DrawingEventDto>> getDrawingHistory(@PathVariable String boardId) {
        UUID id = UUID.fromString(boardId);
        // Validate access via BoardService
        boardService.getBoard(id);

        List<DrawingEvent> events = drawingEventRepository.findByBoardIdOrderByCreatedAtAsc(id);
        List<DrawingEventDto> dtos = events.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    private DrawingEventDto toDto(DrawingEvent event) {
        return DrawingEventDto.builder()
                .boardId(event.getBoardId().toString())
                .userId(event.getUserId().toString())
                .drawingType(event.getDrawingType())
                .coordinates(event.getPayload())
                .color(event.getPayload() != null && event.getPayload().containsKey("color")
                        ? String.valueOf(event.getPayload().get("color")) : null)
                .strokeWidth(event.getPayload() != null && event.getPayload().containsKey("strokeWidth")
                        ? (Double) event.getPayload().get("strokeWidth") : null)
                .build();
    }
}
