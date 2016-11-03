package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.repository.SysSchemaObjectClassRepository;
import eu.bcvsolutions.idm.acc.service.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.core.api.dto.EmptyFilter;
import eu.bcvsolutions.idm.core.api.repository.BaseRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

/**
 * Default schema object class service
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysSchemaObjectClassService extends AbstractReadWriteEntityService<SysSchemaObjectClass, EmptyFilter>
		implements SysSchemaObjectClassService {

	@Autowired
	private SysSchemaObjectClassRepository repository;

	@Override
	protected BaseRepository<SysSchemaObjectClass, EmptyFilter> getRepository() {
		return repository;
	}
}
