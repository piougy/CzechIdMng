package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.acc.domain.SynchronizationEventType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationContext;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig;
import eu.bcvsolutions.idm.acc.entity.SysSyncItemLog;
import eu.bcvsolutions.idm.acc.event.processor.synchronization.SynchronizationCancelProcessor;
import eu.bcvsolutions.idm.acc.event.processor.synchronization.SynchronizationStartProcessor;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskExecutor;
import eu.bcvsolutions.idm.ic.api.IcAttribute;

/**
 * Service for do synchronization and reconciliation
 * @author svandav
 *
 */
public interface SynchronizationService extends LongRunningTaskExecutor<SysSyncConfig> {

	public static final String WF_VARIABLE_KEY_UID = "uid";
	public static final String WF_VARIABLE_KEY_ENTITY_TYPE = "entityType";
	public static final String WF_VARIABLE_KEY_SYNC_SITUATION = "situation";
	public static final String WF_VARIABLE_KEY_IC_ATTRIBUTES = "icAttributes";
	public static final String WF_VARIABLE_KEY_ACTION_TYPE = "actionType";
	public static final String WF_VARIABLE_KEY_SYNC_CONFIG_ID = "syncConfigId";
	public static final String WF_VARIABLE_KEY_ENTITY_ID = "entityId";
	public static final String WF_VARIABLE_KEY_ACC_ACCOUNT_ID = "accountId";
	public static final String WF_VARIABLE_KEY_LOG_ITEM = "logItem";
	public static final String WRAPPER_SYNC_ITEM = "wrapper_sync_item";
	public static final String RESULT_SYNC_ITEM = "result_sync_item";

	/**
	 * Main method for start synchronization by given configuration.
	 * This method produces event  {@link SynchronizationEventType.START}.
	 * @param config
	 * @return
	 */
	SysSyncConfig startSynchronizationEvent(SysSyncConfig config);
	
	/**
	 * Main method for cancel running synchronization by given configuration.
	 * This method produces event  {@link SynchronizationEventType.CANCEL}.
	 * @param config
	 * @return
	 */
	SysSyncConfig stopSynchronizationEvent(SysSyncConfig config);
	
	/**
	 * Default implementation of synchronization. By default is call from {@link SynchronizationStartProcessor}
	 * as reaction on {@link SynchronizationEventType.START} event.
	 * @param config
	 * @return
	 */
	void startSynchronization(SysSyncConfig config);
	
	/**
	 * Default implementation cancel running synchronization. By default is call from {@link SynchronizationCancelProcessor}
	 * as reaction on {@link SynchronizationEventType.CANCEL} event.
	 * @param config
	 * @return
	 */
	SysSyncConfig stopSynchronization(SysSyncConfig config);

	/**
	 * Basic method for item synchronization. Item is obtained from target resource (searched). This method
	 * is called for synchronization and for reconciliation too.
	 * Every call this method starts new transaction. 
	 * @param wrapper for easier handling required attributes for item synchronization
	 * @return If is true, then well be synchronization continued, if is false, then will be cancel.
	 */
	boolean doItemSynchronization(SynchronizationContext wrapper);
	
	/**
	 * Public method for resolve missing entity situation for one item.
	 * This method can be call outside main synchronization loop. For example from workflow process.
	 * @param uid Item identification
	 * @param entityType Type of resolve entity
	 * @param icAttributes List of attributes given from target resource
	 * @param configId Id of {@link SysSyncConfig}
	 * @param actionType Action for this situation.
	 * @return
	 */
	SysSyncItemLog resolveMissingEntitySituation(String uid, SystemEntityType entityType,
			List<IcAttribute> icAttributes, UUID configId, String actionType);

	/**
	 * Public method for resolve linked situation for one item.
	 * This method can be call outside main synchronization loop. For example from workflow process.
	 * @param uid Item identification
	 * @param entityType Type of resolve entity
	 * @param icAttributes List of attributes given from target resource
	 * @param configId Id of {@link SysSyncConfig}
	 * @param actionType Action for this situation.
	 * @return
	 */
	SysSyncItemLog resolveLinkedSituation(String uid, SystemEntityType entityType, List<IcAttribute> icAttributes,
			UUID accountId, UUID configId, String actionType);

	/**
	 * Public method for resolve unlinked situation for one item.
	 * This method can be call outside main synchronization loop. For example from workflow process.
	 * @param uid Item identification
	 * @param entityType Type of resolve entity
	 * @param icAttributes List of attributes given from target resource
	 * @param configId Id of {@link SysSyncConfig}
	 * @param actionType Action for this situation.
	 * @return
	 */
	SysSyncItemLog resolveUnlinkedSituation(String uid, SystemEntityType entityType, UUID entityId, UUID configId,
			String actionType);

	/**
	 * Public method for resolve missing account situation for one item.
	 * This method can be call outside main synchronization loop. For example from workflow process.
	 * @param uid Item identification
	 * @param entityType Type of resolve entity
	 * @param icAttributes List of attributes given from target resource
	 * @param configId Id of {@link SysSyncConfig}
	 * @param actionType Action for this situation.
	 * @return
	 */
	SysSyncItemLog resolveMissingAccountSituation(String uid, SystemEntityType entityType, UUID accountId,
			UUID configId, String actionType);

	/**
	 * Long running task input parameter
	 * 
	 * @param synchronizationConfigId
	 */
	void setSynchronizationConfigId(UUID synchronizationConfigId);
}