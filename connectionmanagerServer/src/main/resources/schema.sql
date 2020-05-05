<!--DROP TABLE IF EXISTS system;-->

--CREATE TABLE customer (
--  id INT NOT NULL AUTO_INCREMENT,
--  name VARCHAR(100) NOT NULL,
--  startDate DATE NOT NULL,
--  location VARCHAR(100),
--  billingAddress VARCHAR(200),
--  PRIMARY KEY (id));
--
--CREATE TABLE gateway (
--  id INT NOT NULL AUTO_INCREMENT,
--  name VARCHAR(100) NOT NULL,
--  coordinates VARCHAR(100),
--  location VARCHAR(100),
--  CONSTRAINT `gateway_fk_1` FOREIGN KEY (`customerId`) REFERENCES `customer` (`customerId`));