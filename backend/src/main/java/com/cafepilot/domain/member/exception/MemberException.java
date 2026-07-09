package com.cafepilot.domain.member.exception;

import com.cafepilot.global.exception.BusinessException;
import com.cafepilot.global.exception.ErrorCode;

public class MemberException extends BusinessException {

    public MemberException(ErrorCode errorCode) {
        super(errorCode);
    }
}
