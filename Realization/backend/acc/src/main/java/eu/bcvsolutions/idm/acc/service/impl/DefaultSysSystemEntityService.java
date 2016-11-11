package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.dto.SystemEntityFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.repository.SysSystemEntityRepository;
import eu.bcvsolutions.idm.acc.service.SysSystemEntityService;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

/**
 * Entities on target system
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultSysSystemEntityService extends AbstractReadWriteEntityService<SysSystemEntity, SystemEntityFilter> implements SysSystemEntityService {

	@Autowired
	private SysSystemEntityRepository systemEntityRepository;
	
	@Override
	protected AbstractEntityRepository<SysSystemEntity, SystemEntityFilter> getRepository() {
		return systemEntityRepository;
	}
}
