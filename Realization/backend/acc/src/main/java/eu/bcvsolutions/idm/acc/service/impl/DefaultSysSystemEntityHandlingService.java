package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.dto.SystemEntityHandlingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntityHandling;
import eu.bcvsolutions.idm.acc.repository.SysSystemEntityHandlingRepository;
import eu.bcvsolutions.idm.acc.service.SysSystemEntityHandlingService;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

/**
 * Default system entity handling
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysSystemEntityHandlingService extends
		AbstractReadWriteEntityService<SysSystemEntityHandling, SystemEntityHandlingFilter> implements SysSystemEntityHandlingService {

	@Autowired
	private SysSystemEntityHandlingRepository repository;

	@Override
	protected AbstractEntityRepository<SysSystemEntityHandling, SystemEntityHandlingFilter> getRepository() {
		return repository;
	}
}
