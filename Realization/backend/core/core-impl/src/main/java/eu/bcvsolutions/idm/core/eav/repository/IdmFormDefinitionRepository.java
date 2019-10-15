package eu.bcvsolutions.idm.core.eav.repository;

import java.util.List;

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
}
