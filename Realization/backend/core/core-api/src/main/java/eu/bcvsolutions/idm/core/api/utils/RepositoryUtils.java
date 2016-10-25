package eu.bcvsolutions.idm.core.api.utils;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

public final class RepositoryUtils {

	/**
	 * Return collection of entity ids usable in repository query. 
	 * If {@code entities} is null or empty, returns collection with non-existent {@code Long} id, so could be used in IN clause etc.
	 * 
	 * @param entities
	 * @return
	 */
	public static List<Long> queryEntityIds(List<BaseEntity> entities) {
		List<Long> entityIds = new ArrayList<>();
		if (entities != null && !entities.isEmpty()) {
			for(BaseEntity entity : entities) {
				entityIds.add(entity.getId());
			}
		} else {
			// add non-existent long id 
			entityIds.add(-1L);
		}
		return entityIds;
	}
	
	
}
