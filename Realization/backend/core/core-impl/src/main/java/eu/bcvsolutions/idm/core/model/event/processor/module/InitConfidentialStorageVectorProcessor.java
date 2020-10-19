package eu.bcvsolutions.idm.core.model.event.processor.module;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmConfidentialStorageValueDto;
import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConfidentialStorageValueFilter;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractInitApplicationProcessor;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.IdmConfidentialStorageValueService;

/**
 * Find and setup dynamic vector for confidential storage
 *
 * @author Ondrej Kopr
 * @since 10.6.0
 *
 */
@Component(InitConfidentialStorageVectorProcessor.PROCESSOR_NAME)
@Description("Init dynamic vector for confidential storage codelists (environment).")
public class InitConfidentialStorageVectorProcessor extends AbstractInitApplicationProcessor {

	public static final String PROCESSOR_NAME = "core-init-confidential-storage-vector-processor";

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(InitConfidentialStorageVectorProcessor.class);

	@Autowired
	private IdmConfidentialStorageValueService confidentialStorageValueService;
	@Autowired
	private ConfidentialStorage confidentialStorage;

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		IdmConfidentialStorageValueFilter filter = new IdmConfidentialStorageValueFilter();
		filter.setEmptyVector(Boolean.TRUE);
		List<UUID> values = confidentialStorageValueService.findIds(filter, null).getContent();
		long count = values.size();

		if (count == 0) {
			return new DefaultEventResult<>(event, this);
		}

		LOG.info("Confidential storage initialization found [{}] values that hasn't defined dynamic vector.", count);
		
		for (UUID valueId : values) {
			IdmConfidentialStorageValueDto value = confidentialStorageValueService.get(valueId);
			LOG.info("Renew vector for confidential storage value id [{}]", valueId);
			confidentialStorage.renewVector(value);
		}

		return new DefaultEventResult<>(event, this);
	}
}
