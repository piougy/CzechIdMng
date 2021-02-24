package eu.bcvsolutions.idm.acc.connector;

import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.ConnectorTypeDto;
import eu.bcvsolutions.idm.acc.dto.SysConnectorKeyDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.service.api.ConnectorManager;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcObjectClassInfo;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Abstract connector type for all JDBC wizards.
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
public abstract class AbstractJdbcConnectorType extends DefaultConnectorType {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractJdbcConnectorType.class);

	public static final String STEP_ONE_CREATE_SYSTEM = "jdbcStepOne";
	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String DATABASE = "database";
	public static final String USER = "user";
	public static final String PASSWORD = "password";
	public static final String TABLE = "table";
	public static final String KEY_COLUMN = "keyColumn";
	public static final String JDBC_DRIVER = "jdbcDriver";
	public static final String JDBC_URL_TEMPLATE = "jdbcUrlTemplate";
	public static final String SYSTEM_NAME = "name";
	public static final String SCHEMA_ID_KEY = "schemaId";

	@Autowired
	private ConnectorManager connectorManager;
	@Autowired
	private SysSystemMappingService systemMappingService;
	@Autowired
	private SysSyncConfigService syncConfigService;

	@Override
	public String getConnectorName() {
		return "net.tirasa.connid.bundles.db.table.DatabaseTableConnector";
	}

	@Override
	public boolean supports() {
		try {
			Class.forName(this.getJdbcDriverName());
		} catch (ClassNotFoundException e) {
			LOG.info(MessageFormat.format("JDBC driver [{0}] wasn't found. This connector type [{1}] is not supports now.",
					this.getJdbcDriverName(), this.getId()));
			return false;
		}
		return super.supports();
	}

	@Override
	public ConnectorTypeDto load(ConnectorTypeDto connectorType) {
		super.load(connectorType);
		if (!connectorType.isReopened()) {
			return connectorType;
		}
		// Load the system.
		SysSystemDto systemDto = (SysSystemDto) connectorType.getEmbedded().get(SYSTEM_DTO_KEY);
		Assert.notNull(systemDto, "System must exists!");
		connectorType.getMetadata().put(SYSTEM_NAME, systemDto.getName());
		Map<String, String> metadata = connectorType.getMetadata();

		IdmFormDefinitionDto connectorFormDef = this.getSystemService().getConnectorFormDefinition(systemDto);
		// Find attribute with port.
		metadata.put(PORT, getValueFromConnectorInstance(PORT, systemDto, connectorFormDef));
		// Find attribute with host.
		metadata.put(HOST, getValueFromConnectorInstance(HOST, systemDto, connectorFormDef));
		// Find attribute with database.
		metadata.put(DATABASE, getValueFromConnectorInstance(DATABASE, systemDto, connectorFormDef));
		// Find attribute with table.
		metadata.put(TABLE, getValueFromConnectorInstance(TABLE, systemDto, connectorFormDef));
		// Find attribute with key column.
		metadata.put(KEY_COLUMN, getValueFromConnectorInstance(KEY_COLUMN, systemDto, connectorFormDef));
		// Find attribute with user.
		metadata.put(USER, getValueFromConnectorInstance(USER, systemDto, connectorFormDef));

		// Load the mapping.
		SysSystemMappingFilter mappingFilter = new SysSystemMappingFilter();
		mappingFilter.setSystemId(systemDto.getId());
		SysSystemMappingDto mappingDto = systemMappingService.find(mappingFilter, null)
				.getContent()
				.stream().min(Comparator.comparing(SysSystemMappingDto::getCreated))
				.orElse(null);
		connectorType.getEmbedded().put(DefaultConnectorType.MAPPING_DTO_KEY, mappingDto);

		// Load the sync.
		SysSyncConfigFilter syncFilter = new SysSyncConfigFilter();
		syncFilter.setSystemId(systemDto.getId());
		if (mappingDto != null) {
			syncFilter.setSystemMappingId(mappingDto.getId());
		}
		AbstractSysSyncConfigDto syncDto = syncConfigService.find(syncFilter, null)
				.getContent()
				.stream().min(Comparator.comparing(AbstractDto::getCreated))
				.orElse(null);
		connectorType.getEmbedded().put(DefaultConnectorType.SYNC_DTO_KEY, syncDto);

		return connectorType;
	}

	@Override
	@Transactional
	public ConnectorTypeDto execute(ConnectorTypeDto connectorType) {
		super.execute(connectorType);
		if (STEP_ONE_CREATE_SYSTEM.equals(connectorType.getWizardStepName())) {
			executeStepOne(connectorType);
		}
		return connectorType;
	}

	/**
	 * Execute first step of JDBC wizard.
	 *
	 * @param connectorType
	 */
	private void executeStepOne(ConnectorTypeDto connectorType) {
		String port = connectorType.getMetadata().get(PORT);
		Assert.notNull(port, "Port cannot be null!");
		String host = connectorType.getMetadata().get(HOST);
		Assert.notNull(host, "Host cannot be null!");
		String database = connectorType.getMetadata().get(DATABASE);
		Assert.notNull(database, "Database cannot be null!");
		String table = connectorType.getMetadata().get(TABLE);
		Assert.notNull(table, "Table cannot be null!");
		String keyColumn = connectorType.getMetadata().get(KEY_COLUMN);
		Assert.notNull(keyColumn, "Key column cannot be null!");
		String user = connectorType.getMetadata().get(USER);
		Assert.notNull(user, "Username cannot be null!");
		String password = connectorType.getMetadata().get(PASSWORD);
		// Remove password from metadata.
		connectorType.getMetadata().remove(PASSWORD);

		String systemId = connectorType.getMetadata().get(SYSTEM_DTO_KEY);
		SysSystemDto systemDto;
		if (systemId != null) {
			// System already exists.
			systemDto = getSystemService().get(UUID.fromString(systemId), IdmBasePermission.READ);
		} else {
			// Create new system.
			systemDto = new SysSystemDto();
		}
		systemDto.setName(connectorType.getMetadata().get(SYSTEM_NAME));
		// Resolve remote system.
		systemDto.setRemoteServer(connectorType.getRemoteServer());
		// Find connector key and set it to the system.
		IcConnectorKey connectorKey = connectorManager.findConnectorKey(connectorType);
		Assert.notNull(connectorKey, "Connector key was not found!");
		systemDto.setConnectorKey(new SysConnectorKeyDto(connectorKey));
		systemDto = getSystemService().save(systemDto, IdmBasePermission.CREATE);

		// Put new system to the connector type (will be returned to FE).
		connectorType.getEmbedded().put(SYSTEM_DTO_KEY, systemDto);

		IdmFormDefinitionDto connectorFormDef = this.getSystemService().getConnectorFormDefinition(systemDto);
		// Set the port.
		this.setValueToConnectorInstance(PORT, port, systemDto, connectorFormDef);
		// Set the host.
		this.setValueToConnectorInstance(HOST, host, systemDto, connectorFormDef);
		// Set the database.
		this.setValueToConnectorInstance(DATABASE, database, systemDto, connectorFormDef);
		// Set the table.
		this.setValueToConnectorInstance(TABLE, table, systemDto, connectorFormDef);
		// Set the user.
		this.setValueToConnectorInstance(USER, user, systemDto, connectorFormDef);
		// Set the password.
		// Password is mandatory only if none exists in connector configuration.
		String passwordInSystem = this.getValueFromConnectorInstance(PASSWORD, systemDto, connectorFormDef);
		if (Strings.isNotBlank(password) && !GuardedString.SECRED_PROXY_STRING.equals(password)) {
			this.setValueToConnectorInstance(PASSWORD, password, systemDto, connectorFormDef);
		}else {
			Assert.notNull(passwordInSystem, "Password cannot be null!");
		}
		// Set the JDBC driver.
		this.setValueToConnectorInstance(JDBC_DRIVER, getJdbcDriverName(), systemDto, connectorFormDef);
		// Set the JDBC url template.
		this.setValueToConnectorInstance(JDBC_URL_TEMPLATE, getJdbcUrlTemplate(), systemDto, connectorFormDef);
		// Set the column with PK.
		this.setValueToConnectorInstance(KEY_COLUMN, keyColumn, systemDto, connectorFormDef);

		// Generate schema
		List<SysSchemaObjectClassDto> schemas = this.getSystemService().generateSchema(systemDto);
		SysSchemaObjectClassDto schemaAccount = schemas.stream()
				.filter(schema -> IcObjectClassInfo.ACCOUNT.equals(schema.getObjectClassName())).findFirst()
				.orElse(null);
		Assert.notNull(schemaAccount, "We cannot found schema for ACCOUNT!");
		connectorType.getMetadata().put(SCHEMA_ID_KEY, schemaAccount.getId().toString());
	}

	@Override
	public boolean hideParentConnector() {
		return false;
	}

	@Override
	public boolean supportsSystem(SysSystemDto systemDto) {
		if (!super.supportsSystem(systemDto)) {
			return false;
		}

		IdmFormDefinitionDto connectorFormDef = this.getSystemService().getConnectorFormDefinition(systemDto);
		// Find attribute with drive name.
		String jdbcDriverName = getValueFromConnectorInstance(JDBC_DRIVER, systemDto, connectorFormDef);

		return this.getJdbcDriverName().equals(jdbcDriverName);
	}

	/**
	 * Database drive name.
	 */
	public abstract String getJdbcDriverName();

	/**
	 * Database connection URL. The url is used to connect to database.
	 */
	public abstract String getJdbcUrlTemplate();
}
