package eu.bcvsolutions.idm.core.model.service.impl;


import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityPasswordFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityPassword;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityPasswordRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityPasswordService;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;

/**
 * 
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * 
 * TODO: password valid till and valid from!!
 */

@Service
public class DefaultIdmIdentityPasswordService extends AbstractReadWriteEntityService<IdmIdentityPassword, IdentityPasswordFilter> implements IdmIdentityPasswordService {
	
	private IdmIdentityPasswordRepository identityPasswordRepository;
	
	private final String ALGORITHM = "PBKDF2WithHmacSHA512";
	
	private final int ITERATION_COUNT = 512;
	
	private final int DERIVED_KEY_LENGTH = 256;
	
	@Autowired
	public DefaultIdmIdentityPasswordService(
			IdmIdentityPasswordRepository identityPasswordRepository) {
		super(identityPasswordRepository);
		//
		Assert.notNull(identityPasswordRepository);
		//
		this.identityPasswordRepository = identityPasswordRepository;
	}

	@Override
	public IdmIdentityPassword save(IdmIdentity identity, PasswordChangeDto passwordDto) {
		Assert.notNull(identity);
		Assert.notNull(passwordDto);
		Assert.notNull(passwordDto.getNewPassword());
		GuardedString password = passwordDto.getNewPassword();
		//
		IdmIdentityPassword passwordEntity = getPasswordByIdentity(identity);
		//
		if (passwordEntity == null) {
			// identity has no password yet
			passwordEntity = new IdmIdentityPassword();
			passwordEntity.setIdentity(identity);
		}
		//
		if (passwordDto.getMaxPasswordAge() != null) {
			passwordEntity.setValidTill(passwordDto.getMaxPasswordAge().toLocalDate());
		}
		//
		passwordEntity.setPassword(this.generateHash(password, getSalt(identity)));
		//
		return identityPasswordRepository.save(passwordEntity);
	}

	@Override
	public void delete(IdmIdentity identity) {
		IdmIdentityPassword passwordEntity = getPasswordByIdentity(identity);
		if (passwordEntity != null) {
			this.identityPasswordRepository.delete(passwordEntity);
		}
	}
	
	@Override
	public IdmIdentityPassword get(IdmIdentity identity) {
		return this.getPasswordByIdentity(identity);
	}
	
	@Override
	public boolean checkPassword(GuardedString passwordToCheck, IdmIdentityPassword password) {
		byte[] newPassword = generateHash(passwordToCheck, this.getSalt(password.getIdentity()));
		return Arrays.equals(newPassword, password.getPassword());
	}

	@Override
	public byte[] generateHash(GuardedString password, byte[] salt) {
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
			PBEKeySpec keySpec = new PBEKeySpec(password.asString().toCharArray(), salt, ITERATION_COUNT, DERIVED_KEY_LENGTH);
			SecretKey key = factory.generateSecret(keySpec);
			return key.getEncoded();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_CHANGE_FAILED, ImmutableMap.of("error", e.getMessage()));
		}
	}
	
	@Override
	public byte[] getSalt(IdmIdentity identity) {
		UUID id = identity.getId();
		return ByteBuffer.allocate(16).putLong(id.getMostSignificantBits()).putLong(id.getLeastSignificantBits()).array();
	}
	
	/**
	 * Method get IdmIdentityPassword by identity.
	 * 
	 * @param identity
	 * @return Object IdmIdentityPassword when password for identity was founded otherwise null.
	 */
	private IdmIdentityPassword getPasswordByIdentity(IdmIdentity identity) {
		Assert.notNull(identity);
		//
		return this.identityPasswordRepository.findOneByIdentity(identity);
	}
}
