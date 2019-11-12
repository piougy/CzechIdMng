# Migration Guide : CzechIdM 9.7.x to CzechIdM 10.0.x

## Introduction

This guide describes the various things that are needed when migrating from CzechIdM version 9.7.x to version 10.
In version 10 were upgraded major devstack dependencies (see list bellow). The goals:
- Fix known issues with newer versions of currently used third party libraries (e.g. ModelMapper).
- To be up to date. Some third party libraries cannot be used with our old devstack.
- Remove obsolete deprecated classes and methods.


# Backend
In this chapter will be describe migration for the backend part of IdM.

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

### Replaces

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

# Frontend
In this chapter will be describe migration for the frontend part of IdM.

The main goal of upgrading the frontend in **version 10** was to upgrade **React** to version **16**. Previous version **15** has already limited us in selecting new and upgrading existing components.

Main dependencies upgraded during migration:

* **React** from **15.6** to **16.9**.
* **React-router** from **2.3** to **5.1**.
* **React-redux** from **4.4** to **7.1**.
* **Redux** from **3.5** to **4.0**.
* **Redux-immutable** from **1.3** to **4.0**.
* **React-ace** from **3.7** to **7.0**.
* **Browserify** from **13.0** to **16.5**.
* And next more then 20 minor updates to dependent components.

> All updated dependencies are in the product package.json. It means, If you will used the CzechIdM 10.x.x, then you already have dependency on the **React 16** and others.

## React sice version 16 has PropTypes in separate module

Use official conversion utility for move PropTypes in you module.

<code>npx react-codemod React-PropTypes-to-prop-types --force</code>

## Method componentWillReceiveProps and componentWillMount
Method **componentWillReceiveProps** and **componentWillMount** were renamed to **UNSAFE_componentWillReceiveProps** and **UNSAFE_componentWillMount**. Since **React 17** only UNSAFE variant will be called!

This **methods are deprecated** and should be not used. In the product was this method componentWillReceiveProps used in 60 files. In version 10 was this method removed from 30 files (typically from component shows a detail).

Removal of this method was possible thanks to a new approach to rendering individual routed components.
In other words, the **componentWillReceiveProps** method was used in these places to detect changes in properies. For example, if the user name identity has changed in the url. If this change was detected, the identity was read again and the detail was initialized (sets to the state).

However, since **version 10**, if the url is changed, the routed component is destroyed and recreated. Therefore, the **componentWillReceiveProps** method is no longer needed.

> If you don't want remove UNSAFE_componentWillReceiveProps or UNSAFE_componentWillMount you don't need to. Only what you have to need is rename this methods (add UNSAFE_).

Use official conversion utility for rename deprecated methods.

<code>npx react-codemod rename-unsafe-lifecycles --force</code>

## React TestUtils was moved to react-dom

**Replace all occurrences**:

<code>import TestUtils from 'react-addons-test-utils';</code>
->
 <code>import TestUtils from 'react-dom/test-utils';</code>

## React Shallow renderer

Tests using Shallow renderer from the **react-test-renderer** now.

**Replace all occurrences**:

<code>TestUtils.createRenderer();</code>
->
 <code>new ShallowRenderer();</code>

 You have to add import to a modified files:

 <code>import ShallowRenderer from 'react-test-renderer/shallow';</code>.

## React-redux - forwardRef

In new version of Redux, is returned wrapped component directly (component.getWrappedInstance() is not necessary). For this feature you have to define  **forwardRef: true** in redux connect function.

**Replace all occurrences**:

<code>withRef: true</code>
->
 <code>forwardRef: true</code>

## React-redux - remove getWrappedInstance

Method **getWrappedInstance()** on a redux components no longer exists, because is no necessary (if **forwardRef** is used).

**Replace all occurrences**:

<code>.getWrappedInstance().</code>
->
 <code>.</code>

## React router - Link
React router components were moved to the module **react-router-dom**.

**Replace all occurrences**:

<code>import { Link } from 'react-router';</code>
->
 <code>import { Link } from 'react-router-dom';</code>

## React router - Context
In new version of the React Router is router context no longer propagate.

>We used router context for redirection (methods **replace**/**push**/**goBack**) in previous verson of CzechIdM.
For replace it was in CzechIdM 10 created **IdmContext**. This context contains all **routes**, redux **store** and router **history**.

You have to use **this.context.history** for redirection (replace/push/goBack).

**Replace all occurrences**:

<code>this.context.router</code>
->
 <code>this.context.history</code>

## React router - URL params
Url parameters are no longer in **props.params**, but in match object. Use **props.match.params** instead.

**Replace all occurrences (in the same order)**:

<code>component.params</code>
->
 <code>component.match.params</code>

