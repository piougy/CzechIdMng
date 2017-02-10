package eu.bcvsolutions.idm.acc.scheduler.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractSchedulableTaskExecutor;

/**
 * Synchronization start
 * 
 * @author Radek Tomiška
 *
 */
@Service
@Description("Synchronization scheduling - publishes start event only")
public class SynchronizationSchedulableTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {

	private static final String PARAMETER_UUID = "Synchronization uuid";
	@Autowired
	private SynchronizationService synchronizationService;
	@Autowired
	SysSyncConfigService service;
	//
	private UUID synchronizationId;
	
	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		synchronizationId = getParameterConverter().toUuid(properties, PARAMETER_UUID);
		//
		// validation only
		getConfig();
	}
	
	@Override
	public Boolean process() {
		synchronizationService.startSynchronizationEvent(getConfig());	
		return true;
	}
	
	private SysSyncConfig getConfig() {
		SysSyncConfig config = service.get(synchronizationId);
		//
		if (config == null) {
			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_NOT_FOUND,
					ImmutableMap.of("id", synchronizationId));
		}
		return config;
	}
	
	@Override
	public List<String> getParameterNames() {
		List<String> params = super.getParameterNames();
		params.add(PARAMETER_UUID);
		return params;
	}

}
