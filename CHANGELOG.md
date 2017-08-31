# Changelog
All notable changes to this project will be documented in this file.


## [7.4.0-Snapshot] (Unreleased)

### Requirements

#### All workflow instances (task) have to be closed (resolved / canceled) before update. Some services were refactored and new workflow definitions (core) will be installed automatically.

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

- Role agenda was refactored to dto usage. Search **``IdmRoleService``** usage in your project - **all methods works with dto**, entity service methods were removed (e.q. ``findSecured``). **In workflow ``roleSevice.get(roleId, null)`` method has to be used**.
- **``GrantedAuthoritiesFactory#getActiveRoleAuthorities(UUID identityId, IdmRoleDto role)``** - method using role dto as parameter.
- **``IdmRoleDto`` is used as event content - change all entity event processors from template ``IdmRole`` to ``IdmRoleDto`` in your project.** ``RoleEvent`` uses ``IdmRoleDto`` content too.
- Rest endpoint - changed role lists structure (``IdmRoleGuaranteeDto, IdmRoleCatalogueDto`` are used) - uuid only is needed, when relation is created and updated.
- ``IdmRoleGuaranteeRepository`` - removed methods ``deleteByGuarantee_Id``, ``deleteByRole`` - service layer has to be used for create audit records.
- **``RoleFilter``** was **moved to core api** module and renamed to ``IdmRoleFilter`` - we will add ``Idm`` prefix to all core filters.
- **``IdmRoleFilter``** - fields are ``UUID`` now - **roleCatalogue => roleCatalogueId**, **guarantee => guaranteeId**

##### Role request

