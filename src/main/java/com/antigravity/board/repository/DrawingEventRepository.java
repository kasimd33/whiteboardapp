package com.antigravity.board.repository;

import com.antigravity.board.entity.DrawingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DrawingEventRepository extends JpaRepository<DrawingEvent, UUID> {

    List<DrawingEvent> findByBoardIdOrderByCreatedAtAsc(UUID boardId);
}
