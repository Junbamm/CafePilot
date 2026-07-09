package com.cafepilot.domain.menu.exception;

import com.cafepilot.global.exception.BusinessException;
import com.cafepilot.global.exception.ErrorCode;

public class MenuException extends BusinessException {

    public MenuException(ErrorCode errorCode) {
        super(errorCode);
    }
}
