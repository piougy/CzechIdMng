package eu.bcvsolutions.idm.core.bulk.action.impl;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

public abstract class AbstractIdentityBulkAction extends AbstractBulkAction<IdmIdentityDto> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractIdentityBulkAction.class);
	
	@Autowired
	private IdmIdentityService identitySevice;
	
	@Override
	public boolean supports(Class<? extends AbstractEntity> clazz) {
		return clazz.isAssignableFrom(IdmIdentity.class);
	}
	
	@Override
	public String getEntityClass() {
		return IdmIdentity.class.getName();
	}
	
	@Override
	public String getFilterClass() {
		return IdmIdentityFilter.class.getName();
	}
	
	public Boolean process() {
		IdmBulkActionDto action = this.getAction();
		Assert.notNull(action);
		//
		Collection<UUID> identities = null;
		if (action.getIdentifiers() != null) {
			identities = action.getIdentifiers();
		} else {
			identities = find(transformFilter(action.getTransformedFilter()), null);
		}
		this.count = Long.valueOf(identities.size());
		this.counter = 0l;
		//
		return processIdentities(identities);
	}
	
	protected IdmIdentityFilter transformFilter(BaseFilter filter) {
		if (filter instanceof IdmIdentityFilter) {
			return (IdmIdentityFilter) filter;
		}
		throw new ResultCodeException(CoreResultCode.BULK_ACTION_BAD_FILTER, ImmutableMap.of("filter", IdmIdentityFilter.class.getName(), "givenFilter", filter.getClass().getName()));
	}
	
	protected Boolean processIdentities(Collection<UUID> identitiesId) {
		for (UUID identityId : identitiesId) {
			IdmIdentityDto identity = identitySevice.get(identityId);
			if (identity == null) {
				LOG.warn("Identity with id [{}] not found. The identity will be skipped.", identityId);
			}
			//
			processIdentity(identity);
			//
			this.increaseCounter();
			if (!updateState()) {
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}
	
	protected List<UUID> find(IdmIdentityFilter filter, Pageable pageable) {
		return identitySevice.findIds(filter, null).getContent();
	}
	
	protected abstract void processIdentity(IdmIdentityDto dto);
}