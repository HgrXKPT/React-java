package com.higor.React_java.model.dto.output;

import java.util.UUID;

public record LoginResponse(
    UUID id,
    String email,
    String login
) {
} 
