package eu.bcvsolutions.idm.acc.connector;

import com.google.common.collect.ImmutableMap;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.ConnectorTypeDto;
import eu.bcvsolutions.idm.acc.dto.SysConnectorKeyDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaObjectClassFilter;
import eu.bcvsolutions.idm.acc.service.api.ConnectorManager;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.ic.api.IcAttributeInfo;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcObjectClassInfo;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * CSV connector type extends standard CSV connector for more metadata (image, wizard, ...).
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
@Component(CsvConnectorType.NAME)
public class CsvConnectorType extends AbstractConnectorType {

	public static final String NAME = "csv-connector-type";
	public static final String STEP_ONE_CREATE_SYSTEM = "csvStepOne";
	public static final String STEP_TWO_SELECT_PK = "csvStepTwo";
	public static final String FILE_PATH = "filePath";
	public static final String PRIMARY_SCHEMA_ATTRIBUTE = "primarySchemaAttributeId";
	public static final String SYSTEM_NAME = "name";
	public static final String SEPARATOR = "separator";
	public static final String SCHEMA_ID_KEY = "schemaId";
	public static final String CONNECTOR_SOURCE_PATH = "sourcePath";
	public static final String CONNECTOR_SEPARATOR = "separator";
	public static final String CONNECTOR_UID = "uid";
	public static final String CONNECTOR_INCLUDES_HEADERS = "includesHeader";

	@Autowired
	private AttachmentManager attachmentManager;
	@Autowired
	private ConnectorManager connectorManager;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private FormService formService;
	@Autowired
	private SysSchemaObjectClassService objectClassService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;

	@Override
	public String getConnectorName() {
		return "eu.bcvsolutions.idm.connectors.csv.CSVConnConnector";
	}

	@Override
	public String getIconKey() {
		return "csv";
	}

	public ConnectorTypeDto deployCsv(IdmAttachmentDto attachment, String goal) {

		Path source = Paths.get(attachmentManager.getStoragePath(), attachment.getContentPath());
		String finalPath = source.toString();
		if (Strings.isNotBlank(goal)) {
			Path goalPath = Paths.get(goal);
			String lastPart = goalPath.getFileName().toString();
			if (lastPart != null && lastPart.contains(".")) {
				// Last part is file -> we need cut of it.
				goalPath = goalPath.getParent();
			}
			Path target = Paths.get(goalPath.toString(), attachment.getName());
			// Move CSV to path defined by user.
			try {
				finalPath = Files.move(source, target, StandardCopyOption.REPLACE_EXISTING).toString();
			} catch (IOException e) {
				throw new ResultCodeException(AccResultCode.WIZARD_CSV_CONNECTOR_UPLOAD_FAILED, e);
			}
		}
		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(this);
		connectorTypeDto.getMetadata().put(FILE_PATH, finalPath);

		return connectorTypeDto;
	}


	@Override
	public Map<String, String> getMetadata() {
		// Default values:
		Map<String, String> metadata = super.getMetadata();
		String filePath = String.format("%s/", attachmentManager.getStoragePath());
		metadata.put(FILE_PATH, Paths.get(filePath).toString());
		metadata.put(SYSTEM_NAME, this.findUniqueSystemName("CSV system", 1));
		metadata.put(SEPARATOR, ";");

		return metadata;
	}

	@Override
	@Transactional
	public ConnectorTypeDto execute(ConnectorTypeDto connectorType) {
		super.execute(connectorType);
		if (STEP_ONE_CREATE_SYSTEM.equals(connectorType.getWizardStepName())) {
			executeStepOne(connectorType);
		} else if (STEP_TWO_SELECT_PK.equals(connectorType.getWizardStepName())) {
			executeStepTwo(connectorType);
		}
		return connectorType;
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

		// Find attribute with CSV file path.
		IdmFormDefinitionDto connectorFormDef = this.systemService
				.getConnectorFormDefinition(systemDto.getConnectorInstance());
		IdmFormAttributeDto csvFilePathAttribute = connectorFormDef.getMappedAttributeByCode(CONNECTOR_SOURCE_PATH);
		List<IdmFormValueDto> values = formService.getValues(systemDto, csvFilePathAttribute, IdmBasePermission.READ);
		if (values != null && values.size() == 1) {
			connectorType.getMetadata().put(FILE_PATH, values.get(0).getStringValue());
		}

		// Find attribute contains separator.
		IdmFormAttributeDto separatorAttribute = connectorFormDef.getMappedAttributeByCode(CONNECTOR_SEPARATOR);
		values = formService.getValues(systemDto, separatorAttribute, IdmBasePermission.READ);
		if (values != null && values.size() == 1) {
			connectorType.getMetadata().put(SEPARATOR, values.get(0).getStringValue());
		}

		// Find attribute contains primary identifier.
		IdmFormAttributeDto uidAttribute = connectorFormDef.getMappedAttributeByCode(CONNECTOR_UID);
		values = formService.getValues(systemDto, uidAttribute, IdmBasePermission.READ);
		if (values != null && values.size() == 1) {
			String uidColumnName = values.get(0).getStringValue();
			Assert.notNull(uidColumnName, "UID column name cannot be null!");
			SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
					schemaAttributeFilter.setSystemId(systemDto.getId());
					schemaAttributeFilter.setName(uidColumnName);
			List<SysSchemaAttributeDto> uidAttributes = schemaAttributeService.find(schemaAttributeFilter, null).getContent();
			if (uidAttributes.size() == 1) {
				connectorType.getMetadata().put(PRIMARY_SCHEMA_ATTRIBUTE, uidAttributes.get(0).getId().toString());
			}
		}

		return connectorType;
	}

