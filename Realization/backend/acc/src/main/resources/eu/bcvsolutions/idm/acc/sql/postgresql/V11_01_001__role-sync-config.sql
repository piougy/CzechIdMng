--
-- CzechIdM 11.1.0 Flyway script 
-- BCV solutions s.r.o.
--
-- Specific configuration for a role sync.

CREATE TABLE sys_sync_role_config
(
    assign_catalogue_switch     bool  NOT NULL,
    assign_role_remove_switch   bool  NOT NULL,
    assign_role_switch          bool  NOT NULL,
    forward_acm_switch          bool  NOT NULL,
    membership_switch           bool  NOT NULL,
    remove_catalogue_switch     bool  NOT NULL,
    skip_value_if_ex_switch     bool  NOT NULL,
    id                          bytea NOT NULL,
    main_catalogue_node         bytea NULL,
    member_identifier_attribute bytea NULL,
    members_of_attribute        bytea NULL,
    member_sys_mapping          bytea NULL,
    remove_catalogue_parent     bytea NULL,
    CONSTRAINT sys_sync_role_config_pkey PRIMARY KEY (id)
);
CREATE
INDEX idx_sys_s_role_m_cat_n ON sys_sync_role_config USING btree (main_catalogue_node);
CREATE
INDEX idx_sys_s_role_m_id_att ON sys_sync_role_config USING btree (member_identifier_attribute);
CREATE
INDEX idx_sys_s_role_m_of_att ON sys_sync_role_config USING btree (members_of_attribute);
CREATE
INDEX idx_sys_s_role_m_s_map ON sys_sync_role_config USING btree (member_sys_mapping);
CREATE
INDEX idx_sys_s_role_r_cat_p ON sys_sync_role_config USING btree (remove_catalogue_parent);


CREATE TABLE sys_sync_role_config_a
(
    id                                  bytea NOT NULL,
    rev                                 int8  NOT NULL,
    assign_catalogue_switch             bool NULL,
    assign_catalogue_switch_m           bool NULL,
    assign_role_remove_switch           bool NULL,
    assign_role_remove_switch_m         bool NULL,
    assign_role_switch                  bool NULL,
    assign_role_switch_m                bool NULL,
    forward_acm_switch                  bool NULL,
    forward_acm_switch_m                bool NULL,
    membership_switch                   bool NULL,
    membership_switch_m                 bool NULL,
    remove_catalogue_switch             bool NULL,
    remove_catalogue_role_switch_m      bool NULL,
    skip_value_if_ex_switch             bool NULL,
    skip_value_if_excluded_switch_m     bool NULL,
    main_catalogue_node                 bytea NULL,
    main_catalogue_role_node_m          bool NULL,
    member_identifier_attribute         bytea NULL,
    member_identifier_attribute_m       bool NULL,
    members_of_attribute                bytea NULL,
    member_of_attribute_m               bool NULL,
    member_sys_mapping                  bytea NULL,
    member_system_mapping_m             bool NULL,
    remove_catalogue_parent             bytea NULL,
    remove_catalogue_role_parent_node_m bool NULL,
    CONSTRAINT sys_sync_role_config_a_pkey PRIMARY KEY (id, rev)
);

ALTER TABLE sys_sync_role_config
    ADD CONSTRAINT fkc07ywlx9k3441kmcailrjqgps FOREIGN KEY (id) REFERENCES sys_sync_config (id);
ALTER TABLE sys_sync_role_config_a
    ADD CONSTRAINT fk9p9d0uyyg0qg2rmkj9ufp57pn FOREIGN KEY (id, rev) REFERENCES sys_sync_config_a (id, rev);

-- We need to insert data from abstract sync conf to the new sys_sync_role_config.

INSERT INTO sys_sync_role_config (id, assign_catalogue_switch, assign_role_remove_switch, assign_role_switch, forward_acm_switch, membership_switch, remove_catalogue_switch,
                                  skip_value_if_ex_switch)
SELECT c.id,
       false,
       false,
       false,
       false,
       false,
       false,
       false
FROM sys_sync_config c
WHERE c.id IN (
    SELECT sc.id
    FROM sys_system_mapping sm
             JOIN sys_sync_config sc ON (sm.id = sc.system_mapping_id)
    WHERE sm.entity_type = 'ROLE'
)

