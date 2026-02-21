package com.antigravity.board.controller;

import com.antigravity.board.dto.BoardRequest;
import com.antigravity.board.dto.BoardResponse;
import com.antigravity.board.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    public ResponseEntity<BoardResponse> createBoard(@Valid @RequestBody BoardRequest request) {
        BoardResponse response = boardService.createBoard(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardResponse> getBoard(@PathVariable String id) {
        BoardResponse response = boardService.getBoard(UUID.fromString(id));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable String id) {
        boardService.deleteBoard(UUID.fromString(id));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<BoardResponse> joinBoard(@PathVariable String id) {
        BoardResponse response = boardService.joinBoard(UUID.fromString(id));
        return ResponseEntity.ok(response);
    }
}
