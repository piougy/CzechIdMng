package eu.bcvsolutions.idm.core.api.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;

import eu.bcvsolutions.idm.core.api.domain.ResultCode;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Utils for Spring Data repositories
 * 
 * @author Radek Tomiška
 *
 */
public final class RepositoryUtils {
	
	private RepositoryUtils() {
	}

	/**
	 * Return collection of entity ids usable in repository query. 
	 * If {@code entities} is null or empty, returns collection with non-existent {@code Long} id, so could be used in IN clause etc.
	 * 
	 * @param entities
	 * @return
	 */
	public static List<UUID> queryEntityIds(List<BaseEntity> entities) {
		List<UUID> entityIds = new ArrayList<>();
		if (entities != null && !entities.isEmpty()) {
			for(BaseEntity entity : entities) {
				entityIds.add(entity.getId());
			}
		} else {
			// add non-existent long id 
			entityIds.add(UUID.randomUUID());
		}
		return entityIds;
	}
	
	/**
	 * TODO: constraint name - result code mapping
	 * 
	 * @param ex
	 * @return
	 */
	public static ResultCode resolveResultCode(DataIntegrityViolationException  ex) {
		throw new UnsupportedOperationException("not implemented");
	}
	
}
