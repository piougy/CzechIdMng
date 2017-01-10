package eu.bcvsolutions.idm.core.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.filter.PasswordPolicyFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordPolicyService;

@Service
public class DefaultIdmPasswordPolicyService extends AbstractReadWriteEntityService<IdmPasswordPolicy, PasswordPolicyFilter> implements IdmPasswordPolicyService {
	
	@Autowired
	public DefaultIdmPasswordPolicyService(
			AbstractEntityRepository<IdmPasswordPolicy, PasswordPolicyFilter> repository) {
		super(repository);
	}

}
