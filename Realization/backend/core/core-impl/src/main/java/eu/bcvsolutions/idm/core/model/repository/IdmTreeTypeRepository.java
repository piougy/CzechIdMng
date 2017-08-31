package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmTreeTypeFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;

/**
 * Repository for tree types
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
public interface IdmTreeTypeRepository extends AbstractEntityRepository<IdmTreeType, IdmTreeTypeFilter> {
	
	@Override
	@Query(value = "select e from #{#entityName} e" +
	        " where"
	        + " ("
		        + " ?#{[0].text} is null"
		        + " or lower(e.code) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}"
		        + " or lower(e.name) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}"
	        + " )")
	Page<IdmTreeType> find(IdmTreeTypeFilter filter, Pageable pageable);
	
	/**
	 * Finds treeType by code (unique).
	 * 
	 * @param code
	 * @return
	 */
	IdmTreeType findOneByCode(@Param("code") String code);
}
