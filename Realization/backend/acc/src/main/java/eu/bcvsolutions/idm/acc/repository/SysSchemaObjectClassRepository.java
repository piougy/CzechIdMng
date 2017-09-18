package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import eu.bcvsolutions.idm.acc.dto.filter.SchemaObjectClassFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Schema object class
 * 
 * @author Svanda
 *
 */
public interface SysSchemaObjectClassRepository extends AbstractEntityRepository<SysSchemaObjectClass> {

	@Query(value = "select e from SysSchemaObjectClass e" +
	        " where" +
	        " (?#{[0].systemId} is null or e.system.id = ?#{[0].systemId})" +
	        " and" +
	        " (lower(e.objectClassName) like ?#{[0].objectClassName == null ? '%' : '%'.concat([0].objectClassName.toLowerCase()).concat('%')})")
	Page<SysSchemaObjectClass> find(SchemaObjectClassFilter filter, Pageable pageable);
	
}
