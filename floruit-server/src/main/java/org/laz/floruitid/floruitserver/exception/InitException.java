package org.laz.floruitid.floruitserver.exception;

/**
 * 应用初始化过程中异常
 */
public class InitException extends RuntimeException {
    public InitException(String message) {
        super(message);
    }
}
