package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.acc.domain.ProvisioningOperation;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.acc.dto.filter.ProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningArchive;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningArchiveRepository;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;

/**
 * Archived provisioning operations
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultSysProvisioningArchiveService
		extends AbstractReadWriteDtoService<SysProvisioningArchiveDto, SysProvisioningArchive, ProvisioningOperationFilter> implements SysProvisioningArchiveService {

	@Autowired
	public DefaultSysProvisioningArchiveService(
			SysProvisioningArchiveRepository repository) {
		super(repository);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW) // we want log in archive always
	public SysProvisioningArchiveDto archive(ProvisioningOperation provisioningOperation) {
		SysProvisioningArchiveDto archive = new SysProvisioningArchiveDto.Builder(provisioningOperation).build();
		// preserve original operation creator
		archive.setCreator(provisioningOperation.getCreator());
		archive.setCreatorId(provisioningOperation.getCreatorId());
		archive.setOriginalCreator(provisioningOperation.getOriginalCreator());
		archive.setOriginalCreatorId(provisioningOperation.getOriginalCreatorId());
		return save(archive);
	}
}
