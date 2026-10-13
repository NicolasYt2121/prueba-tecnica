package com.nexo.banking.service;
public class BusinessException extends RuntimeException {
    public enum Code { NOT_FOUND, CONFLICT, INVALID }
    public final Code code;
    public BusinessException(Code code,String message) { super(message); this.code=code; }
    public static BusinessException notFound(String resource) { return new BusinessException(Code.NOT_FOUND,resource+" no encontrado"); }
    public static BusinessException conflict(String message) { return new BusinessException(Code.CONFLICT,message); }
    public static BusinessException invalid(String message) { return new BusinessException(Code.INVALID,message); }
}
