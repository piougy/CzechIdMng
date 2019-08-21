-- create database
CREATE DATABASE bcv_idm_storage;
GO
ALTER DATABASE bcv_idm_storage SET READ_COMMITTED_SNAPSHOT ON;
GO
ALTER DATABASE bcv_idm_storage SET ALLOW_SNAPSHOT_ISOLATION ON;
GO
-- set active database
USE bcv_idm_storage;
GO
-- create login, login is used for connect to server, check policy is for development prupose (password idmadmin)
CREATE LOGIN idmadmin WITH PASSWORD = 'idmadmin', check_policy = off;
GO
-- create user, user will be used for connect to database
CREATE USER idmadmin FOR LOGIN idmadmin;
GO
-- create schema
CREATE SCHEMA bcv_idm_storage AUTHORIZATION idmadmin;
GO
-- set default schema
ALTER USER idmadmin WITH DEFAULT_SCHEMA = bcv_idm_storage;
GO
-- grant permision for schema (grant all is deprecated)
GRANT ALTER, CONTROL, CREATE SEQUENCE, DELETE, EXECUTE, INSERT, REFERENCES, SELECT, TAKE OWNERSHIP, UPDATE, VIEW CHANGE TRACKING, VIEW DEFINITION ON SCHEMA::bcv_idm_storage TO idmadmin;
GO
-- grant create table to idmadmin
GRANT CREATE TABLE TO idmadmin;
GO
-- grant create view to idmadmin
GRANT CREATE VIEW TO idmadmin;
GO
-- CREATE required tables for ACC tests

CREATE TABLE bcv_idm_storage.test_contract_resource (
	ID nvarchar(255) NOT NULL,
	NAME nvarchar(255),
	STATE nvarchar(255),
	DISABLED nvarchar(255),
	DESCRIPTION nvarchar(255),
	VALIDFROM date,
	VALIDTILL date,
	LEADER nvarchar(255),
	MAIN nvarchar(255),
	OWNER nvarchar(255),
	WORKPOSITION nvarchar(255),
	MODIFIED datetime2,
	POSITIONS nvarchar(255),
	CONSTRAINT test_contract_resource_pkey PRIMARY KEY (ID)
);

CREATE TABLE bcv_idm_storage.test_contract_slice_resource (
	ID nvarchar(255) NOT NULL,
	NAME nvarchar(255),
	STATE nvarchar(255),
	DISABLED nvarchar(255),
	DESCRIPTION nvarchar(255),
	VALIDFROM date,
	VALIDTILL date,
	LEADER nvarchar(255),
	MAIN nvarchar(255),
	OWNER nvarchar(255),
	WORKPOSITION nvarchar(255),
	MODIFIED datetime2,
	VALIDFROM_SLICE date,
	CONTRACT_CODE nvarchar(255),
	CONSTRAINT test_contract_slice_resource_pkey PRIMARY KEY (ID)
);

CREATE TABLE bcv_idm_storage.test_resource (
	NAME nvarchar(255) NOT NULL,
	LASTNAME nvarchar(255),
	FIRSTNAME nvarchar(255),
	PASSWORD nvarchar(255),
	EMAIL nvarchar(255),
	DESCRIP nvarchar(2000),
	STATUS nvarchar(255),
	EAV_ATTRIBUTE nvarchar(255),
	MODIFIED datetime2,
	CONSTRAINT test_resource_pkey PRIMARY KEY (NAME)
);

CREATE TABLE bcv_idm_storage.test_role_resource (
	NAME nvarchar(255) NOT NULL,
	TYPE nvarchar(255),
	PRIORITY int,
	DESCRIPTION nvarchar(255),
	APPROVE_REMOVE bit NOT NULL,
	STATUS nvarchar(255),
	MODIFIED datetime2,
	EAV_ATTRIBUTE nvarchar(255),
	MEMBER nvarchar(2000),
	CONSTRAINT test_role_resource_pkey PRIMARY KEY (NAME)
);

CREATE TABLE bcv_idm_storage.test_schema_resource (
	NAME nvarchar(255) NOT NULL,
	STRING_VALUE nvarchar(MAX),
	SHORT_TEXT_VALUE nvarchar(2000),
	BOOLEAN_VALUE bit,
	LONG_VALUE BIGINT,
	INT_VALUE INT,
	DOUBLE_VALUE numeric(38,4),
	DATE_VALUE datetime2,
	BYTE_VALUE varbinary(255),
	UUID_VALUE binary(16),
	CONSTRAINT test_schema_resource_pkey PRIMARY KEY (NAME)
);

CREATE TABLE bcv_idm_storage.test_tree_resource (
	ID nvarchar(255) NOT NULL,
	CODE nvarchar(255),
	PARENT nvarchar(255),
	NAME nvarchar(255),
	EMAIL nvarchar(255),
	DESCRIPT nvarchar(255),
	STATUS nvarchar(255),
	MODIFIED datetime2,
	CONSTRAINT test_tree_resource_pkey PRIMARY KEY (ID)
);

GO