	/**
	 * Execute first step of CSV wizard.
	 *
	 * @param connectorType
	 */
	private void executeStepOne(ConnectorTypeDto connectorType) {
		// Validations:
		String csvPath = connectorType.getMetadata().get(FILE_PATH);
		if (Strings.isBlank(csvPath)) {
			throw new ResultCodeException(AccResultCode.WIZARD_CSV_CONNECTOR_CSV_FILE_NOT_FOUND, ImmutableMap.of("path","-"));
		}
		Path csvFilePath = Paths.get(csvPath);
		if (!csvFilePath.toFile().exists()) {
			throw new ResultCodeException(AccResultCode.WIZARD_CSV_CONNECTOR_CSV_FILE_NOT_FOUND, ImmutableMap.of("path", csvPath));
		}
		String lastPart = csvFilePath.getFileName().toString();
		if (lastPart == null || !lastPart.contains(".")) {
			// Last part isn't file!
			throw new ResultCodeException(AccResultCode.WIZARD_CSV_CONNECTOR_CSV_FILE_NOT_FOUND, ImmutableMap.of("path", csvPath));
		}

		String systemId = connectorType.getMetadata().get(SYSTEM_DTO_KEY);
		SysSystemDto systemDto;
		if (systemId != null) {
			// System already exists.
			systemDto = systemService.get(UUID.fromString(systemId), IdmBasePermission.READ);
		} else {
			// Create new system.
			systemDto = new SysSystemDto();
		}
		systemDto.setName(connectorType.getMetadata().get(SYSTEM_NAME));
		// Find connector key and set it to the system.
		IcConnectorKey connectorKey = connectorManager.findConnectorKey(this.getConnectorName());
		Assert.notNull(connectorKey, "Connector key was not found!");
		systemDto.setConnectorKey(new SysConnectorKeyDto(connectorKey));
		systemDto = systemService.save(systemDto, IdmBasePermission.CREATE);

		// Put new system to the connector type (will be returned to FE).
		connectorType.getEmbedded().put(SYSTEM_DTO_KEY, systemDto);

		// Find and update attribute with CSV file path.
		IdmFormDefinitionDto connectorFormDef = this.systemService
				.getConnectorFormDefinition(systemDto.getConnectorInstance());
		IdmFormAttributeDto csvFilePathAttribute = connectorFormDef.getMappedAttributeByCode(CONNECTOR_SOURCE_PATH);
		List<Serializable> csvFileValue = new ArrayList<>();
		csvFileValue.add(csvFilePath.toString());
		formService.saveValues(systemDto, csvFilePathAttribute, csvFileValue);

		// Find and update attribute contains separator.
		IdmFormAttributeDto separatorAttribute = connectorFormDef.getMappedAttributeByCode(CONNECTOR_SEPARATOR);
		List<Serializable> separatorValue = new ArrayList<>();
		separatorValue.add(connectorType.getMetadata().get(SEPARATOR));
		formService.saveValues(systemDto, separatorAttribute, separatorValue);

		// Find and update attribute defines if headers are included in the file.
		IdmFormAttributeDto includeHeaderAttribute = connectorFormDef.getMappedAttributeByCode(CONNECTOR_INCLUDES_HEADERS);
		List<Serializable> includesHeaderValue = new ArrayList<>();
		includesHeaderValue.add(Boolean.TRUE);
		formService.saveValues(systemDto, includeHeaderAttribute, includesHeaderValue);

		// This is skipped for reopen case.
		if (!connectorType.isReopened()) {
			// Find and update attribute defines UID attribute.
			// UID attribute have to be filled before schema generating, but I don't know it (I need list of column first).
			// So UID attribute will be set to random value, and modified in next step.
			IdmFormAttributeDto uidAttribute = connectorFormDef.getMappedAttributeByCode(CONNECTOR_UID);
			List<Serializable> uidValue = new ArrayList<>();
			uidValue.add("...random...");
			formService.saveValues(systemDto, uidAttribute, uidValue);
			// Beware, potential danger for already existed system show in the wizard.
			// Load existing object class from system and delete them (because this wizard step could be repeated).
			SysSchemaObjectClassFilter objectClassFilter = new SysSchemaObjectClassFilter();
			objectClassFilter.setSystemId(systemDto.getId());
			objectClassService.find(objectClassFilter, null)
					.getContent()
					.forEach(sysSchemaObjectClassDto -> objectClassService.delete(sysSchemaObjectClassDto));

			// Generate schema
			List<SysSchemaObjectClassDto> schemas = this.systemService.generateSchema(systemDto);
			SysSchemaObjectClassDto schemaAccount = schemas.stream()
					.filter(schema -> IcObjectClassInfo.ACCOUNT.equals(schema.getObjectClassName())).findFirst()
					.orElse(null);
			Assert.notNull(schemaAccount, "We cannot found schema for ACCOUNT!");

			// Attribute __NAME__ is generate for random value. We need delete him now (will be generated in next wizard step).
			SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
			schemaAttributeFilter.setSystemId(systemDto.getId());
			Assert.notNull(schemaAccount.getId(), "Schema ID cannot be null!");
			schemaAttributeFilter.setObjectClassId(schemaAccount.getId());
			schemaAttributeFilter.setName(IcAttributeInfo.NAME);
			schemaAttributeService.find(schemaAttributeFilter, null)
					.getContent()
					.forEach(nameAttribute -> schemaAttributeService.delete(nameAttribute));

			connectorType.getMetadata().put(SCHEMA_ID_KEY, schemaAccount.getId().toString());
		} else {
			// Generate schema
			List<SysSchemaObjectClassDto> schemas = this.systemService.generateSchema(systemDto);
			SysSchemaObjectClassDto schemaAccount = schemas.stream()
					.filter(schema -> IcObjectClassInfo.ACCOUNT.equals(schema.getObjectClassName())).findFirst()
					.orElse(null);
			Assert.notNull(schemaAccount, "We cannot found schema for ACCOUNT!");
			connectorType.getMetadata().put(SCHEMA_ID_KEY, schemaAccount.getId().toString());
		}
	}

