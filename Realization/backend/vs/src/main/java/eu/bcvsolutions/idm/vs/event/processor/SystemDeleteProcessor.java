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
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
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
 * Before system in acc delete - deletes all request archive in VS module.
 * 
 * @author svandav
 * @author Radek Tomiška
 */
@Component("vsSystemDeleteProcessor")
@Description("Ensures referential integrity. Cannot be disabled.")
public class SystemDeleteProcessor extends AbstractEntityEventProcessor<SysSystemDto> {

	public static final String PROCESSOR_NAME = "system-delete-processor";
	@Autowired
	private VsRequestService requestService;
	@Autowired
	private VsAccountService accountService;
	@Autowired
	private FormService formService;
	@Autowired
	private VsSystemImplementerService systemImplementerService;

	public SystemDeleteProcessor() {
		super(SystemEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<SysSystemDto> event) {
		// We want execute this processor only for virtual system
		SysSystemDto system = event.getContent();
		Assert.notNull(system, "System is required.");
		return system.isVirtual();
	}
	

	@Override
	public EventResult<SysSystemDto> process(EntityEvent<SysSystemDto> event) {

		SysSystemDto system = event.getContent();
		Assert.notNull(system, "System is required.");
		Assert.notNull(system.getId(), "System identifier is required.");
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
		if (system.getConnectorKey() != null) {
			String virtualSystemKey = MessageFormat.format("{0}:systemId={1}", system.getConnectorKey().getFullName(),
					system.getId());
			IdmFormDefinitionDto definition = this.formService.getDefinition(VsAccount.class, virtualSystemKey);
			if (definition != null) {
				formService.deleteDefinition(definition);
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