-- AntiGravityBoard - PostgreSQL Schema
-- Tables are auto-created by Hibernate with ddl-auto: update
-- This file documents the expected schema

-- users
-- id UUID PRIMARY KEY
-- username VARCHAR(255) UNIQUE NOT NULL
-- email VARCHAR(255) UNIQUE NOT NULL
-- password VARCHAR(255) NOT NULL
-- role VARCHAR(50) NOT NULL
-- created_at TIMESTAMP NOT NULL

-- boards
-- id UUID PRIMARY KEY
-- name VARCHAR(255) NOT NULL
-- host_id UUID REFERENCES users(id) NOT NULL
-- created_at TIMESTAMP NOT NULL

-- board_participants
-- id UUID PRIMARY KEY
-- board_id UUID REFERENCES boards(id) NOT NULL
-- user_id UUID REFERENCES users(id) NOT NULL
-- joined_at TIMESTAMP NOT NULL

-- drawing_events
-- id UUID PRIMARY KEY
-- board_id UUID NOT NULL
-- user_id UUID NOT NULL
-- drawing_type VARCHAR(50) NOT NULL
-- payload JSONB
-- created_at TIMESTAMP NOT NULL
