package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmPasswordHistoryDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmPasswordHistoryFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordHistoryService;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordHistory;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordHistory_;
import eu.bcvsolutions.idm.core.model.repository.IdmPasswordHistoryRepository;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Default implementation of service that validate same password trough history.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class DefaultIdmPasswordHistoryService
		extends AbstractReadWriteDtoService<IdmPasswordHistoryDto, IdmPasswordHistory, IdmPasswordHistoryFilter>
		implements IdmPasswordHistoryService {

	@Autowired
	public DefaultIdmPasswordHistoryService(IdmPasswordHistoryRepository repository) {
		super(repository);
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmPasswordHistory> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, IdmPasswordHistoryFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// Identity id
		if (filter.getIdentityId() != null) {
			predicates.add(builder.equal(root.get(IdmPasswordHistory_.identity).get(AbstractEntity_.id), filter.getIdentityId()));
		}
		//
		return predicates;
	}

	@Override
	public boolean checkHistory(UUID identityId, int countOfIteration, GuardedString newPassword) {
		Assert.notNull(identityId, "Identity id can't be null.");
		Assert.notNull(newPassword, "New password can't be null.");
		//
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
		//
		for (IdmPasswordHistoryDto passwordHistory : getPasswordHistoryForIdentity(identityId, countOfIteration)) {
			boolean matches = encoder.matches(newPassword.asString(), passwordHistory.getPassword());
			if (matches) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void deleteAllByIdentity(UUID identityId) {
		Assert.notNull(identityId, "Identity id can't be null.");
		//
		IdmPasswordHistoryFilter filter = new IdmPasswordHistoryFilter();
		filter.setIdentityId(identityId);
		//
		for (IdmPasswordHistoryDto passwordHistory : this.find(filter, null)) {
			this.delete(passwordHistory);
		}
	}

	/**
	 * Return list of {@link IdmPasswordHistoryDto} for given identity id.
	 *
	 * @param identityId
	 * @param count
	 * @return
	 */
	private List<IdmPasswordHistoryDto> getPasswordHistoryForIdentity(UUID identityId, int count) {
		IdmPasswordHistoryFilter filter = new IdmPasswordHistoryFilter();
		filter.setIdentityId(identityId);
		return this.find(filter, new PageRequest(0, count, new Sort(Direction.DESC, IdmPasswordHistory_.created.getName()))).getContent();
	}

}
