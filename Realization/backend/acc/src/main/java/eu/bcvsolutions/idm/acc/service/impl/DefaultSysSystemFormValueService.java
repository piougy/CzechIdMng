package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemFormValue;
import eu.bcvsolutions.idm.acc.repository.SysSystemFormValueRepository;
import eu.bcvsolutions.idm.eav.repository.AbstractFormValueRepository;
import eu.bcvsolutions.idm.eav.service.AbstractFormValueService;

/**
 * Form values for system entity
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultSysSystemFormValueService extends AbstractFormValueService<SysSystem, SysSystemFormValue> {

	private final SysSystemFormValueRepository systemFormValueRepository;
	
	@Autowired
	public DefaultSysSystemFormValueService(SysSystemFormValueRepository systemFormValueRepository) {
		Assert.notNull(systemFormValueRepository);
		//
		this.systemFormValueRepository = systemFormValueRepository;
	}
	
	@Override
	protected AbstractFormValueRepository<SysSystem, SysSystemFormValue> getRepository() {
		return systemFormValueRepository;
	}
}
