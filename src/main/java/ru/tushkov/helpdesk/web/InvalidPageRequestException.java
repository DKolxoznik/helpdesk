package ru.tushkov.helpdesk.web;

public class InvalidPageRequestException extends RuntimeException {

    public InvalidPageRequestException() {
        super("Параметры page и size вне допустимого диапазона: page >= 0, size от 1 до 100");
    }
}
