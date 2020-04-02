package eu.bcvsolutions.idm.core.eav.repository;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormProjection;

/**
 * Form projection - entity can be created / edited by different form projection.
 * 
 * @author Radek Tomiška 
 * @since 10.2.0
 */
public interface IdmFormProjectionRepository extends AbstractEntityRepository<IdmFormProjection> {
	
	/**
	 * Codeable lookup.
	 * 
	 * @param code code
	 * @return projection
	 */
	IdmFormProjection findOneByCode(String code);
}
