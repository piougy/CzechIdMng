package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccUniformPasswordSystemFilter;
import eu.bcvsolutions.idm.acc.event.UniformPasswordEvent.UniformPasswordEventType;
import eu.bcvsolutions.idm.acc.service.api.AccUniformPasswordService;
import eu.bcvsolutions.idm.acc.service.api.AccUniformPasswordSystemService;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;

/**
 * Uniform password processor for delete {@link AccUniformPasswordDto} and ensures
 * referential integrity
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Component("accUniformPasswordDeleteProcessor")
@Description("Delete uniform password definition and ensures referential integrity. Cannot be disabled.")
public class UniformPasswordDeleteProcessor extends CoreEventProcessor<AccUniformPasswordDto> {

	private static final String PROCESSOR_NAME = "uniform-password-delete-processor";

	@Autowired
	private AccUniformPasswordService uniformPasswordService;
	@Autowired
	private AccUniformPasswordSystemService uniformPasswordSystemService;

	public UniformPasswordDeleteProcessor() {
		super(UniformPasswordEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<AccUniformPasswordDto> process(EntityEvent<AccUniformPasswordDto> event) {
		AccUniformPasswordDto uniformPasswordFilter = event.getContent();
		Assert.notNull(uniformPasswordFilter, "Uniform password cannot be null!");
		Assert.notNull(uniformPasswordFilter.getId(), "ID for uniform password must exists!");

		// Delete all connections with systems
		AccUniformPasswordSystemFilter uniformPasswordSystemFilter = new AccUniformPasswordSystemFilter();
		uniformPasswordSystemFilter.setUniformPasswordId(uniformPasswordFilter.getId());
		uniformPasswordSystemService.find(uniformPasswordSystemFilter, null).forEach(uniformPasswordSystem -> {
			uniformPasswordSystemService.delete(uniformPasswordSystem);
		});
		
		uniformPasswordService.deleteInternal(uniformPasswordFilter);

		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER;
	}

	@Override
	public boolean isDisableable() {
		// Cannot be disabled
		return false;
	}
}
