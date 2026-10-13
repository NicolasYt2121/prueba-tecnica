package com.nexo.banking.repository;
import com.nexo.banking.application.port.out.AccountNumberGenerator;
import com.nexo.banking.domain.model.Types.AccountType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
@Component
public class PostgresAccountNumberGenerator implements AccountNumberGenerator {
    private final JdbcTemplate jdbc;
    public PostgresAccountNumberGenerator(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    public String next(AccountType type) {
        Long value=jdbc.queryForObject("select nextval('account_number_seq')",Long.class);
        return (type==AccountType.AHORROS ? "53" : "33") + String.format(java.util.Locale.ROOT,"%08d",value);
    }
}
