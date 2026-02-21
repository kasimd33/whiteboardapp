package com.antigravity.board.service;

import com.antigravity.board.dto.BoardRequest;
import com.antigravity.board.dto.BoardResponse;
import com.antigravity.board.entity.Board;
import com.antigravity.board.entity.BoardParticipant;
import com.antigravity.board.entity.User;
import com.antigravity.board.repository.BoardParticipantRepository;
import com.antigravity.board.repository.BoardRepository;
import com.antigravity.board.repository.UserRepository;
import com.antigravity.board.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardParticipantRepository participantRepository;
    private final UserRepository userRepository;

    @Transactional
    public BoardResponse createBoard(BoardRequest request) {
        UserPrincipal principal = getCurrentUser();
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Board board = Board.builder()
                .name(request.getName())
                .host(user)
                .build();

        board = boardRepository.save(board);

        // Add host as participant
        BoardParticipant participant = BoardParticipant.builder()
                .board(board)
                .user(user)
                .build();
        participantRepository.save(participant);

        return toResponse(board);
    }

    public BoardResponse getBoard(UUID boardId) {
        UserPrincipal principal = getCurrentUser();
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("Board not found: " + boardId));

        // Check if user is participant or host
        if (!board.getHost().getId().equals(principal.getUserId()) &&
                !participantRepository.existsByBoardIdAndUserId(boardId, principal.getUserId())) {
            throw new AccessDeniedException("You are not a participant of this board");
        }

        return toResponse(board);
    }

    @Transactional
    public void deleteBoard(UUID boardId) {
        UserPrincipal principal = getCurrentUser();
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("Board not found: " + boardId));

        if (!board.getHost().getId().equals(principal.getUserId())) {
            throw new AccessDeniedException("Only the host can delete the board");
        }

        boardRepository.delete(board);
    }

    @Transactional
    public BoardResponse joinBoard(UUID boardId) {
        UserPrincipal principal = getCurrentUser();
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("Board not found: " + boardId));

        if (participantRepository.existsByBoardIdAndUserId(boardId, principal.getUserId())) {
            return toResponse(board); // Already a participant
        }

        BoardParticipant participant = BoardParticipant.builder()
                .board(board)
                .user(user)
                .build();
        participantRepository.save(participant);

        return toResponse(board);
    }

    private UserPrincipal getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal)) {
            throw new AccessDeniedException("Authentication required");
        }
        return (UserPrincipal) auth.getPrincipal();
    }

    private BoardResponse toResponse(Board board) {
        return BoardResponse.builder()
                .id(board.getId().toString())
                .name(board.getName())
                .hostId(board.getHost().getId().toString())
                .hostUsername(board.getHost().getUsername())
                .createdAt(board.getCreatedAt())
                .build();
    }
}
