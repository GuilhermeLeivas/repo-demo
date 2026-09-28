package com.example.accounts;

class BusinessException extends RuntimeException {
    final String code;
    BusinessException(String code, String message) { super(message); this.code = code; }
}
