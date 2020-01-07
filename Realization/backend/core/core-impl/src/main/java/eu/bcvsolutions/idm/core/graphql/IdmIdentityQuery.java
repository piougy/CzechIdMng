package eu.bcvsolutions.idm.core.graphql;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;

/**
 * GraphQL PoC for identities.
 * - TODO: ZonedDateTime - blockLoginDate, Enums - state
 * - TODO: auditable - schema generalization
 * - TODO: complex filter
 * - TODO: eavs
 * - TODO: mutations
 * - TODO: security
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class IdmIdentityQuery implements GraphQLQueryResolver  {
	
    @Autowired private IdmIdentityService identityService;
    
    // FIXME: move methods under sub root query / identity / find
    public List<IdmIdentityDto> getIdentities(int page, int size) {
        return identityService.find(null, PageRequest.of(page, size)).getContent();
    }

}