<code>this.props.params</code>
 ->
  <code>this.props.match.params</code>

<code>params={ this.props.match.params }</code>
 ->
  <code>match={ this.props.match }</code>

<code>params={this.props.match.params}</code>
 ->
  <code>match={ this.props.match }</code>

<code>props.params</code>
 ->
  <code>props.match.params</code>

<code>nextProps.params</code>
 ->
  <code>nextProps.match.params</code>

## React router - Dynamic routes
In **React Router v4** are **routes.js** no longer use. Every parent component must know all his children now.

**This broked our modularity**, because we compose routes from every modules dynamically. It means parent component doesn't know about his children routes in CzechIdM.  

In **CzechIdM 10** was developed mechanism for generating **<Route>** elements by configuration from a **routes.js**.

**This was the biggest task in upgrade the frontend.**

>All components in CzechIdM extends **AbstractContextComponent**. In this class was implemented dynamic routing. It means here are used old routes (composed from all routes.js) and generated new `<Route>` for React Router V4. All what you need is call **this.getRoutes()** in every places where routes children (**this.props.children**) were rendered (in Routes V4 will be **this.props.children** empty).

Definition of routes in all modules shouldn't need any update. But definition of a routes can be written in many ways and it means some of these cases have to be not covered by dynamic routing in CzechIdM 10.

**So beware and test  all routing/redirections in your module after upgrade on CzechIdM 10!**

**!!ONLY!!** for components using **Advanced.TabPanel** make replace:

<code>this.props.children</code>
->
 <code>this.getRoutes()</code>

## React router - redirections

If you using redirect via **replace** or **push** method, then you have to ensure correct format of the path. Previouse version of react-router used absolute path only. **This is not true now**!

So, if you have this redirection:

`this.context.history.replace('systems');`

, then final url will be `http://localhost:3000/#/currentUrl/systems`!

If you want to use **absolute path** `http://localhost:3000/#/systems`, you have to add the slash:

 `this.context.history.replace('/systems');`!

**Suggestion is try to search** (`this.context.history.push(`, `this.context.history.replace(`) and check if all useing of **replace**/**push** starts with **slash**.
>Replace all is not good idea, because in some cases can starts expression with call a method.

## React router - redirections with `to`
Same situation as previous occurs for `<Basic.LinkCell>` and `<Advanced.ColumnLink>` components, where attribute **to** is no longer absolute. So you need to add slash (if missing on the start of path).

**Suggestion is try to search** (`to="`) and check if all useing starts with slash.
> Replace all is not good idea, because in some cases can starts expression with call a method.

**Example:**

 `<Basic.LinkCell property="key" to="workflow/definitions/:key"/>`

 ->

 `<Basic.LinkCell property="key" to="/workflow/definitions/:key"/>`

## React router - query parameters
 **React router since V4 doesn't parse the query parameters** (`.../?new=1`).

In CzechIdM 10 was implemented parsing in **AbstractContextComponent._parseUrlQuery**.
Parsing is executed in constructor and result query object is sets to the `this.props.location.query` (for ensure back compatibility).

**Beware** `this.props.location.query` could be **null**!

Because Redux calling **Select** method before constructor of **AbstractContextComponent**. You have to prevent of throwing of the **null pointer**, typicaly in select method.

**For example**:

 `component.location ? component.location.query.automaticRoleId` should be modified on `component.location && component.location.query ? component.location.query.automaticRoleId : null;`

## Method checkAccess removed
Method **checkAccess** in the SecurityManager was removed. This logic was moved to the `AbstractContextComponent._getComponent(route)`.


## React bootstrap - Modal component bug

**Modal** component from react-bootstrap contains bug! Modal dialog is show before subcomponents are rendered. So if you call **setState** for show a modal dialog and in callback you want to set focus on some component (`this.refs.firstName.focus()`), then you will obtain **exception**, because the ref for that component will be null.

I found ugly but working solution, it is the timeout (10ms). This timeout is implemented in **AbstractTableContent.showDetail**. It means, if you use this method, your modal dialog should be correctly rendered. If you showing modal dialog **manually**, then **you have to add timeout or use method showDialog**.

**Here is example:**

_Not working:_
<pre><code class="text">
  this.setState({
      detail: {
        show: true,
        entity
      }
    }, () => {
      this.refs.generatorType.focus();
    });
</code></pre>

_Working (using showDetail from AbstractTableContent):_
<pre><code class="text">
  super.showDetail(entity, () => {
      this.refs.generatorType.focus();
    });
</code></pre>

_Working (manually added timeout):_
<pre><code class="text">
  super.showDetail(entity, () => {
      setTimeout(() => {
        this.refs.generatorType.focus();
      }, 10);
    });
</code></pre>
