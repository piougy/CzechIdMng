package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.PasswordFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.PasswordPolicyFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.entity.IdmPassword;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;
import eu.bcvsolutions.idm.core.model.repository.IdmPasswordPolicyRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmPasswordRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Service for working with password.
 * Now is password connect only to entity IdmIdentity.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
public class DefaultIdmPasswordService
		extends AbstractReadWriteDtoService<IdmPasswordDto, IdmPassword, PasswordFilter>
		implements IdmPasswordService {

	private final IdmPasswordRepository repository;
	private final IdmPasswordPolicyRepository policyRepository;

	@Autowired
	public DefaultIdmPasswordService(IdmPasswordRepository repository,
									 IdmPasswordPolicyRepository policyRepository) {
		super(repository);
		//
		this.repository = repository;
		this.policyRepository = policyRepository;
	}

	@Override
	@Transactional
	public IdmPasswordDto save(IdmIdentityDto identity, PasswordChangeDto passwordChangeDto) {
		Assert.notNull(identity);
		Assert.notNull(passwordChangeDto);
		Assert.notNull(passwordChangeDto.getNewPassword());
		GuardedString password = passwordChangeDto.getNewPassword();
		//
		IdmPasswordDto passwordDto = getPasswordByIdentity(identity.getId());
		//
		if (passwordDto == null) {
			// identity has no password yet
			passwordDto = new IdmPasswordDto();
			passwordDto.setIdentity(identity.getId());
			IdmPasswordPolicy policy = findPasswordCreationPolicy();
			if (policy != null && policy.getMaxPasswordAge() > 0) {
				passwordDto.setValidTill(LocalDate.now().plusDays(policy.getMaxPasswordAge()));
			}
		}
		//
		if (passwordChangeDto.getMaxPasswordAge() != null) {
			passwordDto.setValidTill(passwordChangeDto.getMaxPasswordAge().toLocalDate());
		}
		// set valid from now
		passwordDto.setValidFrom(new LocalDate());
		//
		passwordDto.setPassword(this.generateHash(password, getSalt(identity)));
		//
		// set must change password to false
		passwordDto.setMustChange(false);
		//
		return save(passwordDto);
	}

	@Override
	@Transactional
	public void delete(IdmIdentityDto identity) {
		Assert.notNull(identity);
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
	public boolean checkPassword(GuardedString passwordToCheck, IdmPasswordDto password) {
		return BCrypt.checkpw(passwordToCheck.asString(), password.getPassword());
	}

	@Override
	public String generateHash(GuardedString password, String salt) {
		return BCrypt.hashpw(password.asString(), salt);
	}

	@Override
	public String getSalt(IdmIdentityDto identity) {
		return BCrypt.gensalt(12);
	}

	/**
	 * Method get IdmIdentityPassword by identity.
	 *
	 * @param identityId
	 * @return Object IdmIdentityPassword when password for identity was founded otherwise null.
	 */
	private IdmPasswordDto getPasswordByIdentity(UUID identityId) {
		Assert.notNull(identityId);
		//
		return toDto(this.repository.findOneByIdentity_Id(identityId));
	}

	/**
	 * Finds default password policy for password creation. Only password generation policies are taken into account.<br/>
	 * <p>
	 * Policies are searched in following order:
	 * <ol>
	 * <li>default generation policy</li>
	 * <li>generation policy</li>
	 * <li>no policy</li>
	 * </ol>
	 * <p>
	 * In case multiple search results are found, the lexicographically first on is returned by the method.
	 * are taken into account
	 *
	 * @return Password policy for password creation
	 */
	private IdmPasswordPolicy findPasswordCreationPolicy() {
		// default generation policy
		PasswordPolicyFilter f = new PasswordPolicyFilter();
		f.setDefaultPolicy(true);
		f.setType(IdmPasswordPolicyType.GENERATE);
		final ArrayList<IdmPasswordPolicy> defPolicies = Lists.newArrayList();
		policyRepository.find(f, null).forEach(p -> defPolicies.add(p));
		if (!defPolicies.isEmpty()) {
			return getFirstPolicy(defPolicies);
		}
		// generation policy
		f.setDefaultPolicy(null);
		final ArrayList<IdmPasswordPolicy> policies = Lists.newArrayList();
		policyRepository.find(f, null).forEach(p -> policies.add(p));
		if (!policies.isEmpty()) {
			return getFirstPolicy(policies);
		}
		// nothing
		return null;
	}

	/**
	 * Returns lexicographically first element of the policy collection.
	 *
	 * @param policies
	 * @return
	 */
	private IdmPasswordPolicy getFirstPolicy(List<IdmPasswordPolicy> policies) {
		Collections.sort(policies, Comparator.comparing(IdmPasswordPolicy::getName));
		return policies.get(0);
	}

}
