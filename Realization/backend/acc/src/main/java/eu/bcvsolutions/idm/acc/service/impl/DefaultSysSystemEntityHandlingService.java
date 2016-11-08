package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.dto.SystemEntityHandlingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntityHandling;
import eu.bcvsolutions.idm.acc.repository.SysSchemaObjectClassRepository;
import eu.bcvsolutions.idm.acc.repository.SysSystemEntityHandlingRepository;
import eu.bcvsolutions.idm.acc.service.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.SysSystemEntityHandlingService;
import eu.bcvsolutions.idm.core.api.dto.EmptyFilter;
import eu.bcvsolutions.idm.core.api.repository.BaseRepository;
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
	protected BaseRepository<SysSystemEntityHandling, SystemEntityHandlingFilter> getRepository() {
		return repository;
	}
}
