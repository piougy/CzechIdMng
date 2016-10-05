package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.dto.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.repository.AccIdentityAccountRepository;
import eu.bcvsolutions.idm.acc.service.AccIdentityAccountService;
import eu.bcvsolutions.idm.core.api.repository.BaseRepository;
import eu.bcvsolutions.idm.core.model.service.impl.AbstractReadWriteEntityService;

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
	protected BaseRepository<AccIdentityAccount, IdentityAccountFilter> getRepository() {
		return identityAccountRepository;
	}
}
