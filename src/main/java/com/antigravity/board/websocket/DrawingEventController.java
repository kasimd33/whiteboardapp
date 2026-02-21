package com.antigravity.board.websocket;

import com.antigravity.board.dto.DrawingEventDto;
import com.antigravity.board.entity.DrawingEvent;
import com.antigravity.board.repository.DrawingEventRepository;
import com.antigravity.board.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class DrawingEventController {

    private final SimpMessagingTemplate messagingTemplate;
    private final DrawingEventRepository drawingEventRepository;

    @MessageMapping("/board/{boardId}/draw")
    public void handleDrawingEvent(@DestinationVariable String boardId, @Payload DrawingEventDto dto,
                                    Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return;
        }

        dto.setBoardId(boardId);
        dto.setUserId(principal.getUserId().toString());

        if ("clear".equals(dto.getDrawingType())) {
            messagingTemplate.convertAndSend("/topic/board/" + boardId, dto);
            return;
        }

        // Persist event
        Map<String, Object> payload = new HashMap<>();
        if (dto.getCoordinates() != null) payload.putAll(dto.getCoordinates());
        if (dto.getColor() != null) payload.put("color", dto.getColor());
        if (dto.getStrokeWidth() != null) payload.put("strokeWidth", dto.getStrokeWidth());

        DrawingEvent event = DrawingEvent.builder()
                .boardId(UUID.fromString(boardId))
                .userId(principal.getUserId())
                .drawingType(dto.getDrawingType())
                .payload(payload)
                .build();
        drawingEventRepository.save(event);

        // Broadcast to all subscribers
        messagingTemplate.convertAndSend("/topic/board/" + boardId, dto);
    }
}
