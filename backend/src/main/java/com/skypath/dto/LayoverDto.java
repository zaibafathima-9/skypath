package com.skypath.dto;

public record LayoverDto(
        String airport,
        long durationMinutes,
        String connectionType
) {
}
