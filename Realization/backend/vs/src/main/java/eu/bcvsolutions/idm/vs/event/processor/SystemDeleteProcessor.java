package eu.bcvsolutions.idm.vs.event.processor;

import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.event.SystemEvent.SystemEventType;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.vs.domain.VsRequestState;
import eu.bcvsolutions.idm.vs.dto.filter.VsAccountFilter;
import eu.bcvsolutions.idm.vs.dto.filter.VsRequestFilter;
import eu.bcvsolutions.idm.vs.dto.filter.VsSystemImplementerFilter;
import eu.bcvsolutions.idm.vs.entity.VsAccount;
import eu.bcvsolutions.idm.vs.exception.VsResultCode;
import eu.bcvsolutions.idm.vs.service.api.VsAccountService;
import eu.bcvsolutions.idm.vs.service.api.VsRequestService;
import eu.bcvsolutions.idm.vs.service.api.VsSystemImplementerService;

/**
 * Before system in acc delete - deletes all request archive in VS module
 * 
 * @author svandav
 *
 */
@Component("vsSystemDeleteProcessor")
@Description("Ensures referential integrity. Cannot be disabled.")
public class SystemDeleteProcessor extends AbstractEntityEventProcessor<SysSystemDto> {

	public static final String PROCESSOR_NAME = "system-delete-processor";
	private final VsRequestService requestService;
	private final VsAccountService accountService;
	private final IdmFormDefinitionService formDefinitionService;
	private final VsSystemImplementerService systemImplementerService;

	@Autowired
	public SystemDeleteProcessor(VsRequestService requestService, VsAccountService accountService,
			IdmFormDefinitionService formDefinitionService, VsSystemImplementerService systemImplementerService) {
		super(SystemEventType.DELETE);
		//
		Assert.notNull(requestService);
		Assert.notNull(accountService);
		Assert.notNull(formDefinitionService);
		Assert.notNull(systemImplementerService);
		//
		this.requestService = requestService;
		this.accountService = accountService;
		this.formDefinitionService = formDefinitionService;
		this.systemImplementerService = systemImplementerService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<SysSystemDto> process(EntityEvent<SysSystemDto> event) {

		SysSystemDto system = event.getContent();
		Assert.notNull(system);
		//
		// If exists unresolved vs request, then is not possible to delete
		// system
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(system.getId());
		requestFilter.setState(VsRequestState.IN_PROGRESS);
		if (requestService.find(requestFilter, null).getTotalElements() > 0) {
			throw new ResultCodeException(VsResultCode.VS_SYSTEM_DELETE_FAILED_HAS_REQUEST,
					ImmutableMap.of("system", system.getName()));
		}

		// Delete archived vs requests
		requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(system.getId());
		requestFilter.setOnlyArchived(Boolean.TRUE);
		requestService.find(requestFilter, null).forEach(entity -> {
			requestService.delete(entity);
		});

		// Delete vs account
		VsAccountFilter accountFilter = new VsAccountFilter();
		accountFilter.setSystemId(system.getId());
		accountService.find(accountFilter, null).forEach(entity -> {
			accountService.delete(entity);
		});

		// Delete vs account form definition
		if(system.getConnectorKey() != null) {
			String virtualSystemKey = MessageFormat.format("{0}:systemId={1}", system.getConnectorKey().getFullName(),
					system.getId());
			IdmFormDefinitionDto definition = this.formDefinitionService.findOneByTypeAndCode(VsAccount.class.getName(),
					virtualSystemKey);
			if (definition != null) {
				formDefinitionService.delete(definition);
			}
		}

		// Delete vs implementers
		VsSystemImplementerFilter implementerFilter = new VsSystemImplementerFilter();
		implementerFilter.setSystemId(system.getId());
		systemImplementerService.find(implementerFilter, null).forEach(entity -> {
			systemImplementerService.delete(entity);
		});

		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		// right now before system delete
		return CoreEvent.DEFAULT_ORDER - 1;
	}

	@Override
	public boolean isDisableable() {
		return false;
	}
}