package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.repository.BaseRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.exception.TreeNodeException;
import eu.bcvsolutions.idm.core.model.dto.RoleCatalogueFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleCatalogueRepository;
import eu.bcvsolutions.idm.core.model.service.IdmRoleCatalogueService;

/**
 * Implementation of @IdmRoleCatalogueService
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
public class DefaultIdmRoleCatalogueService extends AbstractReadWriteEntityService<IdmRoleCatalogue, RoleCatalogueFilter>  implements IdmRoleCatalogueService {
	
	@Autowired
	private IdmRoleCatalogueRepository roleCatalogueRepository;;
	
	@Autowired
	private DefaultBaseTreeService<IdmRoleCatalogue> baseTreeService;
	
	@Override
	@Transactional(readOnly = true)
	public IdmRoleCatalogue getByName(String name) {
		return roleCatalogueRepository.findOneByName(name);
	}
	@Override
	protected BaseRepository<IdmRoleCatalogue, RoleCatalogueFilter> getRepository() {
		return roleCatalogueRepository;
	}
	
	@Override
	public IdmRoleCatalogue save(IdmRoleCatalogue entity) {
		// test role catalogue to parent and children
		if (this.baseTreeService.validateTreeNodeParents(entity)) {
			throw new TreeNodeException(CoreResultCode.ROLE_CATALOG_BAD_PARENT,  "Role catalog ["+entity.getName() +"] have bad parent.");
		}
		return super.save(entity);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmRoleCatalogue> findRoots() {
		return this.roleCatalogueRepository.findRoots();
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmRoleCatalogue> findChildrenByParent(Long parent) {
		return this.roleCatalogueRepository.findChildrenByParent(parent);
	}
	
}
