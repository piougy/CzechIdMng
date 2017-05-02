package eu.bcvsolutions.idm.acc.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysSyncActionLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig;
import eu.bcvsolutions.idm.acc.entity.SysSyncItemLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncLog;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.impl.IcSyncDeltaTypeEnum;

/**
 * 
 * Wrapper for synchronization item. Only for easier handling attributes needs for item synchronization.  
 * @author svandav
 *  
 * @param uid Identification of item on target resource
 * @param icObject Object from target resource
 * @param type Type of synchronization (Create, Update, Delete)
 * @param config Configuration of synchronization
 * @param system Idm system for synchronization
 * @param entityType Type of entity (Identity, Groupe, ...)
 * @param mappedAttributes Mapped attribute for this IDM system
 * @param account AccAccount
 * @param log Log for full synchronization
 * @param logItem Log for this item
 * @param actionLogs Relations between item log and full log and action.
 *
 */
public class SynchronizationItemBuilder implements Serializable {

	private static final long serialVersionUID = 1L;
	private String uid;
	private IcConnectorObject icObject;
	private IcSyncDeltaTypeEnum type;
	private SysSyncConfig config;
	private SysSystem system;
	private SystemEntityType entityType;
	private List<SysSystemAttributeMapping> mappedAttributes;
	private AccAccount account;
	private SysSyncLog log;
	private SysSyncItemLog logItem;
	private List<SysSyncActionLog> actionLogs;
	private UUID entityId;
	private boolean exportAction = false;
	
	public String getUid() {
		return uid;
	}
	public SynchronizationItemBuilder addUid(String uid) {
		this.uid = uid;
		return this;
	}
	public IcConnectorObject getIcObject() {
		return icObject;
	}
	public SynchronizationItemBuilder addIcObject(IcConnectorObject icObject) {
		this.icObject = icObject;
		return this;
	}
	public IcSyncDeltaTypeEnum getType() {
		return type;
	}
	public SynchronizationItemBuilder addType(IcSyncDeltaTypeEnum type) {
		this.type = type;
		return this;
	}
	public SysSyncConfig getConfig() {
		return config;
	}
	public SynchronizationItemBuilder addConfig(SysSyncConfig config) {
		this.config = config;
		return this;
	}
	public SysSystem getSystem() {
		return system;
	}
	public SynchronizationItemBuilder addSystem(SysSystem system) {
		this.system = system;
		return this;
	}
	public SystemEntityType getEntityType() {
		return entityType;
	}
	public SynchronizationItemBuilder addEntityType(SystemEntityType entityType) {
		this.entityType = entityType;
		return this;
	}
	public List<SysSystemAttributeMapping> getMappedAttributes() {
		return mappedAttributes;
	}
	public SynchronizationItemBuilder addMappedAttributes(List<SysSystemAttributeMapping> mappedAttributes) {
		this.mappedAttributes = mappedAttributes;
		return this;
	}
	public AccAccount getAccount() {
		return account;
	}
	public SynchronizationItemBuilder addAccount(AccAccount account) {
		this.account = account;
		return this;
	}
	public SysSyncLog getLog() {
		return log;
	}
	public SynchronizationItemBuilder addLog(SysSyncLog log) {
		this.log = log;
		return this;
	}
	public SysSyncItemLog getLogItem() {
		return logItem;
	}
	public SynchronizationItemBuilder addLogItem(SysSyncItemLog logItem) {
		this.logItem = logItem;
		return this;
	}
	public List<SysSyncActionLog> getActionLogs() {
		return actionLogs;
	}
	public SynchronizationItemBuilder addActionLogs(List<SysSyncActionLog> actionLogs) {
		this.actionLogs = actionLogs;
		return this;
	}

	public UUID getEntityId() {
		return entityId;
	}

	public SynchronizationItemBuilder addEntityId(UUID entityId) {
		this.entityId = entityId;
		return this;
	}

	public boolean isExportAction() {
		return exportAction;
	}

	public SynchronizationItemBuilder addExportAction(boolean exportAction) {
		this.exportAction = exportAction;
		return this;
	}
	
}
