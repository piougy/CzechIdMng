package eu.bcvsolutions.idm.core.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleGuaranteeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleGuaranteeService;

/**
 * ROle guarantees
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmRoleGuaranteeService 
		extends AbstractReadWriteDtoService<IdmRoleGuaranteeDto, IdmRoleGuarantee, EmptyFilter> 
		implements IdmRoleGuaranteeService {
	
	
	@Autowired
	public DefaultIdmRoleGuaranteeService(IdmRoleGuaranteeRepository repository) {
		super(repository);
	}
}
