package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.forest.index.service.api.ForestIndexService;
import eu.bcvsolutions.forest.index.service.impl.BaseForestContentService;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleCatalogueRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleCatalogueForestContentService;

/**
 * Index and search role catalogue items by forest index
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultIdmRoleCatalogueForestContentService 
		extends BaseForestContentService<IdmRoleCatalogue, IdmForestIndexEntity, UUID>
		implements IdmRoleCatalogueForestContentService {
	
	private final IdmRoleCatalogueRepository repository;
	
	@Autowired
	public DefaultIdmRoleCatalogueForestContentService(
			ForestIndexService<IdmForestIndexEntity, UUID> forestIndexService,
			IdmRoleCatalogueRepository repository) {
		super(forestIndexService, repository);
		//
		this.repository = repository;
	}
	
	@Override
	public void rebuildIndexes(String forestTreeType) {
		throw new UnsupportedOperationException("Use TreeNodeService.rebuildIndexes instead - added long running task support");
	}

	@Override
	public Page<IdmRoleCatalogue> findRoots(String forestTreeType, Pageable pageable) {
		return repository.findRoots(pageable);
	}
}
