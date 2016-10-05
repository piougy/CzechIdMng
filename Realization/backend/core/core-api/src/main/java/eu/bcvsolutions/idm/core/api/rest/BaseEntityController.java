package eu.bcvsolutions.idm.core.api.rest;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

public interface BaseEntityController<E extends BaseEntity> {
	
	static final String BASE_PATH = "/api/v1";
	
	static final String TREE_BASE_PATH = "/tree";
}
