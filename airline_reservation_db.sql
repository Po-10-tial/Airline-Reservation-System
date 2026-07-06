-- ============================================================
-- Airline Reservation System - MySQL Database Setup
-- ============================================================
-- Run this script to create the database, tables, and seed data.
-- Usage: mysql -u root -p < airline_reservation_db.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS airline_reservation_db;
USE airline_reservation_db;

-- -----------------------------------------------------------
-- Flights table
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS flights (
    flight_number VARCHAR(20) PRIMARY KEY,
    origin VARCHAR(50) NOT NULL,
    destination VARCHAR(50) NOT NULL,
    departure_time VARCHAR(20) NOT NULL,
    arrival_time VARCHAR(20) NOT NULL,
    total_seats INT NOT NULL,
    available_seats INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL
);

-- -----------------------------------------------------------
-- Passengers table
-- Extended with passport, type, seat preference, travel class
-- to match the Java Passenger record.
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS passengers (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    passport_number VARCHAR(50) DEFAULT '',
    passenger_type VARCHAR(20) DEFAULT 'Adult',
    seat_preference VARCHAR(20) DEFAULT 'Any',
    travel_class VARCHAR(20) DEFAULT 'Economy'
);

-- -----------------------------------------------------------
-- Users table
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    phone VARCHAR(20),
    passenger_id VARCHAR(50),
    FOREIGN KEY (passenger_id) REFERENCES passengers(id) ON DELETE SET NULL
);

-- -----------------------------------------------------------
-- Reservations table
-- Extended with total_price, booked_by_user_id, payment_method
-- to match the Java Reservation record.
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS reservations (
    id VARCHAR(50) PRIMARY KEY,
    flight_number VARCHAR(20) NOT NULL,
    passenger_id VARCHAR(50) NOT NULL,
    seats INT NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    booked_by_user_id VARCHAR(50),
    payment_method VARCHAR(30) DEFAULT 'Unknown',
    FOREIGN KEY (flight_number) REFERENCES flights(flight_number) ON DELETE CASCADE,
    FOREIGN KEY (passenger_id) REFERENCES passengers(id) ON DELETE CASCADE
);

-- ============================================================
-- Seed Data
-- ============================================================

-- Default Admin Account
INSERT INTO users (id, name, email, password, role, phone, passenger_id)
VALUES ('U-ADMIN01', 'Administrator', 'admin@system.com', 'admin123', 'ADMIN', '0000000000', NULL)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Sample Flights (all 45 routes)
INSERT INTO flights (flight_number, origin, destination, departure_time, arrival_time, total_seats, available_seats, price) VALUES
('KE101', 'Nairobi',  'Mombasa',    '07:30', '08:45', 180, 180, 14500.00),
('KE102', 'Mombasa',  'Kisumu',     '09:15', '11:55', 160, 160, 19000.00),
('KE103', 'Nairobi',  'Kisumu',     '10:00', '11:30', 170, 170, 16000.00),
('KE104', 'Nairobi',  'Eldoret',    '08:45', '10:00', 150, 150, 12000.00),
('KE105', 'Nairobi',  'Nakuru',     '12:00', '13:00', 180, 180,  9000.00),
('KE106', 'Nairobi',  'Malindi',    '14:30', '15:50', 140, 140, 18000.00),
('KE107', 'Mombasa',  'Nyeri',      '06:45', '08:10', 150, 150, 17000.00),
('KE108', 'Nairobi',  'Meru',       '11:15', '12:35', 140, 140, 11500.00),
('KE109', 'Kisumu',   'Nakuru',     '13:00', '14:10', 150, 150,  9500.00),
('KE110', 'Kisumu',   'Kakamega',   '15:00', '16:15', 130, 130,  8500.00),
('KE111', 'Eldoret',  'Turkana',    '16:30', '18:15', 120, 120, 21000.00),
('KE112', 'Nairobi',  'Garissa',    '17:00', '18:40', 150, 150, 22000.00),
('KE113', 'Nairobi',  'Kitale',     '07:00', '08:10', 160, 160, 13000.00),
('KE114', 'Mombasa',  'Lamu',       '10:00', '11:00', 140, 140, 14000.00),
('KE115', 'Nairobi',  'Nyeri',      '09:30', '10:40', 150, 150, 10500.00),
('KE116', 'Nairobi',  'Embu',       '11:00', '12:05', 150, 150, 10800.00),
('KE117', 'Mombasa',  'Voi',        '12:30', '13:20', 140, 140,  9500.00),
('KE118', 'Nairobi',  'Thika',      '08:15', '09:00', 170, 170,  6800.00),
('KE119', 'Nairobi',  'Kitui',      '13:15', '14:25', 140, 140, 14500.00),
('KE120', 'Kisumu',   'Homa Bay',   '07:45', '08:40', 150, 150,  7600.00),
('KE121', 'Nairobi',  'Naivasha',   '14:00', '15:00', 160, 160,  8200.00),
('KE122', 'Nakuru',   'Kericho',    '09:20', '10:10', 140, 140,  7800.00),
('KE123', 'Eldoret',  'Kitale',     '11:45', '12:25', 150, 150,  6700.00),
('KE124', 'Nairobi',  'Mwingi',     '15:10', '16:30', 130, 130, 12800.00),
('KE125', 'Mombasa',  'Kwale',      '06:30', '07:10', 140, 140,  7200.00),
('KE126', 'Nairobi',  'Mandera',    '07:50', '09:45', 120, 120, 26000.00),
('KE127', 'Nairobi',  'Marsabit',   '10:30', '12:15', 130, 130, 23000.00),
('KE128', 'Nairobi',  'Isiolo',     '13:00', '14:20', 140, 140, 19000.00),
('KE129', 'Nairobi',  'Wajir',      '15:30', '17:10', 120, 120, 27000.00),
('KE130', 'Kisumu',   'Migori',     '12:00', '12:45', 140, 140,  7500.00),
('KE131', 'Nairobi',  'Bungoma',    '08:30', '09:50', 150, 150, 11300.00),
('KE132', 'Nairobi',  'Busia',      '10:00', '11:35', 140, 140, 15800.00),
('KE133', 'Nairobi',  'Kajiado',    '09:20', '10:10', 150, 150,  9000.00),
('KE134', 'Nairobi',  'Narok',      '11:00', '11:55', 150, 150,  9300.00),
('KE135', 'Nairobi',  'Nyahururu',  '13:45', '14:40', 140, 140,  9800.00),
('KE136', 'Mombasa',  'Malindi',    '16:00', '17:10', 140, 140,  9000.00),
('KE137', 'Nairobi',  'Kisii',      '07:20', '08:40', 150, 150, 12500.00),
('KE138', 'Nairobi',  'Murang''a',  '10:15', '11:10', 150, 150,  8700.00),
('KE139', 'Kisumu',   'Siaya',      '14:20', '15:05', 140, 140,  7800.00),
('KE140', 'Nairobi',  'Busia',      '16:15', '17:55', 140, 140, 16000.00),
('KE141', 'Nairobi',  'Lamu',       '18:00', '19:10', 130, 130, 19000.00),
('KE142', 'Nairobi',  'Malindi',    '06:30', '07:55', 150, 150, 17500.00),
('KE143', 'Nakuru',   'Nyahururu',  '12:30', '13:10', 140, 140,  7000.00),
('KE144', 'Nairobi',  'Emali',      '08:00', '08:55', 150, 150,  7200.00),
('KE145', 'Nairobi',  'Homabay',    '17:00', '18:30', 140, 140, 15000.00)
ON DUPLICATE KEY UPDATE origin = VALUES(origin);
