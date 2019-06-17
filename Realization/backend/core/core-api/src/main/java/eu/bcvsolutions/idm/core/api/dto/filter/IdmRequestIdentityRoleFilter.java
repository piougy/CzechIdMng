package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.MultiValueMap;

/**
 * Filter for changed assigned identity roles
 *
 * @author Vít Švanda
 */
public class IdmRequestIdentityRoleFilter extends IdmConceptRoleRequestFilter {
	
    private UUID identityId;
    private boolean includeEav = false;
    private boolean onlyChanges = false;
    
    public IdmRequestIdentityRoleFilter() {
		super();
	}
    
    public IdmRequestIdentityRoleFilter(MultiValueMap<String, Object> data) {
    	super(data);
    }

	public UUID getIdentityId() {
		return identityId;
	}

	public void setIdentityId(UUID identityId) {
		this.identityId = identityId;
	}

	public boolean isIncludeEav() {
		return includeEav;
	}

	public void setIncludeEav(boolean includeEav) {
		this.includeEav = includeEav;
	}

	public boolean isOnlyChanges() {
		return onlyChanges;
	}

	public void setOnlyChanges(boolean onlyChanges) {
		this.onlyChanges = onlyChanges;
	}
}
