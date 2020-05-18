package eu.bcvsolutions.idm.core.eav.service.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.service.AbstractFormProjectionRoute;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Configurable identity form projection.
 * 
 * @author Radek Tomiška
 * @since 10.3.0
 */
@Component(IdentityFormProjectionRoute.PROJECTION_NAME)
public class IdentityFormProjectionRoute extends AbstractFormProjectionRoute<IdmIdentity> {
	
	public static final String PROJECTION_NAME = "/form/identity-projection";
	public static final String PARAMETER_PRIME_CONTRACT = "prime-contract";
	public static final String PARAMETER_ALL_CONTRACTS = "all-contracts";
	public static final String PARAMETER_OTHER_POSITION = "other-position";
	public static final String PARAMETER_ASSIGNED_ROLES = "assigned-roles";
	
	@Override
	public String getName() {
		return PROJECTION_NAME;
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_PRIME_CONTRACT);
		parameters.add(PARAMETER_ALL_CONTRACTS);
		parameters.add(PARAMETER_OTHER_POSITION);
		parameters.add(PARAMETER_ASSIGNED_ROLES);
		//
		return parameters;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		return Lists.newArrayList(
				new IdmFormAttributeDto(PARAMETER_PRIME_CONTRACT, PARAMETER_PRIME_CONTRACT, PersistentType.BOOLEAN),
				new IdmFormAttributeDto(PARAMETER_ALL_CONTRACTS, PARAMETER_ALL_CONTRACTS, PersistentType.BOOLEAN),
				new IdmFormAttributeDto(PARAMETER_OTHER_POSITION, PARAMETER_OTHER_POSITION, PersistentType.BOOLEAN),
				new IdmFormAttributeDto(PARAMETER_ASSIGNED_ROLES, PARAMETER_ASSIGNED_ROLES, PersistentType.BOOLEAN)
		);
	}
}
