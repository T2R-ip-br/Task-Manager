package com.example.taskManagementApplication.exception;

public class ExclusionTaskProcessing extends Exception{

    private final String textException;

    public ExclusionTaskProcessing(String textException) {
        this.textException = textException;
    }

    @Override
    public String toString() {
        return textException;
    }
}
