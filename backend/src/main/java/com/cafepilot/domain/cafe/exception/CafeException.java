package com.cafepilot.domain.cafe.exception;

import com.cafepilot.global.exception.BusinessException;
import com.cafepilot.global.exception.ErrorCode;

public class CafeException extends BusinessException {

    public CafeException(ErrorCode errorCode) {
        super(errorCode);
    }
}
