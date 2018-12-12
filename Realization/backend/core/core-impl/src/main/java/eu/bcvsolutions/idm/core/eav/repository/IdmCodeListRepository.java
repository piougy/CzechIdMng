package eu.bcvsolutions.idm.core.eav.repository;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.eav.entity.IdmCodeList;

/**
 * Repository for code lists
 * 
 * @author Radek Tomiška 
 * @since 9.4.0
 */
public interface IdmCodeListRepository extends AbstractEntityRepository<IdmCodeList> {

	/**
	 * Finds code list by code
	 * 
	 * @param code
	 * @return
	 */
	IdmCodeList findOneByCode(String code);
}
