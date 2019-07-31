package eu.bcvsolutions.idm.acc.scheduler.task.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.eav.domain.AccFaceType;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;

/**
 * Synchronization schedule task
 * 
 * @author Radek Tomiška
 * @author svandav
 *
 */
@Component(SynchronizationSchedulableTaskExecutor.TASK_NAME)
@Description("Synchronization scheduling - publishes start event only")
public class SynchronizationSchedulableTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {

	public static final String TASK_NAME = "acc-synchronization-long-running-task";
	//
	@Autowired private SynchronizationService synchronizationService;
	@Autowired private SysSyncConfigService service;
	//
	private UUID synchronizationId;

	public SynchronizationSchedulableTaskExecutor() {
		super();
	}

	public SynchronizationSchedulableTaskExecutor(UUID synchronizationId) {
		this.synchronizationId = synchronizationId;
	}
	
	@Override
	public String getName() {
		return TASK_NAME;
	}

	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		synchronizationId = getParameterConverter().toUuid(properties, SynchronizationService.PARAMETER_SYNCHRONIZATION_ID);
		//
		// validation only
	    getConfig();
	}

	@Override
	public Boolean process() {
		AbstractSysSyncConfigDto config = getConfig();
		if (service.isRunning(config)) {
			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_IS_RUNNING,
					ImmutableMap.of("name", config.getName()));
		}
		synchronizationService.startSynchronization(config, this);
		//
		return Boolean.TRUE;
	}

	private AbstractSysSyncConfigDto getConfig() {
		AbstractSysSyncConfigDto config = service.get(synchronizationId);
		//
		if (config == null) {
			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_NOT_FOUND,
					ImmutableMap.of("id", synchronizationId));
		}
		return config;
	}

	@Override
	public List<String> getPropertyNames() {
		List<String> params = super.getPropertyNames();
		params.add(SynchronizationService.PARAMETER_SYNCHRONIZATION_ID);
		return params;
	}

	@Override
	public String getDescription() {
		String deafultDescription =  "Synchronization long running task";
		if (synchronizationId == null) {
			return deafultDescription;
		}
		AbstractSysSyncConfigDto config = service.get(synchronizationId);
		if (config == null) {
			return deafultDescription;
		}
		return MessageFormat.format("Run synchronization name: [{0}] - system mapping id: [{1}]", config.getName(),
				config.getSystemMapping());
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> props = super.getProperties();
		props.put(SynchronizationService.PARAMETER_SYNCHRONIZATION_ID, synchronizationId);
		//
		return props;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto synchronizationAttribute = new IdmFormAttributeDto(
				SynchronizationService.PARAMETER_SYNCHRONIZATION_ID,
				SynchronizationService.PARAMETER_SYNCHRONIZATION_ID, 
				PersistentType.UUID,
				AccFaceType.SYNCHRONIZATION_CONFIG_SELECT);
		synchronizationAttribute.setRequired(true);
		//
		return Lists.newArrayList(synchronizationAttribute);
	}

}
