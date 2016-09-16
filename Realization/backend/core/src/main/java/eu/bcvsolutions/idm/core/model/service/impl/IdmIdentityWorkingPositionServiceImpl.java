package eu.bcvsolutions.idm.core.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentityWorkingPosition;
import eu.bcvsolutions.idm.core.model.repository.BaseRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityWorkingPositionRepository;
import eu.bcvsolutions.idm.core.model.service.IdmIdentityWorkingPositionService;

@Service
public class IdmIdentityWorkingPositionServiceImpl extends AbstractReadWriteEntityService<IdmIdentityWorkingPosition> implements IdmIdentityWorkingPositionService {


	@Autowired
	private IdmIdentityWorkingPositionRepository repository;

	@Override
	protected BaseRepository<IdmIdentityWorkingPosition> getRepository() {
		return repository;
	}
}
