package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.UUID;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.AuditFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.PasswordFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.audit.entity.IdmAudit;
import eu.bcvsolutions.idm.core.model.entity.IdmPassword;
import eu.bcvsolutions.idm.core.model.repository.IdmPasswordPolicyRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmPasswordRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
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

	@Autowired
	public DefaultIdmPasswordService(IdmPasswordRepository repository,
									 IdmPasswordPolicyRepository policyRepository) {
		super(repository);
		//
		this.repository = repository;
	}
	
	@Override
	protected Page<IdmPassword> findEntities(PasswordFilter filter, Pageable pageable, BasePermission... permission) {
		if (filter == null) {
			return getRepository().findAll(pageable);
		}
		return repository.find(filter, pageable);
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
		}
		//
		if (passwordChangeDto.getMaxPasswordAge() != null) {
			passwordDto.setValidTill(passwordChangeDto.getMaxPasswordAge().toLocalDate());
		} else {
			passwordDto.setValidTill(null);
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
}
