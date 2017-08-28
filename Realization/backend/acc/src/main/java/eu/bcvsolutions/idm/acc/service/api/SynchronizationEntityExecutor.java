package eu.bcvsolutions.idm.acc.service.api;

import java.util.UUID;

import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.acc.domain.ReconciliationMissingAccountActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationContext;
import eu.bcvsolutions.idm.acc.domain.SynchronizationLinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationMissingEntityActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationUnlinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysSyncConfigDto;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractLongRunningTaskExecutor;

/**
 * API for synchronization executor
 * @author svandav
 *
 */
public interface SynchronizationEntityExecutor extends Plugin<SystemEntityType> {

	SysSyncConfigDto process(UUID synchronizationConfigId);

	/**
	 * Do synchronization for one item
	 * @param itemContext
	 * @return
	 */
	boolean doItemSynchronization(SynchronizationContext itemContext);

	void setLongRunningTaskExecutor(AbstractLongRunningTaskExecutor<SysSyncConfigDto> longRunningTaskExecutor);

	/**
	 * Method for resolve missing account situation for one item.
	 */
	public void resolveMissingAccountSituation(ReconciliationMissingAccountActionType action, SynchronizationContext context);

	/**
	 * Method for resolve unlinked situation for one item.
	 */
	void resolveUnlinkedSituation(SynchronizationUnlinkedActionType action, SynchronizationContext context);

	/**
	 * Method for resolve missing entity situation for one item.
	 */
	void resolveMissingEntitySituation(SynchronizationMissingEntityActionType actionType,  SynchronizationContext context);

	/**
	 * Method for resolve linked situation for one item.
	 */
	void resolveLinkedSituation(SynchronizationLinkedActionType action, SynchronizationContext context);

}