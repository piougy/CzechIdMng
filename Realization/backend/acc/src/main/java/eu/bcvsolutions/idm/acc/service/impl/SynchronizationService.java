package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSynchronizationConfig;
import eu.bcvsolutions.idm.acc.entity.SysSynchronizationLog;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.ic.api.IcSyncDelta;

public interface SynchronizationService {

	SysSynchronizationConfig synchronization(SysSynchronizationConfig config);

	boolean doItemSynchronization(SysSynchronizationConfig config, SysSystem system, SystemEntityType entityType,
			List<SysSystemAttributeMapping> mappedAttributes, SysSynchronizationLog log, IcSyncDelta delta);

}