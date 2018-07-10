package eu.bcvsolutions.idm.core.security.service.impl;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmTokenFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmTokenService;
import eu.bcvsolutions.idm.core.model.entity.IdmToken_;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;

/**
 * IdM tokens
 * 
 * @author Radek Tomiška
 * @since 8.2.0
 */
public class DefaultTokenManager implements TokenManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultTokenManager.class);
	//
	@Autowired private IdmTokenService tokenService;	
	@Autowired private SecurityService securityService;
	
	@Override
	public IdmTokenDto getCurrentToken() {
		if (!securityService.isAuthenticated()) {
			// not authenticated
			return null;
		}
		// IdM token has to exist
		UUID tokenId = securityService.getId();
		if (tokenId == null) {
			LOG.debug("Identity [{}] was logged some external way, logout is not supported.", securityService.getCurrentUsername());
			return null;
		}
		IdmTokenDto token = getToken(tokenId);
		if (token == null) {
			LOG.debug("Identity [{}] was logged some external way, logout is not supported.", securityService.getCurrentUsername());
			return null;
		}
		//
		return token;
	}
	
	@Override
	@Transactional
	public IdmTokenDto saveToken(Identifiable owner, IdmTokenDto token, BasePermission... permission) {
		Assert.notNull(owner);
		Assert.notNull(owner.getId());
		//
		token.setOwnerType(getOwnerType(owner));
		token.setOwnerId(getOwnerId(owner));
		//
		return tokenService.save(token, permission);
	}
	
	@Override
	public IdmTokenDto getToken(UUID tokenId, BasePermission... permission) {
		return tokenService.get(tokenId, permission);
	}
	
	@Override
	public IdmTokenDto verifyToken(UUID tokenId, BasePermission... permission) {
		IdmTokenDto token = getToken(tokenId, permission);
		if (token == null) {
			throw new ResultCodeException(CoreResultCode.TOKEN_NOT_FOUND);
		}
		if (token.isDisabled()) {
			throw new ResultCodeException(CoreResultCode.AUTHORITIES_CHANGED);
		}
		if (token.getExpiration() != null && token.getExpiration().isBefore(DateTime.now())) {
			throw new ResultCodeException(CoreResultCode.AUTH_EXPIRED);
		}
		//
		return token;
	}
	
	@Override
	public List<IdmTokenDto> getTokens(Identifiable owner, BasePermission... permission) {
		Assert.notNull(owner);
		Assert.notNull(owner.getId());
		//
		IdmTokenFilter filter = new IdmTokenFilter();
		filter.setOwnerType(getOwnerType(owner.getClass()));
		filter.setOwnerId(getOwnerId(owner));
		//
		return tokenService
				.find(filter, new PageRequest(0, Integer.MAX_VALUE, new Sort(Direction.ASC, IdmToken_.expiration.getName())), permission)
				.getContent();
	}
	
	@Override
	@Transactional
	public void deleteTokens(Identifiable owner, BasePermission... permission) {
		getTokens(owner) // permissions are evaluated below, we want to delete all tokens (e.g. referential integrity)
			.forEach(token -> {
				tokenService.delete(token, permission);
			});
	}
	
	@Override
	@Transactional
	public void disableTokens(Identifiable owner, BasePermission... permission) {
		Assert.notNull(owner);
		Assert.notNull(owner.getId());
		//
		IdmTokenFilter filter = new IdmTokenFilter();
		filter.setOwnerType(getOwnerType(owner.getClass()));
		filter.setOwnerId(getOwnerId(owner));
		filter.setDisabled(Boolean.FALSE);
		//
		tokenService
			.find(filter, null) // permissions are evaluated below, we want to disable all tokens (e.g. referential integrity)
			.forEach(token -> {
				disableToken(token, permission);
			});
	}
	
	@Override
	@Transactional
	public IdmTokenDto disableToken(UUID tokenId, BasePermission... permission) {
		Assert.notNull(tokenId);
		//
		IdmTokenDto token = getToken(tokenId);
		if (token == null) {
			LOG.trace("Persisted token with id [{}] not found, disable token will be skipped.", tokenId);
			return null;
		}
		return disableToken(token, permission);
	}
	
	private IdmTokenDto disableToken(IdmTokenDto token, BasePermission... permission) {
		if (token.isDisabled()) {
			LOG.trace("Persisted token with id [{}] is already disabled.", token.getId());
			return null;
		}
		//
		token.setDisabled(true);
		if (token.getExpiration() == null || token.getExpiration().isAfter(DateTime.now())) {
			token.setExpiration(DateTime.now()); // Remove token by LRT depends on expiration time
		}
		return tokenService.save(token, permission);
	}
	
	@Transactional
	@Scheduled(fixedDelay = 3600000)
	public void purgeTokens() {
		// TODO: CONFIGURATION - ENABLE, TTL
		// older then 2 weeks by default
		purgeTokens(null, DateTime.now().minusWeeks(2));
	}
	
	@Override
	@Transactional
	public void purgeTokens(String tokenType, DateTime olderThan) {
		tokenService.purgeTokens(tokenType, olderThan);
	}
	
	@Override
	public String getOwnerType(Identifiable owner) {
		Assert.notNull(owner);
		//
		return getOwnerType(owner.getClass());
	}
	
	@Override
	public String getOwnerType(Class<? extends Identifiable> ownerType) {
		Assert.notNull(ownerType);
		//
		return tokenService.getOwnerType(ownerType);
	}
	
	public void setTokenService(IdmTokenService tokenService) {
		this.tokenService = tokenService;
	}
	
	/**
	 * UUID identifier from given owner.
	 * 
	 * @param owner
	 * @return
	 */
	private UUID getOwnerId(Identifiable owner) {
		Assert.notNull(owner);
		if (owner.getId() == null) {
			return null;
		}		
		Assert.isInstanceOf(UUID.class, owner.getId(), "Entity with UUID identifier is supported as owner for tokens.");
		//
		return (UUID) owner.getId();
	}
}
