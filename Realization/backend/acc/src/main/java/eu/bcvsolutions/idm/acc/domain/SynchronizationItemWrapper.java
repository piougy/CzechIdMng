package eu.bcvsolutions.idm.acc.domain;

import java.io.Serializable;
import java.util.List;

import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysSyncActionLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncItemLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig;
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
public class SynchronizationItemWrapper implements Serializable {

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
	
	public SynchronizationItemWrapper(String uid, IcConnectorObject icObject, IcSyncDeltaTypeEnum type,
			SysSyncConfig config, SysSystem system, SystemEntityType entityType,
			List<SysSystemAttributeMapping> mappedAttributes, AccAccount account, SysSyncLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs) {
		super();
		this.uid = uid;
		this.icObject = icObject;
		this.type = type;
		this.config = config;
		this.system = system;
		this.entityType = entityType;
		this.mappedAttributes = mappedAttributes;
		this.account = account;
		this.log = log;
		this.logItem = logItem;
		this.actionLogs = actionLogs;
	}
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public IcConnectorObject getIcObject() {
		return icObject;
	}
	public void setIcObject(IcConnectorObject icObject) {
		this.icObject = icObject;
	}
	public IcSyncDeltaTypeEnum getType() {
		return type;
	}
	public void setType(IcSyncDeltaTypeEnum type) {
		this.type = type;
	}
	public SysSyncConfig getConfig() {
		return config;
	}
	public void setConfig(SysSyncConfig config) {
		this.config = config;
	}
	public SysSystem getSystem() {
		return system;
	}
	public void setSystem(SysSystem system) {
		this.system = system;
	}
	public SystemEntityType getEntityType() {
		return entityType;
	}
	public void setEntityType(SystemEntityType entityType) {
		this.entityType = entityType;
	}
	public List<SysSystemAttributeMapping> getMappedAttributes() {
		return mappedAttributes;
	}
	public void setMappedAttributes(List<SysSystemAttributeMapping> mappedAttributes) {
		this.mappedAttributes = mappedAttributes;
	}
	public AccAccount getAccount() {
		return account;
	}
	public void setAccount(AccAccount account) {
		this.account = account;
	}
	public SysSyncLog getLog() {
		return log;
	}
	public void setLog(SysSyncLog log) {
		this.log = log;
	}
	public SysSyncItemLog getLogItem() {
		return logItem;
	}
	public void setLogItem(SysSyncItemLog logItem) {
		this.logItem = logItem;
	}
	public List<SysSyncActionLog> getActionLogs() {
		return actionLogs;
	}
	public void setActionLogs(List<SysSyncActionLog> actionLogs) {
		this.actionLogs = actionLogs;
	}
	
	
}
