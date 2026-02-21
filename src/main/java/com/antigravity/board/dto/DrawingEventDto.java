package com.antigravity.board.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrawingEventDto {

    private String boardId;
    private String userId;
    private String drawingType;
    private Map<String, Object> coordinates;
    private String color;
    private Double strokeWidth;
}