	/**
	 * Execute second step of CSV wizard.
	 *
	 * @param connectorType
	 */
	private void executeStepTwo(ConnectorTypeDto connectorType) {

		String schemaAttributeId = connectorType.getMetadata().get(PRIMARY_SCHEMA_ATTRIBUTE);
		Assert.notNull(schemaAttributeId, "Schema attribute ID cannot be null!");
		SysSchemaAttributeDto schemaAttributeDto = schemaAttributeService.get(UUID.fromString(schemaAttributeId), IdmBasePermission.READ);
		Assert.notNull(schemaAttributeDto, "Schema attribute cannot be null!");

		String systemId = connectorType.getMetadata().get(SYSTEM_DTO_KEY);
		Assert.notNull(systemId, "System ID cannot be null!");
		SysSystemDto systemDto = systemService.get(UUID.fromString(systemId), IdmBasePermission.READ);
		Assert.notNull(systemDto, "System cannot be null!");

		// Find and update attribute defines UID attribute.
		IdmFormDefinitionDto connectorFormDef = this.systemService
				.getConnectorFormDefinition(systemDto.getConnectorInstance());
		IdmFormAttributeDto uidAttribute = connectorFormDef.getMappedAttributeByCode(CONNECTOR_UID);
		List<Serializable> uidValue = new ArrayList<>();
		uidValue.add(schemaAttributeDto.getName());
		formService.saveValues(systemDto, uidAttribute, uidValue);

		// Generate schema - again, for create primary attribute __NAME__.
		List<SysSchemaObjectClassDto> schemas = this.systemService.generateSchema(systemDto);
		SysSchemaObjectClassDto schemaAccount = schemas.stream()
				.filter(schema -> IcObjectClassInfo.ACCOUNT.equals(schema.getObjectClassName())).findFirst()
				.orElse(null);
		Assert.notNull(schemaAccount, "We cannot found schema for ACCOUNT!");
	}

	@Override
	public int getOrder() {
		return 100;
	}
}
