package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.model.dto.EmptyFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityWorkingPosition;
import eu.bcvsolutions.idm.core.model.repository.BaseRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityWorkingPositionRepository;
import eu.bcvsolutions.idm.core.model.service.IdmIdentityWorkingPositionService;

@Service
public class IdmIdentityWorkingPositionServiceImpl extends AbstractReadWriteEntityService<IdmIdentityWorkingPosition, EmptyFilter> implements IdmIdentityWorkingPositionService {

	@Autowired
	private IdmIdentityWorkingPositionRepository repository;

	@Override
	protected BaseRepository<IdmIdentityWorkingPosition> getRepository() {
		return repository;
	}
	
	public List<IdmIdentityWorkingPosition> getWorkingPositions(IdmIdentity identity) {
		return repository.findAllByIdentity(identity, null);
	}
}
