-- PostgreSQL sample data
INSERT INTO customers (name, email) VALUES 
('Postgres User', 'pguser@example.com'),
('DB Admin', 'admin@example.com');

INSERT INTO orders (customer_id, amount) VALUES
(1, 125.50),
(2, 327.99);

INSERT INTO json_data (data) VALUES
('{"system": "PostgreSQL", "version": 16, "features": ["JSONB"]}');