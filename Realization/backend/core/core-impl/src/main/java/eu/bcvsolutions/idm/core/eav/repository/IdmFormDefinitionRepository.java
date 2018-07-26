package eu.bcvsolutions.idm.core.eav.repository;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;

/**
 * Form definition repository
 * 
 * @author Radek Tomiška 
 *
 */
public interface IdmFormDefinitionRepository extends AbstractEntityRepository<IdmFormDefinition> {
	
	/**
	 * Returns all form definitions by given type
	 * 
	 * @param type
	 * @return
	 */
	List<IdmFormDefinition> findAllByType(@Param("type") String type);
	
	/**
	 * Returns form definition by given type and name (unique)
	 * 
	 * @param type
	 * @param name
	 * @return
	 */
	IdmFormDefinition findOneByTypeAndCode(@Param("type") String type, @Param("code") String code);
	
	/**
	 * Returns main form definition   (unique)
	 * 
	 * @param type
	 * @param name
	 * @return
	 */
	IdmFormDefinition findOneByTypeAndMainIsTrue(@Param("type") String type);
	
	/**
	 * Clears main definition for given type
	 * 
	 * @param updatedEntityId
	 * @deprecated use update instead, this method is quicker but skips audit
	 */
	@Modifying
	@Deprecated
	@Query("update #{#entityName} e set e.main = false, e.modified = :modified"
			+ " where e.type = :type and (:updatedEntityId is null or e.id != :updatedEntityId)")
	void clearMain(@Param("type") String type, @Param("updatedEntityId") UUID updatedEntityId, @Param("modified") DateTime modified);
}
