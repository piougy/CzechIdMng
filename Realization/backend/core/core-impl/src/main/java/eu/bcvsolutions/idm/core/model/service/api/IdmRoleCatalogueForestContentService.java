package eu.bcvsolutions.idm.core.model.service.api;

import java.util.UUID;

import eu.bcvsolutions.forest.index.service.api.ForestContentService;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;

/**
 * Index and search role catalogue items by forest index
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmRoleCatalogueForestContentService extends ForestContentService<IdmRoleCatalogue, IdmForestIndexEntity, UUID> {

}
