package ru.tushkov.helpdesk.web;

import java.util.UUID;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(UUID id) {
        super("Категория не найдена: " + id);
    }
}
