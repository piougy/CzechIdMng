package eu.bcvsolutions.idm.vs.service.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.vs.entity.VsAccount;
import eu.bcvsolutions.idm.vs.entity.VsAccount_;
import eu.bcvsolutions.idm.vs.repository.VsAccountRepository;
import eu.bcvsolutions.idm.vs.repository.filter.AccountFilter;
import eu.bcvsolutions.idm.vs.service.api.VsAccountService;
import eu.bcvsolutions.idm.vs.service.api.dto.VsAccountDto;

/**
 * Service for account in virtual system
 * 
 * @author Svanda
 *
 */
@Service()
public class DefaultVsAccountService
		extends AbstractReadWriteDtoService<VsAccountDto, VsAccount, AccountFilter> 
		implements VsAccountService {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultVsAccountService.class);

	private final FormService formService;
	
	@Autowired
	public DefaultVsAccountService(
			VsAccountRepository repository,
			EntityEventManager entityEventManager,
			FormService formService) {
		super(repository);
		//
		Assert.notNull(formService);
		Assert.notNull(entityEventManager);
		//
		this.formService = formService;
	}
	
	
	@Override
	public void deleteInternal(VsAccountDto dto) {
		// TODO: eav dto
		formService.deleteValues(getRepository().findOne(dto.getId()));
		//
		super.deleteInternal(dto);
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<VsAccount> root, CriteriaQuery<?> query, CriteriaBuilder builder, AccountFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// quick - "fulltext"
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.or(
					builder.equal(builder.lower(root.get(VsAccount_.uid)), "%" + filter.getText().toLowerCase() + "%")			
					));
		}
		return predicates;
	}

}
