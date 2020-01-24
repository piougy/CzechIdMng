package eu.bcvsolutions.idm.core.graphql;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLResolver;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;

@Component
public class IdmIdentityResolver implements GraphQLResolver<IdmIdentityDto> {
	
    @Autowired private IdmIdentityContractService identityContractService;
    @Autowired private IdmIdentityRoleService identityRoleService;
 
    public IdmIdentityContractDto getPrimeContract(IdmIdentityDto identity) {
        return identityContractService.getPrimeContract(identity.getId());
    }
    
    public List<IdmIdentityRoleDto> getAssignedRoles(IdmIdentityDto identity) {
        return identityRoleService.findAllByIdentity(identity.getId());
    }
}
