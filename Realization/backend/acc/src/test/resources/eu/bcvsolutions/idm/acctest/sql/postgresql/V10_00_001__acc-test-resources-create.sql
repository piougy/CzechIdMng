--
-- CzechIdM 10.0.0 Flyway script
-- BCV solutions s.r.o.
--
-- Test resources for tests with flyway enabled

CREATE TABLE test_contract_resource (
	id varchar(255) NOT NULL,
	description varchar(255) NULL,
	disabled varchar(255) NULL,
	leader varchar(255) NULL,
	main varchar(255) NULL,
	modified timestamp NULL,
	name varchar(255) NULL,
	"owner" varchar(255) NULL,
	positions varchar(255) NULL,
	state varchar(255) NULL,
	validfrom date NULL,
	validtill date NULL,
	workposition varchar(255) NULL,
	CONSTRAINT test_contract_resource_pkey PRIMARY KEY (id)
);

CREATE TABLE test_contract_slice_resource (
	id varchar(255) NOT NULL,
	contract_code varchar(255) NULL,
	description varchar(255) NULL,
	disabled varchar(255) NULL,
	leader varchar(255) NULL,
	main varchar(255) NULL,
	modified timestamp NULL,
	name varchar(255) NULL,
	"owner" varchar(255) NULL,
	state varchar(255) NULL,
	validfrom date NULL,
	validfrom_slice date NULL,
	validtill date NULL,
	workposition varchar(255) NULL,
	CONSTRAINT test_contract_slice_resource_pkey PRIMARY KEY (id)
);

CREATE TABLE test_resource (
	name varchar(255) NOT NULL,
	descrip varchar(2000) NULL,
	eav_attribute varchar(255) NULL,
	email varchar(255) NULL,
	firstname varchar(255) NULL,
	lastname varchar(255) NULL,
	modified timestamp NULL,
	"password" varchar(255) NULL,
	status varchar(255) NULL,
	CONSTRAINT test_resource_pkey PRIMARY KEY (name)
);

CREATE TABLE test_role_resource (
	name varchar(255) NOT NULL,
	approve_remove bool NULL,
	description varchar(255) NULL,
	eav_attribute varchar(255) NULL,
	"member" varchar(2000) NULL,
	modified timestamp NULL,
	priority int4 NULL,
	status varchar(255) NULL,
	"type" varchar(255) NULL,
	CONSTRAINT test_role_resource_pkey PRIMARY KEY (name)
);

CREATE TABLE test_schema_resource (
	name varchar(255) NOT NULL,
	boolean_value bool NULL,
	byte_value bytea NULL,
	date_value timestamp NULL,
	double_value numeric(38,4) NULL,
	int_value int4 NULL,
	long_value int8 NULL,
	short_text_value varchar(2000) NULL,
	string_value text NULL,
	uuid_value bytea NULL,
	CONSTRAINT test_schema_resource_pkey PRIMARY KEY (name)
);

CREATE TABLE test_tree_resource (
	id varchar(255) NOT NULL,
	code varchar(255) NULL,
	descript varchar(255) NULL,
	email varchar(255) NULL,
	modified timestamp NULL,
	name varchar(255) NULL,
	parent varchar(255) NULL,
	status varchar(255) NULL,
	CONSTRAINT test_tree_resource_pkey PRIMARY KEY (id)
);
