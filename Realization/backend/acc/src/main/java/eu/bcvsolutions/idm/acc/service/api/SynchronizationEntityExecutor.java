package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;
import java.util.UUID;

import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.acc.domain.ReconciliationMissingAccountActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationItemBuilder;
import eu.bcvsolutions.idm.acc.domain.SynchronizationLinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationMissingEntityActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationUnlinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysSyncActionLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig;
import eu.bcvsolutions.idm.acc.entity.SysSyncItemLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncLog;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractLongRunningTaskExecutor;
import eu.bcvsolutions.idm.ic.api.IcAttribute;

/**
 * API for synchronization executor
 * @author svandav
 *
 */
public interface SynchronizationEntityExecutor extends Plugin<SystemEntityType> {

	SysSyncConfig process(UUID synchronizationConfigId);

	boolean doItemSynchronization(SynchronizationItemBuilder wrapper);

	void setLongRunningTaskExecutor(AbstractLongRunningTaskExecutor<SysSyncConfig> longRunningTaskExecutor);

	/**
	 * Method for resolve missing account situation for one item.
	 */
	public void resolveMissingAccountSituation(String uid, AccAccount account, SystemEntityType entityType,
			ReconciliationMissingAccountActionType action, SysSystem system, SysSyncLog log, SysSyncItemLog logItem,
			List<SysSyncActionLog> actionLogs);

	/**
	 * Method for resolve unlinked situation for one item.
	 */
	void resolveUnlinkedSituation(String uid, UUID entityId, SystemEntityType entityType, SysSystemEntity systemEntity,
			SynchronizationUnlinkedActionType action, SysSystem system, SysSyncLog log, SysSyncItemLog logItem,
			List<SysSyncActionLog> actionLogs);

	/**
	 * Method for resolve missing entity situation for one item.
	 */
	void resolveMissingEntitySituation(String uid, SystemEntityType entityType,
			List<SysSystemAttributeMapping> mappedAttributes, SysSystem system,
			SynchronizationMissingEntityActionType actionType, SysSyncLog log, SysSyncItemLog logItem,
			List<SysSyncActionLog> actionLogs, List<IcAttribute> icAttributes);

	/**
	 * Method for resolve linked situation for one item.
	 */
	void resolveLinkedSituation(String uid, SystemEntityType entityType, List<IcAttribute> icAttributes,
			List<SysSystemAttributeMapping> mappedAttributes, AccAccount account,
			SynchronizationLinkedActionType action, SysSyncLog log, SysSyncItemLog logItem,
			List<SysSyncActionLog> actionLogs);

}