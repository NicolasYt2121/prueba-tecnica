package com.nexo.banking.service;
public class BusinessException extends RuntimeException {
    public final int status;
    public BusinessException(int status,String message) { super(message); this.status=status; }
    public static BusinessException notFound(String resource) { return new BusinessException(404,resource+" no encontrado"); }
    public static BusinessException conflict(String message) { return new BusinessException(409,message); }
    public static BusinessException invalid(String message) { return new BusinessException(422,message); }
}
