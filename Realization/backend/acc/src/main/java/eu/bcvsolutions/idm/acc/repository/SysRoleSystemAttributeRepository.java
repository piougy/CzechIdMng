package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.dto.RoleSystemAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.repository.projection.SysRoleSystemAttributeExcerpt;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Mapping attribute to system for role
 * 
 * @author svandav
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "roleSystemAttributes", //
		path = "role-system-attributes", //
		itemResourceRel = "roleSystemAttribute", //
		excerptProjection = SysRoleSystemAttributeExcerpt.class, //
		exported = false // we are using repository metadata, but we want expose
							// rest endpoint manually
)
public interface SysRoleSystemAttributeRepository
		extends AbstractEntityRepository<SysRoleSystemAttribute, RoleSystemAttributeFilter> {

	/*
	 * (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository#find(eu.bcvsolutions.idm.core.api.dto.BaseFilter, Pageable)
	 */
	@Override
	@Query(value = "select e from SysRoleSystemAttribute e" + " where"
			+ " (?#{[0].roleSystemId} is null or e.roleSystem.id = ?#{[0].roleSystemId})")
	Page<SysRoleSystemAttribute> find(RoleSystemAttributeFilter filter, Pageable pageable);
}
