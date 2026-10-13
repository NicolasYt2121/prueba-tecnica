-- Ejemplos DML para PostgreSQL. Ejecutar en una base de prueba.
-- No modificar saldos por SQL en una operación normal: usar el servicio transaccional.
BEGIN;
INSERT INTO customers (identification_type,identification_number,first_name,last_name,email,birth_date)
VALUES ('CC','DEMO9001','Laura','Ejemplo','laura@example.com','1990-01-15') RETURNING id;

SELECT id,first_name,last_name,created_at FROM customers WHERE identification_number='DEMO9001';
UPDATE customers SET email='laura.actualizada@example.com' WHERE identification_number='DEMO9001';
-- El trigger cambia updated_at automáticamente.
DELETE FROM customers WHERE identification_number='DEMO9001';
ROLLBACK; -- Los ejemplos no dejan datos.

-- Estado de cuenta y reconciliación (lectura):
SELECT a.number,a.balance,a.available_balance,COALESCE(SUM(m.delta),0) AS ledger_balance
FROM accounts a LEFT JOIN movements m ON m.account_id=a.id
GROUP BY a.id ORDER BY a.id;

-- DDL: ver backend/src/main/resources/db/migration/V1__banking_schema.sql.
-- Contiene CREATE TABLE, CREATE SEQUENCE, CREATE INDEX, funciones, triggers y restricciones.
