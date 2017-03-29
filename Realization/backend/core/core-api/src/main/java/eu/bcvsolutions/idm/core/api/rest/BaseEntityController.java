package eu.bcvsolutions.idm.core.api.rest;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * 
 * @author Radek Tomiška
 *
 * @param <E>
 * @deprecated use {@link BaseDtoController}
 */
public interface BaseEntityController<E extends BaseEntity> extends BaseController {
	
	static final String TREE_BASE_PATH = "/tree";
}
