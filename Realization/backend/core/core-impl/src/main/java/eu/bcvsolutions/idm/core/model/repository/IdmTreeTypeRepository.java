package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;

/**
 * Repository for tree types
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
public interface IdmTreeTypeRepository extends 
		AbstractEntityRepository<IdmTreeType> {
	
	/**
	 * Finds treeType by code (unique).
	 * 
	 * @param code
	 * @return
	 */
	IdmTreeType findOneByCode(@Param("code") String code);
}
