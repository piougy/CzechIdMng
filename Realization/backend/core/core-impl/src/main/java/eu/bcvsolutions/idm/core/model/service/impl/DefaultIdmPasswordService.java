package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;
import java.time.ZonedDateTime;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordHistoryDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmPasswordFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordHistoryService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmPassword;
import eu.bcvsolutions.idm.core.model.entity.IdmPassword_;
import eu.bcvsolutions.idm.core.model.repository.IdmPasswordPolicyRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmPasswordRepository;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Service for working with password.
 * Now is password connect only to entity IdmIdentity.
 *
 * @author Ondrej Kopr
 * @author Radek Tomiška
 */
public class DefaultIdmPasswordService
		extends AbstractEventableDtoService<IdmPasswordDto, IdmPassword, IdmPasswordFilter>
		implements IdmPasswordService {

	private final IdmPasswordHistoryService passwordHistoryService;
	private final LookupService lookupService;

	@Autowired
	public DefaultIdmPasswordService(IdmPasswordRepository repository,
									 IdmPasswordPolicyRepository policyRepository,
									 IdmPasswordHistoryService passwordHistoryService,
									 LookupService lookupService,
									 EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
		//
		this.passwordHistoryService = passwordHistoryService;
		this.lookupService = lookupService;
	}

	@Override
	protected List<Predicate> toPredicates(Root<IdmPassword> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			IdmPasswordFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		if (filter.getIdentityDisabled() != null) {
			predicates.add(builder.equal(root.get(IdmPassword_.identity).get(IdmIdentity_.disabled), filter.getIdentityDisabled()));
		}
		//
		if (StringUtils.isNotEmpty(filter.getPassword())) {
			predicates.add(builder.equal(root.get(IdmPassword_.password), filter.getPassword()));
		}
		//
		if (filter.getMustChange() != null) {
			predicates.add(builder.equal(root.get(IdmPassword_.mustChange), filter.getMustChange()));
		}
		//
		if (filter.getIdentityId() != null) {
			predicates.add(builder.equal(root.get(IdmPassword_.identity).get(IdmIdentity_.id), filter.getIdentityId()));
		}
		//
		if (StringUtils.isNotEmpty(filter.getIdentityUsername())) {
			predicates.add(builder.equal(root.get(IdmPassword_.identity).get(IdmIdentity_.username), filter.getIdentityUsername()));
		}
		//
		if (filter.getValidFrom() != null) {
			predicates.add(builder.greaterThanOrEqualTo(root.get(IdmPassword_.validFrom), filter.getValidFrom()));
		}
		//
		if (filter.getValidTill() != null) {
			predicates.add(builder.lessThanOrEqualTo(root.get(IdmPassword_.validTill), filter.getValidTill()));
		}
		//
		if (StringUtils.isNotEmpty(filter.getText())) {
			throw new UnsupportedOperationException("Filter by text is not supported");
		}
		//
		return predicates;
	}
	
	@Override
	@Transactional
	public IdmPasswordDto save(IdmIdentityDto identity, PasswordChangeDto passwordChangeDto) {
		Assert.notNull(identity, "Identity is required.");
		Assert.notNull(passwordChangeDto, "Password change dto is required.");
		Assert.notNull(passwordChangeDto.getNewPassword(), "New password is required.");
		GuardedString password = passwordChangeDto.getNewPassword();
		//
		IdmPasswordDto passwordDto = getPasswordByIdentity(identity.getId());
		//
		if (passwordDto == null) {
			// identity has no password yet
			passwordDto = new IdmPasswordDto();
			passwordDto.setIdentity(identity.getId());
		}
		//
		if (passwordChangeDto.getMaxPasswordAge() != null) {
			passwordDto.setValidTill(passwordChangeDto.getMaxPasswordAge().toLocalDate());
		} else {
			passwordDto.setValidTill(null);
		}
		// set valid from now
		passwordDto.setValidFrom(LocalDate.now());
		//
		passwordDto.setPassword(this.generateHash(password, getSalt()));
		//
		// set must change password to false
		passwordDto.setMustChange(false);
		//
		// reset unsuccessful attempts, after password is changed
		passwordDto.resetUnsuccessfulAttempts();
		//
		// Clear block loging date
		passwordDto.setBlockLoginDate(null);
		//
		// create new password history with currently changed password
		createPasswordHistory(passwordDto);
		//
		return save(passwordDto);
	}

	@Override
	@Transactional
	public void delete(IdmIdentityDto identity) {
		Assert.notNull(identity, "Identity is required.");
		//
		IdmPasswordDto passwordDto = getPasswordByIdentity(identity.getId());
		if (passwordDto != null) {
			this.delete(passwordDto);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public IdmPasswordDto findOneByIdentity(UUID identity) {
		return this.getPasswordByIdentity(identity);
	}

	@Override
	@Transactional(readOnly = true)
	public IdmPasswordDto findOneByIdentity(String username) {
		return this.getPasswordByIdentityUsername(username);
	}

	@Override
	public boolean checkPassword(GuardedString passwordToCheck, IdmPasswordDto password) {
		// with null password cannot be identity authenticate
		if (password.getPassword() == null) {
			return false;
		}
		// isn't possible compare null password
		if (passwordToCheck.asString() == null) {
			return false;
		}
		return BCrypt.checkpw(passwordToCheck.asString(), password.getPassword());
	}

	@Override
	public String generateHash(GuardedString password, String salt) {
		return BCrypt.hashpw(password.asString(), salt);
	}
	
	@Override
	public String getSalt() {
		return BCrypt.gensalt(12);
	}

	@Override
	public void increaseUnsuccessfulAttempts(String username) {
		IdmPasswordDto passwordDto = getPasswordByIdentityUsername(username);
		if (passwordDto != null) {
			passwordDto = increaseUnsuccessfulAttempts(passwordDto);
		}
	}

	@Override
	public void setLastSuccessfulLogin(String username) {
		IdmPasswordDto passwordDto = getPasswordByIdentityUsername(username);
		if (passwordDto != null) {
			passwordDto = setLastSuccessfulLogin(passwordDto);
		}
	}
	
	@Override
	public IdmPasswordDto increaseUnsuccessfulAttempts(IdmPasswordDto passwordDto) {
		Assert.notNull(passwordDto, "Password DTO cannot be null!");
		passwordDto.increaseUnsuccessfulAttempts();
		return save(passwordDto);
	}

	@Override
	public IdmPasswordDto setLastSuccessfulLogin(IdmPasswordDto passwordDto) {
		Assert.notNull(passwordDto, "Password DTO cannot be null!");
		passwordDto.setLastSuccessfulLogin(ZonedDateTime.now());
		passwordDto.resetUnsuccessfulAttempts();
		passwordDto.setBlockLoginDate(null);
		return save(passwordDto);
	}
	
	@Override
	@Transactional
	public IdmPasswordDto findOrCreateByIdentity(Serializable identifier) {
		IdmIdentityDto identityDto = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identifier);
		//
		if (identityDto == null) {
			return null;
		}
		//
		UUID identityId = identityDto.getId();
		IdmPasswordDto passwordDto = this.findOneByIdentity(identityId);
		//
		if (passwordDto != null) {
			return passwordDto;
		}
		//
		// TODO: two passwords can be created in multi thread access (lock by identity before the get)
		passwordDto = new IdmPasswordDto();
		passwordDto.setIdentity(identityId);
		passwordDto.setMustChange(false);
		passwordDto.setValidFrom(LocalDate.now());
		//
		return this.save(passwordDto);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.PASSWORD, getEntityClass());
	}

	/**
	 * Method get IdmIdentityPassword by identity.
	 *
	 * @param identityId
	 * @return Object IdmIdentityPassword when password for identity was founded otherwise null.
	 */
	private IdmPasswordDto getPasswordByIdentity(UUID identityId) {
		Assert.notNull(identityId, "Identity identifier is required.");
		//
		IdmPasswordFilter filter = new IdmPasswordFilter();
		filter.setIdentityId(identityId);
		// Isn't possible found more than one password for identity, on table exists unique index
		return this.find(filter, null).getContent().stream().findFirst().orElse(null);
	}

	/**
	 * Method get IdmIdentityPassword by username.
	 *
	 * @param username
	 * @return Object IdmIdentityPassword when password for identity was founded otherwise null.
	 */
	private IdmPasswordDto getPasswordByIdentityUsername(String username) {
		Assert.notNull(username, "Username is required.");
		//
		IdmPasswordFilter filter = new IdmPasswordFilter();
		filter.setIdentityUsername(username);
		// Isn't possible found more than one password for identity, on table exists unique index
		return this.find(filter, null).getContent().stream().findFirst().orElse(null);
	}
	
	/**
	 * Create new password history. This is done after success password change in IdM.
	 *
	 * @param passwordDto
	 */
	private void createPasswordHistory(IdmPasswordDto passwordDto) {
		IdmPasswordHistoryDto passwordHistory = new IdmPasswordHistoryDto();
		passwordHistory.setIdentity(passwordDto.getIdentity());
		passwordHistory.setPassword(passwordDto.getPassword());
		
		passwordHistoryService.save(passwordHistory);
	}
}
