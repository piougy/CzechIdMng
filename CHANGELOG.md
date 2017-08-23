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

##### Password policy

- Entity IdmPasswordPolicy was transformed to DTO. Update all places where call from your project service IdmPasswordPolicyService. The service now uses DTO only. Also check your current scripts and their authorities. If you're overloaded any of frontend component which is related IdmPasswordPolicy, check her functionality.
- The attributes of IdmPasswordPolicy are now Integer (in past int). Please Check your password policy settings and remove redundant zeros in settings. The zero in the setting is already taken as a valid setting value.

##### Script

- Script with category DEFAULT can be executed from executor with another category.

##### Confidential storage

- The encryption of the confidetial agenda can now be done using the key from application properties, also file with key may be defined in application properties. Backward compatibility with key defined in resource is maintained.

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


### Removed
