package com.antigravity.board.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardResponse {

    private String id;
    private String name;
    private String hostId;
    private String hostUsername;
    private Instant createdAt;
}
