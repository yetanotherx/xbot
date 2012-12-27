package com.yetanotherx.xbot.exception;

/**
 * Transition exception, simply passes an exception on down to the next level
 * 
 */
public class WrappedCommandException extends CommandException {

    public WrappedCommandException(Throwable t) {
        super(t);
    }
}
