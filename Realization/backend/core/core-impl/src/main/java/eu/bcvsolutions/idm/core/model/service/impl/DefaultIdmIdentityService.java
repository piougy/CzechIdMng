package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.model.dto.IdentityFilter;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmProvisioningService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.eav.service.api.FormService;
import eu.bcvsolutions.idm.eav.service.impl.AbstractFormableService;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.security.api.service.SecurityService;

@Service
public class DefaultIdmIdentityService extends AbstractFormableService<IdmIdentity, IdentityFilter> implements IdmIdentityService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmIdentityService.class);
	public static final String ADD_ROLE_TO_IDENTITY_WORKFLOW = "changeIdentityRoles";

	@Autowired
	private IdmIdentityRepository identityRepository;

	@Autowired
	private WorkflowProcessInstanceService workflowProcessInstanceService;
	
	@Autowired
	private IdmRoleRepository roleRepository;

	@Autowired
	private SecurityService securityService;
	
	@Autowired
	private IdmIdentityRoleRepository identityRoleRepository;
	
	@Autowired
	private IdmIdentityContractRepository identityContractRepository;
	
	private final ConfidentialStorage confidentialStorage;
	
	// TODO MOCKUP
	@Autowired(required = false)
	private IdmProvisioningService provisioningService;
	
	@Autowired
	public DefaultIdmIdentityService(FormService formService, ConfidentialStorage confidentialStorage) {
		super(formService);
		//
		Assert.notNull(confidentialStorage);
		//
		this.confidentialStorage = confidentialStorage;
	}
	
	@Override
	protected AbstractEntityRepository<IdmIdentity, IdentityFilter> getRepository() {
		return identityRepository;
	}

	/**
	 * Start workflow for change permissions
	 */
	@Override
	public ProcessInstance changePermissions(IdmIdentity identity) {
		return workflowProcessInstanceService.startProcess(ADD_ROLE_TO_IDENTITY_WORKFLOW,
				IdmIdentity.class.getSimpleName(), identity.getUsername(), identity.getId().toString(), null);
	}

	@Override
	@Transactional(readOnly = true)
	public IdmIdentity getByUsername(String username) {
		return identityRepository.findOneByUsername(username);
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmIdentity getByName(String username) {
		return this.getByUsername(username);
	}
	
	@Override
	public String getNiceLabel(IdmIdentity identity) {
		if (identity == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		if (identity.getTitleBefore() != null) {
			sb.append(identity.getTitleBefore()).append(" ");
		}
		if (identity.getFirstName() != null) {
			sb.append(identity.getFirstName()).append(" ");
		}
		if (identity.getLastName() != null) {
			sb.append(identity.getLastName()).append(" ");
		}
		if (identity.getTitleAfter() != null) {
			sb.append(identity.getTitleAfter()).append(" ");
		}
		return sb.toString().trim();
	}

	/**
	 * Find all identities usernames by assigned role
	 * 
	 * @param roleId
	 * @return String with all found usernames separate with comma
	 */
	@Override
	@Transactional(readOnly = true)
	public String findAllByRoleAsString(UUID roleId) {
		IdmRole role = roleRepository.findOne(roleId);
		Assert.notNull(role, "Role is required. Role by id [" + roleId + "] not foud.");
		
		List<IdmIdentity> identities = this.findAllByRole(role);				
		StringBuilder sb = new StringBuilder();
		for (IdmIdentity i : identities) {
			sb.append(i.getUsername());
			sb.append(",");
		}
		return sb.toString();
	}
	
	/**
	 * Find all identities by assigned role
	 * 
	 * @param role
	 * @return List of IdmIdentity with assigned role
	 */
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentity> findAllByRole(IdmRole role) {
		Assert.notNull(role, "RoleIs required");
		//
		return identityRepository.findAllByRole(role);
	}

	/**
	 * Method find all managers by identity contract and return manager's usernames,
	 * separate by commas
	 * 
	 * @param identityId
	 * @return String - usernames separate by commas
	 */
	@Override
	@Transactional(readOnly = true)
	public String findAllManagersAsString(UUID identityId) {
		IdmIdentity identity = this.get(identityId);
		Assert.notNull(identity, "Identity is required. Identity by id [" + identityId + "] not found.");
		
		List<String> list = this.findAllManagers(identity, null)
				.stream()
				.map(IdmIdentity::getUsername)
				.collect(Collectors.toList());
		return StringUtils.join(list, ',');
	}

	/**
	 * Method finds all identity's managers by identity contract (guarantee or by assigned tree structure).
	 * 
	 * @param forIdentity
	 * @param byTreeType If optional tree type is given, then only managers defined with this type is returned
	 * @return
	 */
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentity> findAllManagers(IdmIdentity forIdentity, IdmTreeType byTreeType) {
		Assert.notNull(forIdentity, "Identity is required");
		//		
		IdentityFilter filter = new IdentityFilter();
		filter.setManagersFor(forIdentity);
		filter.setManagersByTreeType(byTreeType);
		//
		List<IdmIdentity> results = new ArrayList<IdmIdentity>();		
		Page<IdmIdentity> managers = identityRepository.find(filter, new PageRequest(0, 50, Sort.Direction.ASC, "username"));
		results.addAll(managers.getContent());
		while (managers.hasNext()) {
			managers = identityRepository.find(filter, managers.nextPageable());
			results.addAll(managers.getContent());
		}
		//
		if (!results.isEmpty()) {
			return results;
		}
		// return all identities with admin role
		return this.findAllByRole(this.getAdminRole());
	}

	/**
	 * Changes given identity's password
	 * 
	 * @param identity
	 * @param passwordChangeDto
	 */
	@Override
	@Transactional
	public void passwordChange(IdmIdentity identity, PasswordChangeDto passwordChangeDto) {
		if (!securityService.isAdmin()) {
			if(passwordChangeDto.getOldPassword() == null) {
				throw new ResultCodeException(CoreResultCode.PASSWORD_CHANGE_CURRENT_FAILED_IDM);
			}
			// previous password check
			GuardedString idmPassword = confidentialStorage.getGuardedString(identity, PASSWORD_CONFIDENTIAL_PROPERTY);
			if(!StringUtils.equals(new String(idmPassword.asString()),passwordChangeDto.getOldPassword().asString())) {
				throw new ResultCodeException(CoreResultCode.PASSWORD_CHANGE_CURRENT_FAILED_IDM);
			}
		}
		if (passwordChangeDto.isIdm()) { // change identity's password
			savePassword(identity, passwordChangeDto.getNewPassword());
		}
		// MOCKUP TODO
		if(provisioningService != null){
			provisioningService.changePassword(identity, passwordChangeDto);
		}		
	}
	
	/**
	 * TODO: move to configuration service
	 * 
	 * @return
	 */
	private IdmRole getAdminRole() {
		return this.roleRepository.findOneByName(IdmRoleRepository.ADMIN_ROLE);
	}
	
	@Override
	@Transactional
	public IdmIdentity save(IdmIdentity entity) {
		GuardedString password = entity.getPassword();
		
		entity = super.save(entity);
		// save password to confidential storage
		if (password != null) {
			savePassword(entity, password);
		}
		// MOCKUP TODO
		if(provisioningService != null) {
			provisioningService.doIdentityProvisioning(entity);
		}
		return entity;
	}
	
	@Override
	@Transactional
	public void delete(IdmIdentity identity) {
		// clear referenced roles
		identityRoleRepository.deleteByIdentity(identity);
		// contracts
		identityContractRepository.deleteByIdentity(identity);
		//
		super.delete(identity);
	}

	/**
	 * Loads password from confidential storage
	 */
	@Override
	public GuardedString getPassword(IdmIdentity identity) {
		Assert.notNull(identity);
		//
		return confidentialStorage.getGuardedString(identity, PASSWORD_CONFIDENTIAL_PROPERTY);
	}
	
	/**
	 * Saves identity's password to confidential storage
	 * 
	 * @param identity
	 * @param newPassword
	 */
	private void savePassword(IdmIdentity identity, GuardedString newPassword) {
		LOG.debug("Saving password for identity [{}] to configental storage under key [{}]", identity.getUsername(), PASSWORD_CONFIDENTIAL_PROPERTY);
		confidentialStorage.save(identity, PASSWORD_CONFIDENTIAL_PROPERTY, newPassword.asString());
	}
}
