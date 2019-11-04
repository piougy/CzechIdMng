# Migration Guide : CzechIdM 9.7.x to CzechIdM 10.0.x

## Introduction

This guide describes the various things that are needed when migrating from CzechIdM version 9.7.x to version 10.
In version 10 were upgraded major devstack dependencies (see list bellow). The goals:
- Fix known issues with newer versions of currently used third party libraries (e.g. ModelMapper).
- To be up to date. Some third party libraries cannot be used with our old devstack.
- Remove obsolete deprecated classes and methods.

> Note for **administator**:  is needed to read [Before ugrade](#before-upgrade) and [Configuration section](#configuration-properties).

> Note for **module developer**: is needed to read [Update custom module guide](#update-custom-module) and related conceptual and breaking changes.

> Note for **product developer:** is needed to read it all :).


### Upgraded libraries

- Spring Boot ``1.3.8.RELEASE`` => ``2.1.7.RELEASE``
  - Spring ``4.2.8.RELEASE`` => ``5.1.9.RELEASE``
  - Spring Security ``4.0.4.RELEASE`` => ``5.1.6.RELEASE``
  - Spring Data ``1.9.5.RELEASE`` => ``2.1.10.RELEASE``
  - Hibernate ``5.3.10.Final`` => ``5.3.10.Final``
  - Spring Data Rest removed at all
- Activiti ``5.22.0`` => ``6.0.0``
- Groovy ``2.4.7`` => ``2.5.8``
 - Groovy Sandbox ``1.11`` => ``1.19``
- ModelMapper ``0.7.8`` => ``2.3.5``
- Guava ``18.0`` => ``28.1-jre``
- Swagger ``2.7.0`` => ``2.9.2``
- Forest index ``0.3.0`` => ``1.1.1``
- ... *other minor and third party libraries*.

> Note for developer: We are using java 11 (openjdk) and Tomcat 9 for development.

#### Removed libraries

- **Spring Data Rest** - we are using Spring rest controllers.
- **Joda** time - and related libraries for json and dababase support.
- Spring Boot **Websocket** - websocket support removed.
- **org.codehaus.jackson** - we are usign **com.fasterxml.jackson** only.

## Before upgrade

- Delete all persisted events. Can be used:
  - task ``DeleteExecutedEventTaskExecutor``
  - or button on event agenda (available for super admin users).
- Resolve all workflow task (complete or cancel). New workflow definitions version will be deployed automatically. Old workflow tasks will be alive, but its saver to resolve them before new version is deployed. Historic tasks and processes will be preserved untouched and their detail will be available.

## Database migration

When the CzechIdM version 10 is started for the first time, it will do an automatic update (as with any 9 version) to the schema by provided Flyway change scripts.

> Note for developer: Default database name configured for the all ``dev`` profiles was renamed to ``bcv_idm_10`` to prevent update (by Flyway) old database on the background - **old database can be used for LTS version 9.7.x development**, so clone database is needed.

## Configuration properties

Based on upgraded libraries we have to add and remove configuration properties (mainly related to connection pool and hibernate).

### Added properties

- ``spring.jpa.properties.hibernate.jdbc.time_zone=UTC`` - datetime will be persisted in UTC in database.
- ``spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true`` - disable warning during the boot, when Hibernate tries to retrieve some meta information from the database.
- ``spring.datasource.hikari.maximumPoolSize=25`` - enlarge pool size by default. This property should be revised for each project.
- ``spring.jpa.properties.hibernate.session_factory.interceptor=eu.bcvsolutions.idm.core.model.repository.listener.AuditableInterceptor`` - replaced deprecated Hiberante property.
- ``spring.jpa.hibernate.use-new-id-generator-mappings=false`` - Spring boot 2 changed default to ``true``, but we are using ``IDENTITY`` identifier generators for mssql database.


### Removed properties

- ``spring.jpa.properties.jadira.usertype.autoRegisterUserTypes`` - we are using java time now, so configuration for Joda time is not needed.
- ``spring.jpa.properties.hibernate.ejb.interceptor`` - replaced by new property above.

## Conceptual changes

- Spring repository queries, which updates audited entity (``@Modifying`` annotation) is prohibited, because audit is skipped this way. Use entity service (find, then update). All product provided repository methods are removed in version 10 (see [removed methods](#removed-classes-methods-and-fields)).

## Breaking changes

- **Joda time library was removed** - all entities, dtos and services use java time now ⇒ api was changed, all places which used joda time have to be refactored (included workflow and groovy scripts). The related issue is with [serialized dtos](#serialized-dtos) in workflow properties and operation result.
- **Activiti** 6 registered new ``formService`` bean usable in workflow definition ⇒ IdM eav service is not available under ``formService`` name any more. New bean alias ``idmFormService`` was created and has to be used in workflows.
- **Hibernate** removed data type ``org.hibernate.type.StringClobType`` - all entities has to be refactored, type ``org.hibernate.type.TextType`` has to be used.
- **Mockito** changes behavior for ``any(Class)`` checker - doesn't support ``null`` parameter value now. This is used just in unit tests. Test can be compiled but doesn't work.
- **Spring** data repository api changes:
  - e.g. ``findOne`` renamed to ``findOneById`` and returns ``Optional<E>``,
  - the concept is changed the same way for all methods.

## Update custom module

Due to breaking changes above, custom module requires some refactoring, before it's compatible with CzechIdM version 10. Some refactoring can be done with replaces, but some places has to be changed manually.

### Automatic replaces

> Case sensitive find is expected.

- ``org.hibernate.type.StringClobType`` ⇒ ``org.hibernate.type.TextType``
- ``new PageRequest(`` ⇒ ``PageRequest.of(``
- ``new Sort(`` ⇒ ``Sort.by(``
- ``flyway.getTable()`` ⇒ ``flyway.getConfiguration().getTable()``
- ``import org.joda.time.LocalDate;`` ⇒ ``import java.time.LocalDate;``
- ``import org.joda.time.DateTime;`` ⇒ ``import java.time.ZonedDateTime;``
- ``new DateTime()`` ⇒ ``ZonedDateTime.now()``
- (whole word, case sensitive): ``DateTime ⇒ ZonedDateTime;``
- ``new LocalDate() ⇒ LocalDate.now()``
- ``import org.joda.time.format.DateTimeFormatter;`` ⇒ ``import java.time.format.DateTimeFormatter;``
- `` DateTimeFormatter dateFormat = DateTimeFormat.forPattern(configurationService.getDateFormat());`` ⇒ ``DateTimeFormatter formatter = DateTimeFormatter.ofPattern(getConfigurationService().getDateFormat());``


### Manual changes / cookbook

- *automatic replaces above are expected*
- **Java time** usage:
  - ``date.getMilis()`` ⇒ ``ZonedDateTime.now().toInstant().toEpochMilli()``
  - ``date.toString(pattern)`` ⇒ ``date.format(formatter)`` or ``dateFormat.print(date)`` ⇒ ``formatter.format(date)``
  - ``date.plusMilis(1)`` ⇒ ``date.plus(1, ChronoUnit.MILLIS)``
  - ``Date date = Date.from(ZonedDateTime.now().toInstant());``
  - ``ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(longValue), ZoneId.systemDefault());``
  - ``ChronoUnit.SECONDS.between(authenticationDto.getExpiration(), newExpiration);``
- **Mockito**:
  - ``any(String.class)`` checker doesn't support ``null`` parameter value now ⇒ ``(String) any()`` can be used.
- **Spring**:
  - data repository changes api - ``findOne`` renamed to ``findOneById`` and returns ``Optional<E>`` now.
  - ``@Service`` annotation cannot be used for the integration test itself ⇒ ``applicationContext.getBean(this.getClass)`` doesn't work in interagtion tests ⇒ has to be refactored to ``applicationContext.getAutowireCapableBeanFactory().createBean(this.getClass())`` - new instance is created, but can be overlooked in tests :).
  - ``@Service`` and ``@Component`` constructors was simplified - some constructor parameters was moved to ``@Autowired`` fields. If you are overriding service from IdM implementation package (``core-impl``), then update constructor usage.
- **Workflow**:
  - find ``formService`` usage in workflow definitions and replace it with ``idmFormService ``.
  - check your workflow definitions. Mainly **prevent joda time usage** - replaces above should help to find all places, which have to be refactored.
- **Groovy scripts**:
  - check your groovy scripts. Mainly **prevent joda time usage** (e.g. in transformation scripts) and replace usage with java time.
  - Java time classes were added to global script authorities (``LocalDate``, ``ZonedDateTime``, ``ZoneId``, ``OffsetTime``, ``OffsetDateTime``, ``LocalDateTime``, ``LocalTime``, ``Instant``, ``DateTimeFormatter``).
  - Joda time classes could be still used as input for extended attribute value (``IdmFormValueDto#setValue``) => will be converted automatically to java time (``IdmFormValueDto#getValue`` returns java time only).
- **fix warnings**:
  - ``Assert`` - add message
  - ``IOUtils.closeQuietly`` - refactor with ``try-with-resources`` syntax.
  - ``class.newInstance()`` is deprecated - use ``class.getDeclaredConstructor().newInstance()`` - ``ReflectiveOperationException`` can be catched if needed.
- run tests (all green)
- run application and test


#### Serialized dtos

Serialized dtos are persisted in database - used in workflow properties or for operation results. After changing the data type from **Joda** to java time for dto fields, the persisted serialized stream for this dto is obsolete and cannot be deserialized out of box (=> data type is changed).
We need to provide custom deserialization method (see ``AbstractDto#readObject(ObjectInputStream)``) for dto, which uses Joda ``DataTime`` or ``LocalDate`` field and is used as workflow property or as operation result.

Example (from ``IdmConceptRoleRequestDto``):

```java
  /**
	 * DTO are serialized in WF and embedded objects.
	 * We need to solve legacy issues with joda (old) vs. java time (new) usage.
	 *
	 * @param ois
	 * @throws Exception
	 */
	private void readObject(ObjectInputStream ois) throws Exception {
		GetField readFields = ois.readFields();
		//
		roleRequest = (UUID) readFields.get("roleRequest", null);
	    identityContract = (UUID) readFields.get("identityContract", null);
	    contractPosition = (UUID) readFields.get("contractPosition", null);
	    role = (UUID) readFields.get("role", null);
	    identityRole = (UUID) readFields.get("identityRole", null);
	    roleTreeNode = (UUID) readFields.get("roleTreeNode", null);
	    validFrom = DtoUtils.toLocalDate(readFields.get("validFrom", null));
	    validTill = DtoUtils.toLocalDate(readFields.get("validTill", null));
	    operation = (ConceptRoleRequestOperation) readFields.get("operation", null);
	    state = (RoleRequestState) readFields.get("state", null);
	    wfProcessId = (String) readFields.get("wfProcessId", null);
	    log = (String) readFields.get("log", null);
	    valid = readFields.get("valid", false);
	    duplicate = (Boolean) readFields.get("duplicate", null);
	    systemState = (OperationResultDto) readFields.get("systemState", null);
    }
```

> Note for developer: serialized custom dto is most probably not persisted into database in custom module.

> Note for developer: persistent serialized properties are used for asynchronous event processing to, but its required to remove old events [before version 10](#before-upgrade) is installed.

#### Logback configuration

Configuration file in test package ``logback-test.xml`` has to removed. New ``logback.xml`` with content has to be added:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- https://springframework.guru/using-logback-spring-boot/ -->
<!-- http://logback.qos.ch/manual/appenders.html -->
<!DOCTYPE configuration>
<configuration>

	<conversionRule conversionWord="clr" converterClass="org.springframework.boot.logging.logback.ColorConverter" />
	<conversionRule conversionWord="wex" converterClass="org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter" />
	<conversionRule conversionWord="wEx" converterClass="org.springframework.boot.logging.logback.ExtendedWhitespaceThrowableProxyConverter" />
	<property name="CONSOLE_LOG_PATTERN" value="${CONSOLE_LOG_PATTERN:-%clr(%d{${LOG_DATEFORMAT_PATTERN:-yyyy-MM-dd HH:mm:ss.SSS}}){faint} %clr(${LOG_LEVEL_PATTERN:-%5p}) %clr(${PID:- }){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}}"/>
	<property name="FILE_LOG_PATTERN" value="${FILE_LOG_PATTERN:-%d{${LOG_DATEFORMAT_PATTERN:-yyyy-MM-dd HH:mm:ss.SSS}} ${LOG_LEVEL_PATTERN:-%5p} ${PID:- } --- [%t] %-40.40logger{39} : %m%n${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}}"/>


	<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
		<encoder>
			<pattern>${CONSOLE_LOG_PATTERN}</pattern>
		</encoder>
	</appender>

	<root level="WARN">
		<appender-ref ref="CONSOLE" />
	</root>


</configuration>
```

> Note for developer: every custom module has ``logback-test.xml``. Test cannot run without this change.

## Removed classes, methods and fields

- **Websocket** support removed - Removed all classed for websocket notifications
- ``IdmTreeNodeFilter#setTreeNode(UUID)`` - @deprecated @since 9.4.0 - use ``IdmTreeNodeFilter#setParent(UUID)``.
- ``IdmTreeNodeFilter#getTreeNode()`` - @deprecated @since 9.4.0 - use ``IdmTreeNodeFilter#getParent()``.
- ``IdmTreeNodeFilter#PARAMETER_PARENT_TREE_NODE_ID`` - @deprecated @since 9.4.0 - use ``IdmTreeNodeFilter#PARAMETER_PARENT``.
- ``IdmIdentityContractService#prepareDefaultContract(UUID)`` - @deprecated @since 7.4.0 - use ``IdmIdentityContractService#prepareMainContract(UUID)``.
- ``IdmIdentityContractRepository#findAllByWorkPosition(IdmTreeNode, RecursionType)`` => parameter changed to ``IdmIdentityContractRepository#findAllByWorkPosition(UUID, RecursionType)`` - previous SpEl expression doesn't work => ``UUID`` is used now as parameter.
- ``IdmContractPositionRepository#findAllByWorkPosition(IdmTreeNode, RecursionType)`` => parameter changed to ``IdmContractPositionRepository#findAllByWorkPosition(UUID, RecursionType)``
- ``IdmIdentityContractRepository#countByWorkPosition(IdmTreeNode)`` - @deprecated @since 7.4.0 - use ``IdmIdentityContractRepository#countByWorkPosition_Id(UUID)``.
- ``IdmIdentityContractRepository#countByWorkPosition_TreeType(IdmTreeType)`` - @deprecated @since 7.4.0 - use ``IdmIdentityContractRepository#countByWorkPosition_TreeType_Id(UUID)``.
- Unused Hibernate convertors ``LocalDateJpaConverter``, ``LocalDateTimeJpaConverter`` removed.
- ``WorkflowDefinitionAssembler`` - unused and @deprecated @since 7.8.0.
- ``CustomParseListener`` - unused and @deprecated @since 7.8.0.
- ``FilterBuilder#getPredicate(Root, CriteriaQuery, CriteriaBuilder, DataFilter)`` - @deprecated @since 9.7.0 - use ``FilterBuilder#getPredicate(Root, AbstractQuery, CriteriaBuilder, DataFilter)`` - method was deprecated recently, but we want to prevent its usage (related to issue [#1779](https://redmine.czechidm.com/issues/1779)).
- ``WorkflowFilterDto`` - redundant pageable properties (``pageNumber``, ``pageSize``, ``sortAsc``, ``sortDesc``, ``sortByFields``) removed. @deprecated @since 7.7.0. Use standard ``Pageable`` api instead.
- ``WorkflowTaskInstanceService#search(WorkflowFilterDto)`` - @deprecated @since 7.7.0 - use ``WorkflowTaskInstanceService#find(WorkflowFilterDto, Pageable)``.
- ``WorkflowHistoricProcessInstanceService#search(WorkflowFilterDto)`` - @deprecated @since 7.7.0 - use ``WorkflowHistoricProcessInstanceService#find(WorkflowFilterDto, Pageable)``.
- ``WorkflowHistoricTaskInstanceService#search(WorkflowFilterDto)`` - @deprecated @since 7.7.0 - use ``WorkflowHistoricTaskInstanceService#find(WorkflowFilterDto, Pageable)``.
- ``WorkflowProcessInstanceService#search(WorkflowFilterDto)`` - @deprecated @since 7.7.0 - use ``WorkflowProcessInstanceService#find(WorkflowFilterDto, Pageable)``.
- ``WorkflowProcessInstanceService#searchInternal(WorkflowFilterDto, boolean)`` - @deprecated @since 7.7.0 - use ``WorkflowProcessInstanceService#find(WorkflowFilterDto, Pageable, BasePermission)`` - parameter ``checkRigts`` is synonymum to ``IdmBasePermission.READ`` permission in standard find method.
- Classes ``ResourceWrapper``, ``ResourcesWrapper``, ``EmbeddedsWrapper``, ``ResourcePage'`` (all in package ``eu.bcvsolutions.idm.core.api.rest.domain``) - @deprecated @since 7.7.0 - use standard ``find`` methods on workflow services instead.
- ``IdmAuditFilter#getChangedAttributes()`` - @deprecated @since 9.5.0 - use ``IdmAuditFilter#getChangedAttributesList()``.
- ``IdmAuditFilter#setChangedAttributes(List)`` - @deprecated @since 9.5.0 - use ``IdmAuditFilter#setChangedAttributesList(List<String>)``.
- ``IdmAuditService#findEntityWithRelation(Class, MultiValueMap, Pageable)``- @deprecated @since 9.4.1 use ``IdmAuditService#findEntityWithRelation(IdmAuditFilter, Pageable)`` - ``ownerType`` filter can be used. If ``username`` parameter was specified, then use ``ownerCode`` filter).
- ``ProvisioningService#createAccountsForAllSystems(AbstractDto)`` - @deprecated @since 7.6.0 - use ``ProvisioningService#accountManagement(AbstractDto)``.
- ``ProvisioningEntityExecutor#createAccountsForAllSystems(DTO)`` - @deprecated @since 7.6.0 - use ``ProvisioningEntityExecutor#accountManagement(DTO)``.
- ``IdmAuditService#getNameChangedColumns(Class<T>, UUID, Long, T)`` - @deprecated @since 7.8.2 - changed columns are solved directly in audit strategy (``IdmAuditStrategy``).
- ``AccAccountRepository#countBySystem(SysSystem)`` - @deprecated @since 7.4.0 - use ``AccAccountRepository#countBySystem_Id(UUID)``.
- ``AccAccountRepository#clearSystemEntity(UUID)`` - @deprecated @since 7.5.1 - use find - update in service instead (audit will not be skipped).
- ``SysSystemAttributeMappingRepository#clearIsUidAttribute(UUID)`` - @deprecated @since 7.5.0 - use find - delete in service instead (audit will not be skipped).
- ``AccIdentityAccountRepository#clearRoleSystem(SysRoleSystem)`` - use find - update in service instead (audit will not be skipped).
- ``AccTreeAccountRepository#clearRoleSystem(SysRoleSystem)`` - use find - update in service instead (audit will not be skipped).
- ``SysSystemRepository#find(SysSystemFilter, Pageable)`` - @deprecated @since 7.5.2 - use ``SysSystemService#find(SysSystemFilter, Pageable, BasePermission...)``
- ``SysSystemRepository#clearPasswordPolicy(IdmPasswordPolicy)`` - use find - update in service instead (audit will not be skipped).
- ``IdmFormDefinitionRepository#clearMain(String, UUID, ZonedDateTime)`` - @deprecated @since 7.4.0 - use find - update in service instead (audit will not be skipped).
- ``IdmPasswordPolicyRepository#updateDefaultPolicyByType(IdmPasswordPolicyType, UUID)`` - use find - update in service instead (audit will not be skipped).
- ``SysSyncConfigRepository#clearDefaultLeader(UUID)`` - use find - update instead (audit will not be skipped).
- ``SysSyncConfigRepository#clearDefaultRole(UUID)`` - use find - update instead (audit will not be skipped).
- ``SysSyncConfigRepository#clearDefaultTreeNode(UUID)`` - use find - update instead (audit will not be skipped).
- ``SysSyncConfigRepository#clearDefaultTreeType(UUID)`` - use find - update instead (audit will not be skipped).
- ``IdmLongRunningTaskRepository#findAllByInstanceIdAndResult_State(String, OperationState)`` - @deprecated @since 7.5.0 - use ``IdmLongRunningTaskRepository#findAllByInstanceIdAndResult_StateOrderByCreatedAsc(String, OperationState)``.
- ``AccModuleDescriptor#TOPIC_NEW_PASSWORD_ALL_SYSTEMS`` - @deprecated @since 8.0.0 - use ``CoreModuleDescriptor.TOPIC_PASSWORD_CHANGED``.
- ``SysProvisioningArchiveRepository#deleteBySystem(SysSystem)`` - @deprecated @since 7.4.0 - use ``SysProvisioningArchiveRepository#deleteBySystem_Id(UUID)``.
- ``SysProvisioningOperationRepository#deleteBySystem_Id(UUID)`` - @deprecated @since 9.2.1 - use ``SysProvisioningOperationRepository#deleteBySystem(UUID)``.
- ``SysRoleSystemAttributeRepository#deleteByRoleSystem(SysRoleSystem)`` - @deprecated @since 9.5.0 - use find - delete in service instead (method doesn't put merge values in historical controled values.).
- ``SysProvisioningOperationController#cancelAll(MultiValueMap<String, Object>, )`` - @deprecated @since 9.5.2 - use provided ``ProvisioningOperationCancelBulkAction`` bulk action instead.
- ``BaseEntityController`` - @deprecated @since 7.1.0 - use ``BaseDtoController``.
- ``IdmAutomaticRoleAttributeService#prepareAddAutomaticRoles(IdmIdentityContractDto, Set)`` - @deprecated @since 7.8.4 - use ``IdmAutomaticRoleAttributeService#addAutomaticRoles(IdmIdentityContractDto, Set)``.
- ``IdmAutomaticRoleAttributeService#prepareRemoveAutomaticRoles(IdmIdentityRoleDto, Set)`` - @deprecated @since 7.8.4 - use ``IdmAutomaticRoleAttributeService#removeAutomaticRoles(IdmIdentityRoleDto, Set)``.
- ``IdmConceptRoleRequestFilter#getRoleTreeNodeId()`` - @deprecated @since 7.7.0 - use ``IdmConceptRoleRequestFilter#getAutomaticRole()``.
- ``IdmConceptRoleRequestFilter#setRoleTreeNodeId(UUID)`` - @deprecated @since 7.7.0 - use ``IdmConceptRoleRequestFilter#setAutomaticRole(UUID)``.
- ``IdmEntityEventService#findToExecute(String, ZonedDateTime, PriorityType, Pageable)`` - @deprecated @since 9.4.0 - use ``IdmEntityEventService#findToExecute(String, DateTime, PriorityType, List, Pageable)``.
- ``IdmIdentityRoleService#findValidRole(UUID, Pageable)`` - @deprecated @since 8.0.0 - use ``IdmIdentityRoleService#findValidRoles(UUID, Pageable)``.
- ``IdmIdentityService#findAllGuaranteesByRoleId(UUID)`` - @deprecated @since 8.2.0 - use ``IdmIdentityService#findGuaranteesByRoleId(UUID, Pageable)``.
- ``IdmPasswordService#getSalt(IdmIdentityDto)`` - @deprecated @since 8.0.1 - use ``IdmPasswordService#getSalt()``.
- ``IdmRoleService#getSubroles(UUID)`` - @deprecated @since 8.0.1 - use ``IdmRoleCompositionService#findDirectSubRoles(UUID)``.
- ``IdmRoleTreeNodeService#prepareAssignAutomaticRoles(IdmIdentityContractDto, Set)`` - @deprecated @since 7.8.4 - use ``IdmRoleTreeNodeService#addAutomaticRoles(IdmIdentityContractDto, Set)``.
- ``IdmRoleTreeNodeService#assignAutomaticRoles(IdmIdentityContractDto, Set)`` - @deprecated @since 7.8.4 - use ``IdmRoleTreeNodeService#addAutomaticRoles(IdmIdentityContractDto, Set)``.
- ``IdmRoleTreeNodeService#prepareRemoveAutomaticRoles(IdmIdentityRoleDto, Set)`` - @deprecated @since 9.5.0 - use ``IdmRoleTreeNodeService#removeAutomaticRoles(IdmIdentityContractDto, Set)``.
- ``IdmScriptService#getScriptByName(String)`` - @deprecated @since 7.6.0 - use ``IdmScriptService#getByCode(String)``.
- ``IdmScriptService#getScriptByCode(String)`` - @deprecated @since 7.6.0 - use ``IdmScriptService#getByCode(String)``.
- ``AuthorizationEvaluator#getParameterNames()`` - @deprecated @since 8.2.0 - use ``AuthorizationEvaluator#getFormDefinition()``.
- ``IdmProcessedTaskItemService#findAllRefEntityIdsInQueueByScheduledTask(IdmScheduledTaskDto)`` - @deprecated @since 9.3.0 - use ``IdmProcessedTaskItemService#findAllRefEntityIdsInQueueByScheduledTaskId(UUID)``.
- ``IdmProcessedTaskItemService#createLogItem(DTO, OperationResult, IdmLongRunningTaskDto)`` - @deprecated @since 9.3.0 - use ``IdmProcessedTaskItemService#createLogItem(AbstractDto, OperationResult, UUID)``.
- ``IdmProcessedTaskItemService#createQueueItem(DTO, OperationResult, IdmScheduledTaskDto)`` - @deprecated @since 9.3.0 - use ``IdmProcessedTaskItemService#createQueueItem(AbstractDto, OperationResult, UUID)``.
- ``IdmAudit#DELIMITER`` - @deprecated @since 7.8.2 - use ``IdmAuditDto.CHANGED_COLUMNS_DELIMITER``.
- ``IdmAuditRepository#getPreviousVersion(UUID, Long, Pageable)`` - @deprecated @since 8.0.0 - use ``IdmAuditRepository#getPreviousVersion(UUID, Long)``.
- ``IdentitySaveBulkAction`` - @deprecated @since 9.4.0 - use concrete bulk actions for execute ACM or provisioning only in acc module.
- ``AbstractFormValueRepository#findByOwner(O)`` - @deprecated @since 8.2.0 - use ``FormValueService#find(IdmFormValueFilter, Pageable)``.
- ``AbstractFormValueRepository#findByOwner_Id(Serializable)`` - @deprecated @since 8.2.0 - use ``FormValueService#find(IdmFormValueFilter, Pageable)``.
- ``AbstractFormValueRepository#findByOwnerAndFormAttribute_FormDefinitionOrderBySeqAsc(O, IdmFormDefinition)`` - @deprecated @since 8.2.0 - use ``FormValueService#find(IdmFormValueFilter, Pageable)``.
- ``AbstractFormValueRepository#findByOwnerAndFormAttribute_FormDefinition_IdOrderBySeqAsc(O, UUID)`` - @deprecated @since 8.2.0 - use ``FormValueService#find(IdmFormValueFilter, Pageable)``.
- ``AbstractFormValueRepository#findByOwner_IdAndFormAttribute_FormDefinition_IdOrderBySeqAsc(Serializable, UUID)`` - @deprecated @since 8.2.0 - use ``FormValueService#find(IdmFormValueFilter, Pageable)``.
- ``AbstractFormValueRepository#findByOwner_IdAndFormAttributeOrderBySeqAsc(Serializable, IdmFormAttribute)`` - @deprecated @since 8.2.0 - use ``FormValueService#find(IdmFormValueFilter, Pageable)``.
- ``AbstractFormValueRepository#findByOwner_IdAndFormAttribute_IdOrderBySeqAsc(Serializable, UUID)`` - @deprecated @since 8.2.0 - use ``FormValueService#find(IdmFormValueFilter, Pageable)``.
- ``IdmIdentityRoleRepository#countByRole(IdmRole)`` - @deprecated @since 7.4.0 - use ``IdmIdentityRoleRepository#countByRole_Id(UUID)``.
- ``IdmRoleCatalogueRoleRepository#find(IdmRoleCatalogueRoleFilter, Pageable)`` - @deprecated @since 8.1.4 - use ``IdmRoleCatalogueRoleService#find(IdmRoleCatalogueRoleFilter, Pageable)``.
- ``IdmRoleCatalogueRoleRepository#findAllByRole_Id(UUID)`` - @deprecated @since 8.1.4 - use ``IdmRoleCatalogueRoleService#find(IdmRoleCatalogueRoleFilter, Pageable)``.
- ``IdmRoleCatalogueRoleRepository#findAllByRoleCatalogue_Id(UUID)`` - @deprecated @since 8.1.4 - use ``IdmRoleCatalogueRoleService#find(IdmRoleCatalogueRoleFilter, Pageable)``.
- ``IdmRoleCatalogueRoleRepository#deleteAllByRole_Id(UUID)`` - @deprecated @since 8.1.4 - use ``IdmRoleCatalogueRoleService#find(IdmRoleCatalogueRoleFilter, Pageable)`` and then delete.
- ``IdmRoleCatalogueRoleRepository#deleteAllByRoleCatalogue_Id(UUID)`` - @deprecated @since 8.1.4 - use ``IdmRoleCatalogueRoleService#find(IdmRoleCatalogueRoleFilter, Pageable)`` and then delete.
- ``IdmMessage#DEFAULT_LEVEL`` - @deprecated @since 7.6.0 - use ``IdmMessageDto.DEFAULT_LEVEL``.
- ``IdmNotificationConfigurationRepository#findTypes(String, NotificationLevel)`` - @deprecated @since 9.2.0 - use ``IdmNotificationConfigurationRepository#findAllByTopicAndWildcardLevel(String, NotificationLevel)``.
- ``IdmIdentityController#roles(String)`` - @deprecated @since 9.4.0 - use ``IdmIdentityRoleController#find(MultiValueMap, Pageable)`` with filter by identity.
- ``AbstractAutomaticRoleTaskExecutor#setRoleTreeNodeId(UUID)`` - @deprecated @since 7.6.0 - use ``AbstractAutomaticRoleTaskExecutor#setAutomaticRoleId(UUID)``.
- ``AbstractAutomaticRoleTaskExecutor#getRoleTreeNodeId()`` - @deprecated @since 7.6.0 - use ``AbstractAutomaticRoleTaskExecutor#getAutomaticRoleId(UUID)``.
- ``AddNewAutomaticRoleForPositionTaskExecutor#setRoleTreeNodeId(UUID)`` - @deprecated @since 7.6.0 - use ``AbstractAutomaticRoleTaskExecutor#setAutomaticRoleId(UUID)``.
- ``AddNewAutomaticRoleForPositionTaskExecutor#getRoleTreeNodeId()`` - @deprecated @since 7.6.0 - use ``AbstractAutomaticRoleTaskExecutor#getAutomaticRoleId(UUID)``.
- ``AddNewAutomaticRoleTaskExecutor#setRoleTreeNodeId(UUID)`` - @deprecated @since 7.6.0 - use ``AbstractAutomaticRoleTaskExecutor#setAutomaticRoleId(UUID)``.
- ``AddNewAutomaticRoleTaskExecutor#getRoleTreeNodeId()`` - @deprecated @since 7.6.0 - use ``AbstractAutomaticRoleTaskExecutor#getAutomaticRoleId(UUID)``.
- ``RemoveAutomaticRoleTaskExecutor#setRoleTreeNodeId(UUID)`` - @deprecated @since 7.6.0 - use ``AbstractAutomaticRoleTaskExecutor#setAutomaticRoleId(UUID)``.
- ``RemoveAutomaticRoleTaskExecutor#getRoleTreeNodeId()`` - @deprecated @since 7.6.0 - use ``AbstractAutomaticRoleTaskExecutor#getAutomaticRoleId(UUID)``.
