package eu.bcvsolutions.idm.core.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityFormValue;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityFormValueRepository;
import eu.bcvsolutions.idm.eav.repository.AbstractFormValueRepository;
import eu.bcvsolutions.idm.eav.service.impl.AbstractFormValueService;

/**
 * Form values for identity
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultIdmIdentityFormValueService extends AbstractFormValueService<IdmIdentity, IdmIdentityFormValue> {

	private final IdmIdentityFormValueRepository identityFormValueRepository;
	
	@Autowired
	public DefaultIdmIdentityFormValueService(
			ConfidentialStorage confidentialStorage, 
			IdmIdentityFormValueRepository identityFormValueRepository) {
		super(confidentialStorage);
		//
		Assert.notNull(identityFormValueRepository);
		//
		this.identityFormValueRepository = identityFormValueRepository;
	}
	
	@Override
	protected AbstractFormValueRepository<IdmIdentity, IdmIdentityFormValue> getRepository() {
		return identityFormValueRepository;
	}
}
