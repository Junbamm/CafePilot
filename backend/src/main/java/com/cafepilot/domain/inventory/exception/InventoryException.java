package com.cafepilot.domain.inventory.exception;

import com.cafepilot.global.exception.BusinessException;
import com.cafepilot.global.exception.ErrorCode;

public class InventoryException extends BusinessException {

    public InventoryException(ErrorCode errorCode) {
        super(errorCode);
    }
}
