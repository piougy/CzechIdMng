package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRoleValidRequest;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleValidRequestRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleValidRequestService;

/**
 * Default implementation {@link IdmIdentityRoleValidRequestService}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
public class DefaultIdentityRoleValidRequestService extends AbstractReadWriteEntityService<IdmIdentityRoleValidRequest, EmptyFilter> implements IdmIdentityRoleValidRequestService {
	
	private final IdmIdentityRoleValidRequestRepository repository;
	
	@Autowired
	public DefaultIdentityRoleValidRequestService(
			IdmIdentityRoleValidRequestRepository repository) {
		super(repository);
		//
		this.repository = repository;
	}
	
	

	@Override
	public IdmIdentityRoleValidRequest createByIdentityRole(IdmIdentityRole identityRole) {
		IdmIdentityRoleValidRequest entity = repository.findOneByIdentityRole(identityRole);
		//
		if (entity == null) {
			entity = new IdmIdentityRoleValidRequest();
			entity.setResult(new OperationResult.Builder(OperationState.CREATED).build());
			entity.setIdentityRole(identityRole);
		}
		//
		// just update modified date
		return this.save(entity);
	}



	@Override
	public List<IdmIdentityRoleValidRequest> findAllValid() {
		return this.findAllValidFrom(new DateTime());
	}

	@Override
	public List<IdmIdentityRoleValidRequest> findAllValidFrom(DateTime from) {
		return this.repository.findAllValidFrom(from.toLocalDate());
	}



	@Override
	public List<IdmIdentityRoleValidRequest> findAllValidRequestForRole(IdmRole role) {
		return repository.findAllByRole(role);
	}



	@Override
	public List<IdmIdentityRoleValidRequest> findAllValidRequestForIdentity(IdmIdentity identity) {
		return repository.findAllByIdentity(identity);
	}



	@Override
	public void deleteAll(List<IdmIdentityRoleValidRequest> entities) {
		if (entities != null && !entities.isEmpty()) {
			repository.delete(entities);
		}
	}



	@Override
	public List<IdmIdentityRoleValidRequest> findAllValidRequestForIdentityRole(IdmIdentityRole identityRole) {
		return repository.findAllByIdentityRole(identityRole);
	}



	@Override
	public List<IdmIdentityRoleValidRequest> findAllValidRequestForIdentityContract(
			IdmIdentityContract identityContract) {
		return repository.findAllByIdentityContract(identityContract);
	}
}
