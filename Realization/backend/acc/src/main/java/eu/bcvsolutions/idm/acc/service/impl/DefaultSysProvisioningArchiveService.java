package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto.Builder;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningArchive;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningArchiveRepository;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Archived provisioning operations
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultSysProvisioningArchiveService
		extends AbstractReadWriteDtoService<SysProvisioningArchiveDto, SysProvisioningArchive, SysProvisioningOperationFilter> 
		implements SysProvisioningArchiveService {
	
	private final SysProvisioningArchiveRepository repository;
	private final SysSystemEntityService systemEntityService;

	@Autowired
	public DefaultSysProvisioningArchiveService(
			SysProvisioningArchiveRepository repository,
			SysSystemEntityService systemEntityService) {
		super(repository);
		//
		this.repository = repository;
		this.systemEntityService = systemEntityService;
	}
	
	@Override
	protected Page<SysProvisioningArchive> findEntities(SysProvisioningOperationFilter filter, Pageable pageable, BasePermission... permission) {
		if (filter == null) {
			return repository.findAll(pageable);
		}
		return repository.find(filter, pageable);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW) // we want log in archive always
	public SysProvisioningArchiveDto archive(SysProvisioningOperationDto provisioningOperation) {
		Builder builder = new SysProvisioningArchiveDto.Builder(provisioningOperation);
		if(provisioningOperation.getSystemEntity() != null) {
			SysSystemEntityDto systemEntity = systemEntityService.get(provisioningOperation.getSystemEntity());
			builder.setSystemEntityUid(systemEntity.getUid());
		}
		
		SysProvisioningArchiveDto archive = builder.build();
		// preserve original operation creator
		archive.setCreator(provisioningOperation.getCreator());
		archive.setCreatorId(provisioningOperation.getCreatorId());
		archive.setOriginalCreator(provisioningOperation.getOriginalCreator());
		archive.setOriginalCreatorId(provisioningOperation.getOriginalCreatorId());
		return save(archive);
	}
}
