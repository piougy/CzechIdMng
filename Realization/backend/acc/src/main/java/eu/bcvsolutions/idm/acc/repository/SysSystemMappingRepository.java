package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import eu.bcvsolutions.idm.acc.dto.filter.SystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * System entity handling
 * 
 * @author Svanda
 *
 */
public interface SysSystemMappingRepository extends AbstractEntityRepository<SysSystemMapping> {

	@Query(value = "select e from SysSystemMapping e"+ 
			" where" +
			" (?#{[0].text} is null or lower(e.name) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')})"+
			" and" +
	        " (?#{[0].systemId} is null or e.objectClass.system.id = ?#{[0].systemId})"+
			" and" +
			" (?#{[0].objectClassId} is null or e.objectClass.id = ?#{[0].objectClassId})"+
			" and" +
			" (?#{[0].operationType} is null or e.operationType = ?#{[0].operationType})"+
			" and" +
			" (?#{[0].treeTypeId} is null or e.treeType.id = ?#{[0].treeTypeId})"+
			" and" +
			" (?#{[0].entityType} is null or e.entityType = ?#{[0].entityType})"
			)
	Page<SysSystemMapping> find(SystemMappingFilter filter, Pageable pageable);
}
