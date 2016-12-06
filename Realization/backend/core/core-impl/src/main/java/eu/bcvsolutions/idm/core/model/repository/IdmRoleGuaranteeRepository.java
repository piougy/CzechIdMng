package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.dto.EmptyFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleGuarantee;

/**
 * Role guarantee repository
 * - role guarantee is controlled (CRUD) by role, but some operations are needed to call directly (remove etc.)
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "roleGuarantees", //
		path = "role-guarantees", //
		itemResourceRel = "roleGuarantee", //
		exported = false
)
public interface IdmRoleGuaranteeRepository extends AbstractEntityRepository<IdmRoleGuarantee, EmptyFilter> {
	
	/*
	 * (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository#find(eu.bcvsolutions.idm.core.api.dto.BaseFilter, Pageable)
	 */
	@Override
	@Query(value = "select e from #{#entityName} e")
	Page<IdmRoleGuarantee> find(EmptyFilter filter, Pageable pageable);
	
	/**
	 * Removes guarantee by given identity
	 * 
	 * @param guarantee
	 * @return
	 */
	int deleteByGuarantee(@Param("guarantee") IdmIdentity guarantee);
	
	/**
	 * Removes guarantee by given role
	 * 
	 * @param guarantee
	 * @return
	 */
	int deleteByRole(@Param("role") IdmRole role);
}
