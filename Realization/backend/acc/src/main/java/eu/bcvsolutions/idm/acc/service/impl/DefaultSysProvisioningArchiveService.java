package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.domain.ProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningArchive;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningArchiveRepository;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

/**
 * Archived provisioning operations
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultSysProvisioningArchiveService
		extends AbstractReadWriteEntityService<SysProvisioningArchive, EmptyFilter> implements SysProvisioningArchiveService {

	@Autowired
	public DefaultSysProvisioningArchiveService(
			SysProvisioningArchiveRepository repository) {
		super(repository);
	}

	@Override
	public SysProvisioningArchive archive(ProvisioningOperation provisioningOperation) {
		SysProvisioningArchive archive = new SysProvisioningArchive.Builder(provisioningOperation).build();
		// preserve original operation creator
		archive.setCreated(provisioningOperation.getCreated());
		archive.setCreator(provisioningOperation.getCreator());
		archive.setCreatorId(provisioningOperation.getCreatorId());
		archive.setOriginalCreator(provisioningOperation.getOriginalCreator());
		archive.setOriginalCreatorId(provisioningOperation.getOriginalCreatorId());
		return save(archive);
	}
}
