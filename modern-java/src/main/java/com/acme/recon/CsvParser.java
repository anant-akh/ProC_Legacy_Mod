package com.acme.recon;

import java.util.List;

public final class CsvParser {

    private CsvParser() {
    }

    public static List<String> splitLine(String line) {
        if (line == null || line.isBlank()) {
            return List.of();
        }
        String[] parts = line.split(",", -1);
        return List.of(parts).stream()
                .map(String::strip)
                .toList();
    }
}
