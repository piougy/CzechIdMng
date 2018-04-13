package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.AccContractSliceAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccContractSliceAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.entity.AccContractSliceAccount;
import eu.bcvsolutions.idm.acc.entity.AccContractSliceAccount_;
import eu.bcvsolutions.idm.acc.entity.SysSystem_;
import eu.bcvsolutions.idm.acc.event.ContractSliceAccountEvent;
import eu.bcvsolutions.idm.acc.event.ContractSliceAccountEvent.ContractSliceAccountEventType;
import eu.bcvsolutions.idm.acc.repository.AccContractSliceAccountRepository;
import eu.bcvsolutions.idm.acc.service.api.AccContractSliceAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.model.entity.IdmContractSlice_;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * ContractSlice-slice-accounts on target system
 * 
 * @author Svanda
 *
 */
@Service
public class DefaultAccContractSliceAccountService extends
		AbstractReadWriteDtoService<AccContractSliceAccountDto, AccContractSliceAccount, AccContractSliceAccountFilter>
		implements AccContractSliceAccountService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(DefaultAccContractSliceAccountService.class);
	@Autowired
	private EntityEventManager entityEventManager;

	@Autowired
	public DefaultAccContractSliceAccountService(AccContractSliceAccountRepository repository) {
		super(repository);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(AccGroupPermission.CONTRACTACCOUNT, getEntityClass());
	}
	
	@Override
	public AccContractSliceAccountDto save(AccContractSliceAccountDto dto, BasePermission... permission) {
		Assert.notNull(dto);
		checkAccess(toEntity(dto, null), permission);
		//
		LOG.debug("Saving contract-slice-account [{}]", dto);
		//
		if (isNew(dto)) { // create
			return entityEventManager.process(new ContractSliceAccountEvent(ContractSliceAccountEventType.CREATE, dto)).getContent();
		}
		return entityEventManager.process(new ContractSliceAccountEvent(ContractSliceAccountEventType.UPDATE, dto)).getContent();
	}

	@Override
	@Transactional
	public void delete(AccContractSliceAccountDto dto, BasePermission... permission) {
		this.delete(dto, true, permission);
	}

	@Override
	@Transactional
	public void delete(AccContractSliceAccountDto entity, boolean deleteTargetAccount, BasePermission... permission) {
		Assert.notNull(entity);
		super.delete(entity, permission);
		Assert.notNull(entity);
		checkAccess(this.getEntity(entity.getId()), permission);
		//
		LOG.debug("Deleting contract-slice-account [{}]", entity);
		entityEventManager.process(new ContractSliceAccountEvent(ContractSliceAccountEventType.DELETE, entity,
				ImmutableMap.of(AccIdentityAccountService.DELETE_TARGET_ACCOUNT_KEY, deleteTargetAccount)));
	}

	@Override
	protected List<Predicate> toPredicates(Root<AccContractSliceAccount> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, AccContractSliceAccountFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		if (filter.getAccountId() != null) {
			predicates.add(builder.equal(root.get(AccContractSliceAccount_.account).get(AccAccount_.id),
					filter.getAccountId()));
		}
		if (filter.getSliceId() != null) {
			predicates.add(builder.equal(root.get(AccContractSliceAccount_.slice).get(IdmContractSlice_.id),
					filter.getSliceId()));
		}
		if (filter.getSystemId() != null) {
			predicates.add(
					builder.equal(root.get(AccContractSliceAccount_.account).get(AccAccount_.system).get(SysSystem_.id),
							filter.getSystemId()));
		}
		if (filter.isOwnership() != null) {
			predicates.add(builder.equal(root.get(AccContractSliceAccount_.ownership), filter.isOwnership()));
		}
		//
		return predicates;
	}
}
