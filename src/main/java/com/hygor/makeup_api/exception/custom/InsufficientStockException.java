package com.hygor.makeup_api.exception.custom;

public class InsufficientStockException extends BusinessException {
    public InsufficientStockException(String productName) {
        super("Estoque insuficiente para o produto: " + productName);
    }
}