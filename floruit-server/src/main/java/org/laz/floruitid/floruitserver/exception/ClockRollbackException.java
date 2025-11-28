package org.laz.floruitid.floruitserver.exception;

public class ClockRollbackException extends RuntimeException {
    public ClockRollbackException(String message) {
        super(message);
    }

    public ClockRollbackException() {
    }
}
