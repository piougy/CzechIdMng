package eu.bcvsolutions.idm.core.graphql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLResolver;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;

@Component
public class IdmIdentityRoleResolver implements GraphQLResolver<IdmIdentityRoleDto> {
	
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmRoleService roleService;
    @Autowired private IdmIdentityContractService identityContractService;
 
    
    public IdmIdentityDto getIdentity(IdmIdentityRoleDto identityRole) {
    	IdmIdentityContractDto contract = identityContractService.get(identityRole.getIdentityContract());
    	//
    	return identityService.get(contract.getIdentity());
    }

    public IdmRoleDto getRole(IdmIdentityRoleDto identityRole) {
    	return roleService.get(identityRole.getRole());
    }
}
