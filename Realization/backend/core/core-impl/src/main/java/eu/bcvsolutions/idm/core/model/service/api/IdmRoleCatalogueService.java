package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.service.IdentifiableByNameEntityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.RoleCatalogueFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;

public interface IdmRoleCatalogueService extends ReadWriteEntityService<IdmRoleCatalogue, RoleCatalogueFilter>, IdentifiableByNameEntityService<IdmRoleCatalogue> {

	List<IdmRoleCatalogue> findRoots();
	
	List<IdmRoleCatalogue> findChildrenByParent(UUID parent);
}
