package eu.bcvsolutions.idm.core.model.repository.handler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.exception.CoreResultCode;
import eu.bcvsolutions.idm.core.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmOrganization;
import eu.bcvsolutions.idm.core.model.repository.IdmOrganizationRepository;

/**
 * Securing organization, adds validations (TODO move to validator - e.g. {@link eu.bcvsolutions.idm.core.model.validator.IdmRoleValidator)
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 */
@Component
@RepositoryEventHandler(IdmOrganization.class)
public class IdmOrganizationEventHandler {
	
	@Autowired
	private IdmOrganizationRepository organizationRepository;
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IdmRoleEventHandler.class);
	
	@HandleBeforeSave
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ORGANIZATION_WRITE + "')")
	public void handleBeforeSave(IdmOrganization organization) {
		if (checkParents(organization)) {
			throw new ResultCodeException(CoreResultCode.BAD_VALUE,  "Organization ["+organization.getName() +"] have bad paren organization.", ImmutableMap.of("organization", "manager"));
		}

		if (checkEmptyParent(organization)) {
			throw new ResultCodeException(CoreResultCode.BAD_VALUE,  "Organization ["+organization.getName() +"] have bad paren organization.", ImmutableMap.of("organization", "manager"));
		}
		
		if (checkChildren(organization)) {
			throw new ResultCodeException(CoreResultCode.BAD_VALUE,  "Organization ["+organization.getName() +"] have bad paren organization.", ImmutableMap.of("organization", "manager"));
		}
		
		
		log.debug("1 Role [{}] will be saved", organization);
	}
	
	@HandleBeforeCreate
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ORGANIZATION_WRITE + "')")
	public void handleBeforeCreate(IdmOrganization organization) {
		if (checkParents(organization)) {
			throw new ResultCodeException(CoreResultCode.BAD_VALUE,  "Organization ["+organization.getName() +"] have bad paren organization.", ImmutableMap.of("organization", "manager"));
		}

		if (checkEmptyParent(organization)) {
			throw new ResultCodeException(CoreResultCode.BAD_VALUE,  "Organization ["+organization.getName() +"] have bad paren organization.", ImmutableMap.of("organization", "manager"));
		}
		
		if (checkChildren(organization)) {
			throw new ResultCodeException(CoreResultCode.BAD_VALUE,  "Organization ["+organization.getName() +"] have bad paren organization.", ImmutableMap.of("organization", "manager"));
		}
		
		
		log.debug("1 Role [{}] will be saved", organization);
	}
	
	@HandleBeforeDelete
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ORGANIZATION_DELETE + "')")
	public void handleBeforeDelete(IdmOrganization organization) {	
		// nothing, just security
	}
	
	private boolean checkEmptyParent(IdmOrganization organization) {
		List<?> root = this.organizationRepository.findChildrenByParent(null);
		
		if (organization.getParent() == null && root.isEmpty() || organization.getParent() != null) {
			return false;
		}
		return true;
	}
	
	/**
	 * Method check if parent of organization isnt her children. Recursive.
	 * @param organization
	 * @return 
	 */
	private boolean checkChildren(IdmOrganization organization) {
		IdmOrganization tmp = organization;
		List<Long> listIds = new ArrayList<Long>(); 
		while (tmp.getParent() != null) {
			if	(listIds.contains(tmp.getId())) {
				return true;
			}
			listIds.add(tmp.getId());
			tmp = tmp.getParent();
		}
		return false;
	}
	
	/**
	 * Method check if organization have same id as parent.id
	 * @param organization
	 * @return true if parent.id and id is same
	 */
	private boolean checkParents(IdmOrganization organization) {
		return organization.getParent() != null && (organization.getId() == organization.getParent().getId());
	}
}
