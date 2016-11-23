package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.dto.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.repository.AccIdentityAccountRepository;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

/**
 * Identity accounts on target system
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultAccIdentityAccountService extends AbstractReadWriteEntityService<AccIdentityAccount, IdentityAccountFilter> implements AccIdentityAccountService {

	@Autowired
	private AccIdentityAccountRepository identityAccountRepository;
	
	@Override
	protected AbstractEntityRepository<AccIdentityAccount, IdentityAccountFilter> getRepository() {
		return identityAccountRepository;
	}
}
