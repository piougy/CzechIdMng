package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmTokenFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmTokenService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmToken;
import eu.bcvsolutions.idm.core.model.entity.IdmToken_;
import eu.bcvsolutions.idm.core.model.repository.IdmTokenRepository;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Persisted tokens
 * 
 * @author Radek Tomiška
 * @since 8.2.0
 */
public class DefaultIdmTokenService
		extends AbstractEventableDtoService<IdmTokenDto, IdmToken, IdmTokenFilter>
		implements IdmTokenService {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmTokenService.class);
	private final IdmTokenRepository repository;
	//
	@Autowired private LookupService lookupService;
	
	@Autowired
	public DefaultIdmTokenService(IdmTokenRepository repository, EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
		//
		this.repository = repository;
	}
	
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.TOKEN, getEntityClass());
	}
	
	@Override
	public String getOwnerType(Class<? extends Identifiable> ownerType) {
		return lookupService.getOwnerType(ownerType);
	}
	
	@Override
	@Transactional
	public void purgeTokens(String tokenType, DateTime olderThan) {
		if (olderThan == null && StringUtils.isEmpty(tokenType)) {
			repository.deleteAll();
			LOG.warn("Purged all tokens.");
			return;
		}
		Long purged;
		if (StringUtils.isEmpty(tokenType)) {
			purged = repository.deleteByExpirationLessThan(olderThan);
		} else if (olderThan == null) {
			purged = repository.deleteByTokenType(tokenType);
		} else { // both params are given
			purged = repository.deleteByTokenTypeAndExpirationLessThan(tokenType, olderThan);
		}
		LOG.info("Purged [{}] tokens, which expired before [{}].", purged, olderThan);
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmToken> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmTokenFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// text - like in token
		String text = filter.getText();
		if (StringUtils.isNotEmpty(text)) {
			predicates.add(builder.like(builder.lower(root.get(IdmToken_.token)), "%" + filter.getText().toLowerCase() + "%"));
		}
		// owner type (identity, registration, wf ...)
		String ownerType = filter.getOwnerType();
		if (StringUtils.isNotEmpty(ownerType)) {
			predicates.add(builder.equal(root.get(IdmToken_.ownerType), ownerType));
		}
		// owner id
		UUID ownerId = filter.getOwnerId();
		if (ownerId != null) {
			predicates.add(builder.equal(root.get(IdmToken_.ownerId), ownerId));
		}
		// disabled
		Boolean disabled = filter.getDisabled();
		if (disabled != null) {
			predicates.add(builder.equal(root.get(IdmToken_.disabled), disabled));
		}
		// expiration
		DateTime expirationTill = filter.getExpirationTill();
		if (expirationTill != null) {
			predicates.add(builder.lessThanOrEqualTo(root.get(IdmToken_.expiration), expirationTill));
		}
		//
		return predicates;
	}
}
