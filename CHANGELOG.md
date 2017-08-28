# Changelog
All notable changes to this project will be documented in this file.


## [7.4.0-Snapshot] (Unreleased)

### Added

#### Core module

##### Long running task

- New long running task ExecuteScriptTaskExecutor, this task can execute any of scripts in DEFAULT category.
- new long running task RemoveOldLogsTaskExecutor for remove logging event, logging event exception and logging event property.

### Changed

#### Core module

##### Event processiong

- Added ``AbstractEventableDtoService`` for simplify services working with events. ``CoreEvent`` with ``CoreEventType`` is published by default now for save and delete event for all domain types (e.g. ``IdmIdentityDto``, ``IdmIdentityContractDto``, ``IdmRoleCatalogueDto``, ``IdmIdentityRoleDto`` ...). **Check [appropriate event type comparison](https://wiki.czechidm.com/devel/dev/architecture/events#event_types)** in your project.

##### Password policy

- Entity IdmPasswordPolicy was transformed to DTO. Update all places where call from your project service IdmPasswordPolicyService. The service now uses DTO only. Also check your current scripts and their authorities. If you're overloaded any of frontend component which is related IdmPasswordPolicy, check her functionality.
- The attributes of IdmPasswordPolicy are now Integer (in past int). Please Check your password policy settings and remove redundant zeros in settings. The zero in the setting is already taken as a valid setting value.

##### Script

- Script with category DEFAULT can be executed from executor with another category.

##### Confidential storage

- The encryption of the confidetial agenda can now be done using the key from application properties, also file with key may be defined in application properties. Backward compatibility with key defined in resource is maintained.

##### Role

- Role agenda was refactored to dto usage. Search **IdmRoleService** usage in your project - **all methods works with dto** now, entity service methods were removed (e.q. ``findSecured``). **In workflow ``roleSevice.get(roleId, null)`` method has to be used**.
- **``RoleFilter``** - fields are UUID now - **roleCatalogue => roleCatalogueId**, **guarantee => guaranteeId**
- **``GrantedAuthoritiesFactory#getActiveRoleAuthorities(UUID identityId, IdmRoleDto role)``** - method using role dto as parameter now
- **``TestHelper``** - role entity usage was removed - dto or role id (uuid) is used now.
- **``IdmRoleDto`` is used as event content now - change all entity event processors from template ``IdmRole`` to ``IdmRoleDto`` in your project.** ``RoleEvent`` uses ``IdmRoleDto`` content now too
- Rest endpoint - changed role lists structure (``IdmRoleGuaranteeDto, IdmRoleCatalogueDto`` are used now) - uuid only is needed, when relation is created and updated
- ``IdmRoleGuaranteeRepository`` - removed methods ``deleteByGuarantee_Id``, ``deleteByRole`` - service layer has to be used for create audit records.

##### Role request

- Added authorization policies support. New [authorization policy evaluators](https://wiki.czechidm.com/devel/dev/security/change-user-permissions#security) has to be [configured](https://wiki.czechidm.com/devel/dev/security/authorization#default_settings_of_permissions_for_an_identity_profile) to add permission for role requests. Added new permission group ``ROLEREQUEST``.
- Don't use **IdmRoleRequestRepository#find()** and **IdmConceptRoleRequestRepository#find()** methods directly => use service layer (methods are using criteria api now).
- Role request navigation item was moved to audit and enabled for all logged users - can read their own role requests history.


#### Acc module

##### Synchronization

- Entity SysSyncLog was transformed to DTO. Update all places where call from your project SysSyncLogService. The service now uses only DTO only. Backward compatibility of all synchronization logs is guaranteed.
- Entity SysSyncItemLog was transformed to DTO. Update all places where call from your project SysSyncItemLogService. The service now uses only DTO only. Backward compatibility of all synchronization item logs is guaranteed.
- Entity SysSyncActionLog was transformed to DTO. Update all places where call from your project SysSyncActionLogService. The service now uses only DTO only. Backward compatibility of all synchronization action logs is guaranteed.
- Entity SysSyncConfig was transformed to DTO. Update all places where call from your project SysSyncConfigService. The service now uses only DTO only. Backward compatibility of synchronization config is guaranteed.

##### Account management

- Added authorization policies support for identity accounts. Added new permission group ``IDENTITYACCOUNT`` with new evaluator [IdentityAccountByIdentityEvaluator](https://wiki.czechidm.com/devel/dev/security/authorization#identityaccountbyidentityevaluator).
- Don't use **AccIdentityAccountRepository#find()** method directly => use service layer (methods are using criteria api now).
- Added authorization policies support for role accounts. Added new tab to role detail with assigned role accounts. Added new permission group ``ROLEACCOUNT``. with new evaluator [RoleAccountByRoleEvaluator](https://wiki.czechidm.com/devel/dev/security/authorization#roleaccountbyroleevaluator).
- Don't use **AccRoleAccountRepository#find()** method directly => use service layer (methods are using criteria api now).
- Added authorization policies support for tree accounts. Added new tab to tree node detail with assigned tree accounts. Added new permission group ``TREEACCOUNT``.
- Don't use **AccTreeAccountRepository#find()** method directly => use service layer (methods are using criteria api now).


### Removed
