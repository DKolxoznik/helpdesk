package ru.tushkov.helpdesk.category;

import java.util.UUID;

public record CategoryResponse(UUID id, String code, String name) {}
