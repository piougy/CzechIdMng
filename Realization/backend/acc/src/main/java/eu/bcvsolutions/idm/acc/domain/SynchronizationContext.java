package eu.bcvsolutions.idm.acc.domain;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncActionLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncItemLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.impl.IcSyncDeltaTypeEnum;

/**
 * 
 * Wrapper for synchronization item. Only for easier handling attributes needs
 * for item synchronization.
 * 
 * @author svandav
 * 
 * @param uid
 *            Identification of item on target resource
 * @param icObject
 *            Object from target resource
 * @param type
 *            Type of synchronization (Create, Update, Delete)
 * @param config
 *            Configuration of synchronization
 * @param system
 *            Idm system for synchronization
 * @param entityType
 *            Type of entity (Identity, Groupe, ...)
 * @param mappedAttributes
 *            Mapped attribute for this IDM system
 * @param account
 *            AccAccount
 * @param log
 *            Log for full synchronization
 * @param logItem
 *            Log for this item
 * @param actionLogs
 *            Relations between item log and full log and action.
 *
 */
public class SynchronizationContext implements Serializable {

	private static final long serialVersionUID = 1L;
	private String uid;
	private IcConnectorObject icObject;
	private IcSyncDeltaTypeEnum type;
	private SysSyncConfigDto config;
	private SysSystemDto system;
	private SystemEntityType entityType;
	private List<SysSystemAttributeMappingDto> mappedAttributes;
	private AccAccountDto account;
	private SysSyncLogDto log;
	private SysSyncItemLogDto logItem;
	private List<SysSyncActionLogDto> actionLogs;
	private UUID entityId;
	private boolean exportAction = false;
	private AttributeMapping tokenAttribute;
	private IcConnectorConfiguration connectorConfig;
	private SysSystemEntityDto systemEntity;
	private SynchronizationActionType actionType;

	public String getUid() {
		return uid;
	}

	public SynchronizationContext addUid(String uid) {
		this.uid = uid;
		return this;
	}

	public IcConnectorObject getIcObject() {
		return icObject;
	}

	public SynchronizationContext addIcObject(IcConnectorObject icObject) {
		this.icObject = icObject;
		return this;
	}

	public IcSyncDeltaTypeEnum getType() {
		return type;
	}

	public SynchronizationContext addType(IcSyncDeltaTypeEnum type) {
		this.type = type;
		return this;
	}

	public SysSyncConfigDto getConfig() {
		return config;
	}

	public SynchronizationContext addConfig(SysSyncConfigDto config) {
		this.config = config;
		return this;
	}

	public SysSystemDto getSystem() {
		return system;
	}

	public SynchronizationContext addSystem(SysSystemDto system) {
		this.system = system;
		return this;
	}

	public SystemEntityType getEntityType() {
		return entityType;
	}

	public SynchronizationContext addEntityType(SystemEntityType entityType) {
		this.entityType = entityType;
		return this;
	}

	public List<SysSystemAttributeMappingDto> getMappedAttributes() {
		return mappedAttributes;
	}

	public SynchronizationContext addMappedAttributes(List<SysSystemAttributeMappingDto> mappedAttributes) {
		this.mappedAttributes = mappedAttributes;
		return this;
	}

	public AccAccountDto getAccount() {
		return account;
	}

	public SynchronizationContext addAccount(AccAccountDto account) {
		this.account = account;
		return this;
	}

	public SysSyncLogDto getLog() {
		return log;
	}

	public SynchronizationContext addLog(SysSyncLogDto log) {
		this.log = log;
		return this;
	}

	public SysSyncItemLogDto getLogItem() {
		return logItem;
	}

	public SynchronizationContext addLogItem(SysSyncItemLogDto logItem) {
		this.logItem = logItem;
		return this;
	}

	public List<SysSyncActionLogDto> getActionLogs() {
		return actionLogs;
	}

	public SynchronizationContext addActionLogs(List<SysSyncActionLogDto> actionLogs) {
		this.actionLogs = actionLogs;
		return this;
	}

	public UUID getEntityId() {
		return entityId;
	}

	public SynchronizationContext addEntityId(UUID entityId) {
		this.entityId = entityId;
		return this;
	}

	public boolean isExportAction() {
		return exportAction;
	}

	public SynchronizationContext addExportAction(boolean exportAction) {
		this.exportAction = exportAction;
		return this;
	}

	public SynchronizationContext addTokenAttribute(AttributeMapping tokenAttribute) {
		this.tokenAttribute = tokenAttribute;
		return this;
	}

	public AttributeMapping getTokenAttribute() {
		return tokenAttribute;
	}

	public SynchronizationContext addConnectorConfig(IcConnectorConfiguration connectorConfig) {
		this.connectorConfig = connectorConfig;
		return this;
	}

	public IcConnectorConfiguration getConnectorConfig() {
		return connectorConfig;
	}
	
	public SynchronizationContext addSystemEntity(SysSystemEntityDto systemEntity){
		this.systemEntity = systemEntity;
		return this;
	}
	
	public SysSystemEntityDto getSystemEntity() {
		return systemEntity;
	}
	
	public SynchronizationActionType getActionType() {
		return actionType;
	}

	public SynchronizationContext addActionType(SynchronizationActionType actionType) {
		this.actionType = actionType;
		return this;
	}

	public static SynchronizationContext cloneContext(SynchronizationContext context){
		SynchronizationContext newContext = new SynchronizationContext();
		newContext.addAccount(context.getAccount())
		.addActionLogs(context.getActionLogs())
		.addConfig(context.getConfig())
		.addConnectorConfig(context.getConnectorConfig())
		.addEntityId(context.getEntityId())
		.addEntityType(context.getEntityType())
		.addExportAction(context.isExportAction())
		.addIcObject(context.getIcObject())
		.addLog(context.getLog())
		.addLogItem(context.getLogItem())
		.addMappedAttributes(context.getMappedAttributes())
		.addSystem(context.getSystem())
		.addTokenAttribute(context.getTokenAttribute())
		.addType(context.getType())
		.addSystemEntity(context.getSystemEntity())
		.addUid(context.getUid())
		.addActionType(context.getActionType());
		
		return newContext;
	}

}
