package eu.bcvsolutions.idm.core.rest;

import eu.bcvsolutions.idm.core.model.entity.BaseEntity;

public interface BaseEntityController<E extends BaseEntity> {
	
	static final String BASE_PATH = "/api/v1";
	
	static final String TREE_BASE_PATH = "/tree";
}
