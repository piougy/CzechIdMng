package eu.bcvsolutions.idm.core.model.service.impl;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.dto.filter.PasswordFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmPassword;
import eu.bcvsolutions.idm.core.model.repository.IdmPasswordRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * 
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * 
 */

@Service
public class DefaultIdmPasswordService extends AbstractReadWriteEntityService<IdmPassword, PasswordFilter> implements IdmPasswordService {
	
	private IdmPasswordRepository identityPasswordRepository;
	
	@Autowired
	public DefaultIdmPasswordService(
			IdmPasswordRepository identityPasswordRepository) {
		super(identityPasswordRepository);
		//
		Assert.notNull(identityPasswordRepository);
		//
		this.identityPasswordRepository = identityPasswordRepository;
	}

	@Override
	public IdmPassword save(IdmIdentity identity, PasswordChangeDto passwordDto) {
		Assert.notNull(identity);
		Assert.notNull(passwordDto);
		Assert.notNull(passwordDto.getNewPassword());
		GuardedString password = passwordDto.getNewPassword();
		//
		IdmPassword passwordEntity = getPasswordByIdentity(identity);
		//
		if (passwordEntity == null) {
			// identity has no password yet
			passwordEntity = new IdmPassword();
			passwordEntity.setIdentity(identity);
		}
		//
		if (passwordDto.getMaxPasswordAge() != null) {
			passwordEntity.setValidTill(passwordDto.getMaxPasswordAge().toLocalDate());
		}
		// set valid from now
		passwordEntity.setValidFrom(new LocalDate());
		//
		passwordEntity.setPassword(this.generateHash(password, getSalt(identity)));
		//
		// set must change password to false
		passwordEntity.setMustChange(false);
		//
		return identityPasswordRepository.save(passwordEntity);
	}

	@Override
	public void delete(IdmIdentity identity) {
		IdmPassword passwordEntity = getPasswordByIdentity(identity);
		if (passwordEntity != null) {
			this.identityPasswordRepository.delete(passwordEntity);
		}
	}
	
	@Override
	public IdmPassword get(IdmIdentity identity) {
		return this.getPasswordByIdentity(identity);
	}
	
	@Override
	public boolean checkPassword(GuardedString passwordToCheck, IdmPassword password) {
		return BCrypt.checkpw(passwordToCheck.asString(), password.getPassword());
	}

	@Override
	public String generateHash(GuardedString password, String salt) {
		return BCrypt.hashpw(password.asString(), salt);
	}
	
	@Override
	public String getSalt(IdmIdentity identity) {
		return BCrypt.gensalt(12);
	}
	
	/**
	 * Method get IdmIdentityPassword by identity.
	 * 
	 * @param identity
	 * @return Object IdmIdentityPassword when password for identity was founded otherwise null.
	 */
	private IdmPassword getPasswordByIdentity(IdmIdentity identity) {
		Assert.notNull(identity);
		//
		return this.identityPasswordRepository.findOneByIdentity(identity);
	}
}