- Added authorization policies support. New [authorization policy evaluators](https://wiki.czechidm.com/devel/dev/security/change-user-permissions#security) has to be [configured](https://wiki.czechidm.com/devel/dev/security/authorization#default_settings_of_permissions_for_an_identity_profile) to add permission for role requests. Added new permission group ``ROLEREQUEST``.
- Don't use **``IdmRoleRequestRepository#find()``** and **``IdmConceptRoleRequestRepository#find()``** methods directly => use service layer (methods are using criteria api now).
- Role request navigation item was moved to audit and enabled for all logged users - can read their own role requests history.

##### Tree structure

- Tree type and node agendas were refactored to dto usage. Search **``IdmTreeTypeService``** usage in your project - **all methods works with dto**. Search **``IdmTreeNodeService``** usage in your project - **all methods works with dto**.
- **``IdmTreeTypeService``** supports events (``CREATE``, ``UPDATE``, ``DELETE``).
- **``IdmTreeTypeService#getConfigurations(UUID)`` uses uuid as parameter**.
- **``IdmTreeTypeService#clearDefaultTreeType(UUID)`` uses uuid as parameter**, but will be removed at all soon - configuration service will be used for persist default tree type and node.
- **``IdmTreeTypeService#clearDefaultTreeNode(UUID)`` uses uuid as parameter**, but will be removed at all soon - configuration service will be used for persist default tree type and node.
- **``IdmTreeTypeFilter``** is used as filter in ``IdmTreeTypeService`` - its possible to find types by code from rest api.
- **``TreeNodeFilter``** was renamed to **``IdmTreeNodeFilter``**. Its possible to find node by type and code from rest api.
- Don't use **``IdmTreeTypeRepository#find()``** method directly => use service layer (methods are using criteria api now).
- **``IdmTreeNodeService#rebuildIndexes(UUID)``** uses uuid as parameter**.
- **Tree processors were moved to package ``eu.bcvsolutions.idm.core.model.event.processor.tree``**.
- **``IdmTreeNodeDto`` is used as event content - change all entity event processors from template ``IdmTreeNode`` to ``IdmTreeNodeDto`` in your project.** ``TreeNodeEvent`` uses ``IdmTreeNodeDto`` content too.
- **``IdmTreeTypeDto`` is used as event content - change all entity event processors from template ``IdmTreeType`` to ``IdmTreeTypeDto`` in your project.** ``TreeTypeEvent`` uses ``IdmTreeTypeDto`` content too.
- **Added authorization policies support.** Authorization policies has to be [configured](https://wiki.czechidm.com/devel/dev/security/authorization#default_settings_of_permissions_for_an_identity_profile) to add permission for tree types and nodes. ``BasePermissionEvaluator`` can be used.

##### TestHelper

- **``TestHelper``** interface was moved to core test api module.
- Role entity usage was removed - dto or role id (uuid) is used now.
- Tree type and node entities usage was removed - dto or id (uuid) is used now.

#### Acc module

##### Synchronization

- Entity **SysSyncLog was transformed to SysSyncLogDto**. Update all places where call from your project SysSyncLogService. The service now uses only DTO. Backward compatibility of all synchronization logs is guaranteed.
- Entity **SysSyncItemLog was transformed to SysSyncItemLogDto**. Update all places where call from your project SysSyncItemLogService. The service now uses only DTO. Backward compatibility of all synchronization item logs is guaranteed.
- Entity **SysSyncActionLog was transformed to SysSyncActionLogDto**. Update all places where call from your project SysSyncActionLogService. The service now uses only DTO. Backward compatibility of all synchronization action logs is guaranteed.
- Entity **SysSyncConfig was transformed to SysSyncConfigDto**. Update all places where call from your project SysSyncConfigService. The service now uses only DTO. Backward compatibility of synchronization config is guaranteed.

##### Attribute mapping
- Entity **SysRoleSystemAttribute was transformed to SysRoleSystemAttributeDto**. Update all places where call from your project [SysRoleSystemAttributeService](https://github.com/bcvsolutions/CzechIdMng/blob/develop/Realization/backend/acc/src/main/java/eu/bcvsolutions/idm/acc/service/api/SysRoleSystemAttributeService.java). The service now uses only DTO.
- Entity **SysSchemaAttribute was transformed to SysSchemaAttributeDto**. Update all places where call from your project [SysSchemaAttributeService](https://github.com/bcvsolutions/CzechIdMng/blob/develop/Realization/backend/acc/src/main/java/eu/bcvsolutions/idm/acc/service/api/SysSchemaAttributeService.java). The service now uses only DTO.
- Entity **SysSchemaObjectClass was transformed to SysSchemaObjectClassDto**. Update all places where call from your project [SysSchemaObjectClassService](https://github.com/bcvsolutions/CzechIdMng/blob/develop/Realization/backend/acc/src/main/java/eu/bcvsolutions/idm/acc/service/api/SysSchemaObjectClassService.java). The service now uses only DTO.
- Entity **SysSystemAttributeMapping was transformed to SysSystemAttributeMappingDto**. Update all places where call from your project [SysSystemAttributeMappingService](https://github.com/bcvsolutions/CzechIdMng/blob/develop/Realization/backend/acc/src/main/java/eu/bcvsolutions/idm/acc/service/api/SysSystemAttributeMappingService.java). The service now uses only DTO.
- Entity **SysSystemMapping was transformed to SysSystemMappingDto**. Update all places where call from your project [SysSystemMappingService](https://github.com/bcvsolutions/CzechIdMng/blob/develop/Realization/backend/acc/src/main/java/eu/bcvsolutions/idm/acc/service/api/SysSystemMappingService.java). The service now uses only DTO.

##### Role system
- Entity **SysRoleSystem was transformed to SysRoleSystemDto**. Update all places where call from your project [SysRoleSystemService](https://github.com/bcvsolutions/CzechIdMng/blob/develop/Realization/backend/acc/src/main/java/eu/bcvsolutions/idm/acc/service/api/SysRoleSystemService.java). The service now uses only DTO.

##### Account management

- Added authorization policies support for identity accounts. Added new permission group ``IDENTITYACCOUNT`` with new evaluator [IdentityAccountByIdentityEvaluator](https://wiki.czechidm.com/devel/dev/security/authorization#identityaccountbyidentityevaluator).
- Don't use **AccIdentityAccountRepository#find()** method directly => use service layer (methods are using criteria api now).
- Added authorization policies support for role accounts. Added new tab to role detail with assigned role accounts. Added new permission group ``ROLEACCOUNT``. with new evaluator [RoleAccountByRoleEvaluator](https://wiki.czechidm.com/devel/dev/security/authorization#roleaccountbyroleevaluator).
- Don't use **AccRoleAccountRepository#find()** method directly => use service layer (methods are using criteria api now).
- Added authorization policies support for tree accounts. Added new tab to tree node detail with assigned tree accounts. Added new permission group ``TREEACCOUNT``.
- Don't use **AccTreeAccountRepository#find()** method directly => use service layer (methods are using criteria api now).


### Removed
