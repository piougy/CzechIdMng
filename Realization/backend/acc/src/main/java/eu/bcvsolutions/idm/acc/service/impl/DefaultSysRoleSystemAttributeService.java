package eu.bcvsolutions.idm.acc.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.dto.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.RoleSystemAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.event.processor.IdentityRoleSaveProcessor;
import eu.bcvsolutions.idm.acc.repository.SysRoleSystemAttributeRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Mapping attribute to system for role
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysRoleSystemAttributeService
		extends AbstractReadWriteEntityService<SysRoleSystemAttribute, RoleSystemAttributeFilter>
		implements SysRoleSystemAttributeService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultSysRoleSystemAttributeService.class);
	@Autowired(required = false)
	private AccIdentityAccountService identityAccountService;
	@Autowired
	private ApplicationContext applicationContext;
	private AccAccountManagementService accountManagementService;
	private SysProvisioningService provisioningService;

	@Autowired
	public DefaultSysRoleSystemAttributeService(SysRoleSystemAttributeRepository repository) {
		super(repository);
	}

	@Override
	public SysRoleSystemAttribute save(SysRoleSystemAttribute entity) {

		SysRoleSystemAttribute roleSystemAttribute = super.save(entity);

		// RoleSystemAttribute was changed. We need do ACC management for all
		// connected identities
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setRoleSystemId(entity.getRoleSystem().getId());
		List<AccIdentityAccount> identityAccounts = identityAccountService.find(filter, null).getContent();
		// TODO: move to filter and use distinct
		List<IdmIdentity> identities = new ArrayList<>();
		identityAccounts.stream().forEach(identityAccount -> {
			if (!identities.contains(identityAccount.getIdentity())) {
				identities.add(identityAccount.getIdentity());
			}
		});
		identities.stream().forEach(identity -> {		
			LOG.debug("Call account management for idnetity [{}]", identity.getUsername());
			boolean provisioningRequired = getAccountManagementService().resolveIdentityAccounts(identity);
			if(provisioningRequired){
				LOG.debug("Call provisioning for idnetity [{}]", identity.getUsername());
				getProvisioningService().doProvisioning(identity);
			}
		});

		return roleSystemAttribute;
	}
	
	private AccAccountManagementService getAccountManagementService() {
		if (accountManagementService == null) {
			accountManagementService = applicationContext.getBean(AccAccountManagementService.class);
		}
		return accountManagementService;
	}
	
	/**
	 * provisioningService has dependency everywhere - so we need lazy init ...
	 * 
	 * @return
	 */
	private SysProvisioningService getProvisioningService() {
		if (provisioningService == null) {
			provisioningService = applicationContext.getBean(SysProvisioningService.class);
		}
		return provisioningService;
	}
}
