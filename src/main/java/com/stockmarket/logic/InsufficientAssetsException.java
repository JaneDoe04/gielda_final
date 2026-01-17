package com.stockmarket.logic;

public class InsufficientAssetsException extends RuntimeException {
    public InsufficientAssetsException(String message) {
        super(message);
    }
}
