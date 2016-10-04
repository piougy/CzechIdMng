package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.acc.dto.AccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.repository.AccAccountRepository;
import eu.bcvsolutions.idm.acc.service.AccAccountService;
import eu.bcvsolutions.idm.core.model.repository.BaseRepository;
import eu.bcvsolutions.idm.core.model.service.impl.AbstractReadWriteEntityService;

/**
 * Accounts on target system
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultAccAccountService extends AbstractReadWriteEntityService<AccAccount, AccountFilter> implements AccAccountService {

	@Autowired
	private AccAccountRepository accountRepository;
	
	@Override
	protected BaseRepository<AccAccount> getRepository() {
		return accountRepository;
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<AccAccount> find(AccountFilter filter, Pageable pageable) {
		if (filter == null) {
			return find(pageable);
		}
		return accountRepository.findQuick(filter, pageable);
	}
}
