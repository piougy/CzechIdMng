package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.acc.domain.ReconciliationMissingAccountActionType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysSyncActionLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncItemLog;
import eu.bcvsolutions.idm.acc.entity.SysSynchronizationConfig;
import eu.bcvsolutions.idm.acc.entity.SysSynchronizationLog;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.impl.IcSyncDeltaTypeEnum;

public interface SynchronizationService {

	public static final String WF_VARIABLE_KEY_UID = "uid";
	public static final String WF_VARIABLE_KEY_ENTITY_TYPE = "entityType";
	public static final String WF_VARIABLE_KEY_SYNC_SITUATION = "situation";
	public static final String WF_VARIABLE_KEY_IC_ATTRIBUTES = "icAttributes";
	public static final String WF_VARIABLE_KEY_ACTION_TYPE = "actionType";
	public static final String WF_VARIABLE_KEY_SYNC_CONFIG_ID = "syncConfigId";
	public static final String WF_VARIABLE_KEY_ENTITY_ID = "entityId";
	public static final String WF_VARIABLE_KEY_ACC_ACCOUNT_ID = "accountId";
	public static final String WF_VARIABLE_KEY_LOG_ITEM = "logItem";

	SysSynchronizationConfig synchronization(SysSynchronizationConfig config);

	boolean doItemSynchronization(String uid, IcConnectorObject icObject, IcSyncDeltaTypeEnum type,
			SysSynchronizationConfig config, SysSystem system, SystemEntityType entityType,
			List<SysSystemAttributeMapping> mappedAttributes, SysSynchronizationLog log, SysSyncItemLog logItem,
			List<SysSyncActionLog> actionLogs);

	void resolveMissingAccountSituation(String uid, AccAccount account, SystemEntityType entityType,
			ReconciliationMissingAccountActionType action, SysSystem system, SysSynchronizationLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs);

	SysSyncItemLog resolveMissingEntitySituation(String uid, SystemEntityType entityType,
			List<IcAttribute> icAttributes, UUID configId, String actionType);

	SysSyncItemLog resolveLinkedSituation(String uid, SystemEntityType entityType, List<IcAttribute> icAttributes,
			UUID accountId, UUID configId, String actionType);

	SysSyncItemLog resolveUnlinkedSituation(String uid, SystemEntityType entityType, UUID entityId, UUID configId,
			String actionType);

	SysSyncItemLog resolveMissingAccountSituation(String uid, SystemEntityType entityType, UUID accountId,
			UUID configId, String actionType);

}