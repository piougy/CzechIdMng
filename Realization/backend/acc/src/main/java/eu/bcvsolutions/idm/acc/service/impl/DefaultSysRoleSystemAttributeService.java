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
import eu.bcvsolutions.idm.acc.repository.SysRoleSystemAttributeRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
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

	@Autowired
	private SysRoleSystemAttributeRepository repository;

	@Autowired(required = false)
	private AccIdentityAccountService identityAccountService;
	@Autowired
	private ApplicationContext applicationContext;

	private AccAccountManagementService accountManagementService;

	@Override
	protected AbstractEntityRepository<SysRoleSystemAttribute, RoleSystemAttributeFilter> getRepository() {
		return repository;
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
			if (accountManagementService == null) {
				accountManagementService = applicationContext.getBean(AccAccountManagementService.class);
			}
			accountManagementService.resolveIdentityAccounts(identity);
		});

		return roleSystemAttribute;
	}
}
