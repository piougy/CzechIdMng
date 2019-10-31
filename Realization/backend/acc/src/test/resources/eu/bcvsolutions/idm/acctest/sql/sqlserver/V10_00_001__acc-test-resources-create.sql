--
-- CzechIdM 10.0.0 Flyway script
-- BCV solutions s.r.o.
--
-- Test resources for tests with flyway enabled

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
