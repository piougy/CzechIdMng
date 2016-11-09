package eu.bcvsolutions.idm.core.model.service;

import java.util.List;

import eu.bcvsolutions.idm.core.api.service.IdentifiableByNameEntityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.RoleCatalogueFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;

public interface IdmRoleCatalogueService extends ReadWriteEntityService<IdmRoleCatalogue, RoleCatalogueFilter>, IdentifiableByNameEntityService<IdmRoleCatalogue> {

	List<IdmRoleCatalogue> findRoots();
	
	List<IdmRoleCatalogue> findChildrenByParent(Long parent);
}
