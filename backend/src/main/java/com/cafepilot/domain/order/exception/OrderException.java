package com.cafepilot.domain.order.exception;

import com.cafepilot.global.exception.BusinessException;
import com.cafepilot.global.exception.ErrorCode;

public class OrderException extends BusinessException {

    public OrderException(ErrorCode errorCode) {
        super(errorCode);
    }
}
