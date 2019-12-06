package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.RoleProcessor;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;

/**
 * Appends environment into role code. Checks filled code, base code and environment.
 * 
 * @author Radek Tomiška
 * @since 9.3.0
 */
@Component(RoleCodeEnvironmentProcessor.PROCESSOR_NAME)
@Description("Appends environment into role code. Checks filled code, base code and environment.")
public class RoleCodeEnvironmentProcessor
		extends CoreEventProcessor<IdmRoleDto> 
		implements RoleProcessor {
	
	public static final String PROCESSOR_NAME = "core-role-code-environment-processor";
	//
	private final IdmRoleService service;
	
	@Autowired
	public RoleCodeEnvironmentProcessor(IdmRoleService service) {
		super(RoleEventType.UPDATE, RoleEventType.CREATE);
		//
		Assert.notNull(service, "Service is required.");		
		//
		this.service = service;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleDto> process(EntityEvent<IdmRoleDto> event) {
		IdmRoleDto role = event.getContent();
		//
		// try to set code by name automatically
		if (StringUtils.isEmpty(role.getCode()) && StringUtils.isEmpty(role.getBaseCode())) {
			if (StringUtils.isNotEmpty(role.getName())) {
				role.setCode(role.getName());
			} else {
				throw new ResultCodeException(CoreResultCode.ROLE_CODE_REQUIRED);
			}
		}
		//
		IdmRoleDto previous = event.getOriginalSource();
		if (previous == null) {
			// base code (and environment) or code can be given
			if (StringUtils.isNotEmpty(role.getCode())
					&& (StringUtils.isNotEmpty(role.getBaseCode()) || StringUtils.isNotEmpty(role.getEnvironment()))
					&& (role.getBaseCode() == null || !role.getCode().equals(service.getCodeWithEnvironment(role)))) {
				throw new ResultCodeException(
						CoreResultCode.ROLE_CODE_ENVIRONMENT_CONFLICT, 
						ImmutableMap.of(
								"code", String.valueOf(role.getCode()),
								"baseCode", String.valueOf(role.getBaseCode()),
								"environment", String.valueOf(role.getEnvironment())
								));
			}
		} else { // update - base code (and environment) or code can be changed
			boolean environmentChanged = !StringUtils.equals(previous.getEnvironment(), role.getEnvironment());
			boolean baseCodeChanged = !StringUtils.equals(previous.getBaseCode(), role.getBaseCode());
			boolean codeChanged = !StringUtils.equals(previous.getCode(), role.getCode()) && StringUtils.isNotEmpty(role.getCode());
			// is possible to change only one item.
			if (codeChanged && (baseCodeChanged || environmentChanged)) {
				throw new ResultCodeException(
						CoreResultCode.ROLE_CODE_ENVIRONMENT_CONFLICT, 
						ImmutableMap.of(
								"code", String.valueOf(role.getCode()),
								"baseCode", String.valueOf(role.getBaseCode()),
								"environment", String.valueOf(role.getEnvironment())
								));
			}
			if (codeChanged) {
				// reset base code - will filled automatically by code
				role.setBaseCode(null);
			}
		}
		if (StringUtils.isEmpty(role.getBaseCode())) {
			role.setBaseCode(role.getCode());
		}
		role.setCode(service.getCodeWithEnvironment(role));
		//
		event.setContent(role);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return -100; // before save
	}

}
