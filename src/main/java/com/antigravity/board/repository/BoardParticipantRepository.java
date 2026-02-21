package com.antigravity.board.repository;

import com.antigravity.board.entity.BoardParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BoardParticipantRepository extends JpaRepository<BoardParticipant, UUID> {

    Optional<BoardParticipant> findByBoardIdAndUserId(UUID boardId, UUID userId);

    boolean existsByBoardIdAndUserId(UUID boardId, UUID userId);
}
