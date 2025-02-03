package toonpick.app.exception.exception;

import lombok.Getter;
import toonpick.app.exception.ErrorCode;

@Getter
public class CustomAuthenticationException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomAuthenticationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public CustomAuthenticationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}
