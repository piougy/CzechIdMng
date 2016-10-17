package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.QuickFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.repository.BaseRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.IdentityFilter;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.model.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.security.api.service.SecurityService;

@Service
public class DefaultIdmIdentityService extends AbstractReadWriteEntityService<IdmIdentity, IdentityFilter> implements IdmIdentityService {

	public static final String ADD_ROLE_TO_IDENTITY_WORKFLOW = "changeIdentityRoles";

	@Autowired
	private IdmIdentityRepository identityRepository;

	@Autowired
	private WorkflowProcessInstanceService workflowProcessInstanceService;
	
	@Autowired
	private IdmIdentityContractRepository identityContractRepository;
	
	@Autowired
	private IdmRoleRepository roleRepository;

	@Autowired
	private SecurityService securityService;
	
	@Override
	protected BaseRepository<IdmIdentity, IdentityFilter> getRepository() {
		return identityRepository;
	}

	/**
	 * Start workflow for change permissions
	 */
	@Override
	public ProcessInstance changePermissions(IdmIdentity identity) {
		return workflowProcessInstanceService.startProcess(ADD_ROLE_TO_IDENTITY_WORKFLOW,
				IdmIdentity.class.getSimpleName(), identity.getUsername(), identity.getId(), null);
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
	public String findAllByRoleAsString(Long roleId) {
		List<IdmIdentity> identities = this.findAllByRole(roleId);
				
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
	 * @param roleId
	 * @return List of IdmIdentity with assigned role
	 */
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentity> findAllByRole(Long roleId) {
		List<IdmIdentity> identities = identityRepository.findAllByRole(roleId);
		if (identities == null) {
			return null;
		}
		return identities;
	}

	/**
	 * Method find all managers by user positions and return managers username,
	 * separate by commas
	 * 
	 * @param id
	 * @return String - usernames separate by commas
	 */
	@Override
	@Transactional(readOnly = true)
	public String findAllManagersByUserPositionsString(Long id) {
		List<String> list = this.findAllManagersByUserPositions(id).stream().map(IdmIdentity::getUsername)
				.collect(Collectors.toList());
		return StringUtils.join(list, ',');
	}

	/**
	 * Method find all managers by user positions and return managers identity
	 * @param id
	 * @return List of IdmIdentities 
	 */
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentity> findAllManagersByUserPositions(Long id) {
		List<IdmIdentity> result = new ArrayList<>();
		
		IdmIdentity user = this.get(id);
		List<IdmIdentityContract> positions = identityContractRepository.findAllByIdentity(user, null);
		// TODO: find from parent working positions
		for	(IdmIdentityContract position : positions) {
			if(position.getGuarantee() != null) {
				result.add(position.getGuarantee());
			}
		}
		
		if (result.isEmpty()) {
			return this.findAllByRole(this.getAdminRoleId());
		}
		
		return result;
	}

	/**
	 * Changes given identity's password
	 * 
	 * TODO: propagate password change to other systems
	 * 
	 * @param identity
	 * @param passwordChangeDto
	 */
	@Override
	@Transactional
	public void passwordChange(IdmIdentity identity, PasswordChangeDto passwordChangeDto) {
		if (!securityService.isAdmin() && !StringUtils
				.equals(new String(identity.getPassword()), new String(passwordChangeDto.getOldPassword()))) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_CHANGE_CURRENT_FAILED_IDM);
		}
		identity.setPassword(passwordChangeDto.getNewPassword());
		identityRepository.save(identity);
	}
	
	private Long getAdminRoleId() {
		return this.roleRepository.findOneByName(IdmRoleRepository.ADMIN_ROLE).getId();
	}
}
