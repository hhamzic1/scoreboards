package com.hhamzic1.scoreboards.common.model;

import java.util.UUID;

public record Team(UUID id, String name) {

    public Team(String name) {
        this(UUID.randomUUID(), name);
    }
}