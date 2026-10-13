package com.nexo.banking.entity;

public final class Types {
    private Types() {}
    public enum AccountType { AHORROS, CORRIENTE }
    public enum AccountStatus { ACTIVA, INACTIVA, CANCELADA }
    public enum TransactionType { CONSIGNACION, RETIRO, TRANSFERENCIA }
    public enum IdentificationType { CC, CE, PASAPORTE }
}
