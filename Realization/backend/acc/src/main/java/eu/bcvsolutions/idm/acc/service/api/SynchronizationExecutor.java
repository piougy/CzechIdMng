package eu.bcvsolutions.idm.acc.service.api;

import java.util.UUID;

import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.acc.domain.SynchronizationItemBuilder;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractLongRunningTaskExecutor;

/**
 * API for synchronization executor
 * @author svandav
 *
 */
public interface SynchronizationExecutor extends Plugin<SystemEntityType> {

	SysSyncConfig process(UUID synchronizationConfigId);

	boolean doItemSynchronization(SynchronizationItemBuilder wrapper);

	void setLongRunningTaskExecutor(AbstractLongRunningTaskExecutor<SysSyncConfig> longRunningTaskExecutor);

}