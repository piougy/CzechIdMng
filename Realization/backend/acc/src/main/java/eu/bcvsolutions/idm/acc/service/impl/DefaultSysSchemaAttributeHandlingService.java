package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.dto.SchemaAttributeHandlingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttributeHandling;
import eu.bcvsolutions.idm.acc.repository.SysSchemaAttributeHandlingRepository;
import eu.bcvsolutions.idm.acc.service.SysSchemaAttributeHandlingService;
import eu.bcvsolutions.idm.acc.service.SysSchemaAttributeService;
import eu.bcvsolutions.idm.core.api.dto.EmptyFilter;
import eu.bcvsolutions.idm.core.api.repository.BaseRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

/**
 * Default schema attributes handling
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysSchemaAttributeHandlingService extends AbstractReadWriteEntityService<SysSchemaAttributeHandling, SchemaAttributeHandlingFilter>
		implements SysSchemaAttributeHandlingService {

	@Autowired
	private SysSchemaAttributeHandlingRepository repository;

	@Override
	protected BaseRepository<SysSchemaAttributeHandling, SchemaAttributeHandlingFilter> getRepository() {
		return repository;
	}
}
