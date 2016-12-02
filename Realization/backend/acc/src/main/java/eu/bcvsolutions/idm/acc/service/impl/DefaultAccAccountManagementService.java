package eu.bcvsolutions.idm.acc.service.impl;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.dto.AccountFilter;
import eu.bcvsolutions.idm.acc.dto.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.RoleSystemFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.core.model.dto.IdentityRoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.service.api.IdmAccountManagementService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;

/**
 * Service for control account management
 * @author svandav
 *
 */
@Service
public class DefaultAccAccountManagementService implements IdmAccountManagementService {

	private AccAccountService accountService;
	private SysRoleSystemService roleSystemService;
	private AccIdentityAccountService identityAccountService;
	private IdmIdentityRoleService identityRoleService;

	@Autowired
	public DefaultAccAccountManagementService(SysRoleSystemService roleSystemService, AccAccountService accountService,
			AccIdentityAccountService identityAccountService, IdmIdentityRoleService identityRoleService) {
		super();
		Assert.notNull(identityAccountService);
		Assert.notNull(roleSystemService);
		Assert.notNull(accountService);
		Assert.notNull(identityRoleService);

		this.roleSystemService = roleSystemService;
		this.accountService = accountService;
		this.identityAccountService = identityAccountService;
		this.identityRoleService = identityRoleService;
	}

	@Override
	public boolean resolveIdentityAccounts(IdmIdentity identity) {
		Assert.notNull(identity);

		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		Page<AccIdentityAccount> identityAccounts = identityAccountService.find(filter, null);
		List<AccIdentityAccount> identityAccountList = identityAccounts.getContent();
		// List<IdmIdentityRole> identityRoles = identity.getRoles();

		IdentityRoleFilter identityRoleFilter = new IdentityRoleFilter();
		identityRoleFilter.setIdentityId(identity.getId());
		List<IdmIdentityRole> identityRoles = identityRoleService.find(identityRoleFilter, null).getContent();

		boolean provisioningRequired = false;

		if (CollectionUtils.isEmpty(identityRoles) && CollectionUtils.isEmpty(identityAccountList)) {
			// None roles and accounts ... we don't have any to do
			return false;
		}

		List<AccIdentityAccount> identityAccountsToCreate = new ArrayList<>();
		List<AccIdentityAccount> identityAccountsToDelete = new ArrayList<>();

		// Is role valid in this moment
		identityRoles.stream().filter(identityRole -> {

			LocalDate fromDate = LocalDate.MIN;
			if (identityRole.getValidFrom() != null) {
				if (identityRole.getValidFrom() instanceof Date) {
					fromDate = ((Date) identityRole.getValidFrom()).toLocalDate();
				} else {
					fromDate = identityRole.getValidFrom().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				}
			}

			LocalDate tillDate = LocalDate.MAX;
			if (identityRole.getValidTill() != null) {
				if (identityRole.getValidTill() instanceof Date) {
					tillDate = ((Date) identityRole.getValidTill()).toLocalDate();
				} else {
					tillDate = identityRole.getValidTill().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				}

			}
			LocalDate now = LocalDate.now();
			if (now.isAfter(fromDate) && now.isBefore(tillDate)) {
				return true;
			}
			return false;
		}).forEach(identityRole -> {
			RoleSystemFilter roleSystemFilter = new RoleSystemFilter();
			roleSystemFilter.setRoleId(identityRole.getRole().getId());
			List<SysRoleSystem> roleSystems = roleSystemService.find(roleSystemFilter, null).getContent();

			roleSystems.stream().filter(roleSystem -> {

				return !identityAccountList.stream().filter(identityAccount -> {
					if (roleSystem.getSystem().equals(identityAccount.getAccount().getSystem())) {
						// Account for this identity and system is created
						return true;
					}
					return false;
				}).findFirst().isPresent();

			}).forEach(roleSystem -> {
				// For this system we have to crate new account

				String uid = identity.getUsername();

				// We try find account for same uid on same system
				AccountFilter accountFilter = new AccountFilter();
				accountFilter.setUid(uid);
				accountFilter.setSystemId(roleSystem.getSystem().getId());
				List<AccAccount> sameAccounts = accountService.find(accountFilter, null).getContent();
				AccAccount account = null;
				if (CollectionUtils.isEmpty(sameAccounts)) {
					account = new AccAccount();
					account.setUid(uid);
					account.setAccountType(AccountType.PERSONAL);
					account.setSystem(roleSystem.getSystem());
				} else {
					// We use existed account
					account = sameAccounts.get(0);
				}

				AccIdentityAccount identityAccount = new AccIdentityAccount();
				identityAccount.setAccount(account);
				identityAccount.setIdentity(identity);
				identityAccount.setIdentityRole(identityRole);
				// TODO: Add flag ownership to SystemRole and set here.
				identityAccount.setOwnership(true);

				identityAccountsToCreate.add(identityAccount);
			});
		});

		// Is role unvalid in this moment
		identityRoles.stream().filter(identityRole -> {
			LocalDate fromDate = LocalDate.MIN;
			if (identityRole.getValidFrom() != null) {
				if (identityRole.getValidFrom() instanceof Date) {
					fromDate = ((Date) identityRole.getValidFrom()).toLocalDate();
				} else {
					fromDate = identityRole.getValidFrom().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				}
			}

			LocalDate tillDate = LocalDate.MAX;
			if (identityRole.getValidTill() != null) {
				if (identityRole.getValidTill() instanceof Date) {
					tillDate = ((Date) identityRole.getValidTill()).toLocalDate();
				} else {
					tillDate = identityRole.getValidTill().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				}

			}
			LocalDate now = LocalDate.now();
			if (now.isAfter(fromDate) && now.isBefore(tillDate)) {
				return false;
			}
			return true;
		}).forEach(identityRole -> {
			// Search IdentityAccounts to delete
			identityAccountList.stream().filter(identityAccount -> {
				return identityRole.equals(identityAccount.getIdentityRole());
			}).forEach(identityAccount -> {
				identityAccountsToDelete.add(identityAccount);
			});
		});

		// Delete invalid identity accounts
		provisioningRequired = !identityAccountsToDelete.isEmpty() ? true : provisioningRequired;
		identityAccountsToDelete.stream().forEach(identityAccount -> {
			identityAccountService.delete(identityAccount);
		});

		// Create new identity accounts
		provisioningRequired = !identityAccountsToCreate.isEmpty() ? true : provisioningRequired;
		identityAccountsToCreate.stream().forEach(identityAccount -> {
			identityAccount.setAccount(accountService.save(identityAccount.getAccount()));
			identityAccountService.save(identityAccount);

		});

		return provisioningRequired;
	}

	@Override
	public void deleteIdentityAccount(IdmIdentityRole entity) {
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityRoleId(entity.getId());
		Page<AccIdentityAccount> identityAccounts = identityAccountService.find(filter, null);
		List<AccIdentityAccount> identityAccountList = identityAccounts.getContent();

		identityAccountList.forEach(identityAccount -> {
			identityAccountService.delete(identityAccount);
		});
	}
}
