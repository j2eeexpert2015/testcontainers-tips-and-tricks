-- MySQL sample data
INSERT INTO customers (name, email) VALUES 
('MySQL User', 'mysqluser@example.com'),
('DB Manager', 'manager@example.com');

INSERT INTO orders (customer_id, amount) VALUES
(1, 88.95),
(2, 245.00);

INSERT INTO temporal_data (event_time) VALUES
(NOW(6)),
(DATE_ADD(NOW(6), INTERVAL 1 HOUR));