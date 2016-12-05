package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.service.IdentifiableByNameEntityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.RoleCatalogueFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;

/**
 * Role could be in one catalogue (simply roles folder)
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public interface IdmRoleCatalogueService extends ReadWriteEntityService<IdmRoleCatalogue, RoleCatalogueFilter>, IdentifiableByNameEntityService<IdmRoleCatalogue> {

	List<IdmRoleCatalogue> findRoots();
	
	List<IdmRoleCatalogue> findChildrenByParent(UUID parent);
}
