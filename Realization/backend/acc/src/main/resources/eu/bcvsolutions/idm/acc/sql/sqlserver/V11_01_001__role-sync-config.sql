--
-- CzechIdM 11.1.0 Flyway script 
-- BCV solutions s.r.o.
--
-- Specific configuration for a role sync.

CREATE TABLE sys_sync_role_config
(
    assign_catalogue_switch     bit        NOT NULL,
    assign_role_remove_switch   bit        NOT NULL,
    assign_role_switch          bit        NOT NULL,
    forward_acm_switch          bit        NOT NULL,
    membership_switch           bit        NOT NULL,
    remove_catalogue_switch     bit        NOT NULL,
    skip_value_if_ex_switch     bit        NOT NULL,
    id                          binary(16) NOT NULL,
    main_catalogue_node         binary(16) NULL,
    member_identifier_attribute binary(16) NULL,
    members_of_attribute        binary(16) NULL,
    member_sys_mapping          binary(16) NULL,
    remove_catalogue_parent     binary(16) NULL,
    CONSTRAINT PK__sys_sync__3213E83FC74DC96B PRIMARY KEY (id),
    CONSTRAINT FKc07ywlx9k3441kmcailrjqgps FOREIGN KEY (id) REFERENCES sys_sync_config (id)
);
CREATE INDEX idx_sys_s_role_m_cat_n ON sys_sync_role_config (main_catalogue_node);
CREATE INDEX idx_sys_s_role_m_id_att ON sys_sync_role_config (member_identifier_attribute);
CREATE INDEX idx_sys_s_role_m_of_att ON sys_sync_role_config (members_of_attribute);
CREATE INDEX idx_sys_s_role_m_s_map ON sys_sync_role_config (member_sys_mapping);
CREATE INDEX idx_sys_s_role_r_cat_p ON sys_sync_role_config (remove_catalogue_parent);


CREATE TABLE sys_sync_role_config_a
(
    id                                  binary(16)     NOT NULL,
    rev                                 numeric(19, 0) NOT NULL,
    assign_catalogue_switch             bit            NULL,
    assign_catalogue_switch_m           bit            NULL,
    assign_role_remove_switch           bit            NULL,
    assign_role_remove_switch_m         bit            NULL,
    assign_role_switch                  bit            NULL,
    assign_role_switch_m                bit            NULL,
    forward_acm_switch                  bit            NULL,
    forward_acm_switch_m                bit            NULL,
    membership_switch                   bit            NULL,
    membership_switch_m                 bit            NULL,
    remove_catalogue_switch             bit            NULL,
    remove_catalogue_role_switch_m      bit            NULL,
    skip_value_if_ex_switch             bit            NULL,
    skip_value_if_excluded_switch_m     bit            NULL,
    main_catalogue_node                 binary(16)     NULL,
    main_catalogue_role_node_m          bit            NULL,
    member_identifier_attribute         binary(16)     NULL,
    member_identifier_attribute_m       bit            NULL,
    members_of_attribute                binary(16)     NULL,
    member_of_attribute_m               bit            NULL,
    member_sys_mapping                  binary(16)     NULL,
    member_system_mapping_m             bit            NULL,
    remove_catalogue_parent             binary(16)     NULL,
    remove_catalogue_role_parent_node_m bit            NULL,
    CONSTRAINT PK__sys_sync__BE3894F9954D1A37 PRIMARY KEY (id, rev),
    CONSTRAINT FK9p9d0uyyg0qg2rmkj9ufp57pn FOREIGN KEY (id, rev) REFERENCES sys_sync_config_a (id, rev)
);

-- We need to insert data from abstract sync conf to the new sys_sync_role_config.

INSERT INTO sys_sync_role_config (id, assign_catalogue_switch, assign_role_remove_switch, assign_role_switch, forward_acm_switch, membership_switch, remove_catalogue_switch,
                                  skip_value_if_ex_switch)
SELECT c.id,
       0,
       0,
       0,
       0,
       0,
       0,
       0
FROM sys_sync_config c
WHERE c.id IN (
    SELECT sc.id
    FROM sys_system_mapping sm
             JOIN sys_sync_config sc ON (sm.id = sc.system_mapping_id)
    WHERE sm.entity_type = 'ROLE'
);
