package com.yetanotherx.xbot.exception;

/**
 * Exception for any type of command error. 
 * 
 */
public class CommandException extends Exception {
    private static final long serialVersionUID = 159875872L;

    public CommandException() {
        super();
    }

    public CommandException(String message) {
        super(message);
    }

    public CommandException(Throwable t) {
        super(t);
    }
}
