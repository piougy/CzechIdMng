package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.rest.projection.IdmRoleExcerpt;

/**
 * Roles repository
 * 
 * @author Radek Tomiška 
 *
 */
@RepositoryRestResource( //
		collectionResourceRel = "roles", // 
		path = "roles", //
		itemResourceRel = "role", //
		excerptProjection = IdmRoleExcerpt.class,
		exported = false)
public interface IdmRoleRepository extends AbstractEntityRepository<IdmRole, RoleFilter> {
	
	public static final String ADMIN_ROLE = "superAdminRole"; // TODO: move to configurationService
	
	/**
	 * @deprecated use criteria api
	 */
	@Override
	@Deprecated
	@Query(value = "select e from #{#entityName} e" +
	        " where"
	        + " (?#{[0].text} is null or lower(e.name) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')})"
	        + " and (?#{[0].roleType} is null or e.roleType = ?#{[0].roleType})"
	        + " and "
	        + " ( "
	        	+ "?#{[0].roleCatalogue} is null"
	        	+ " or"
	        	+ " exists (from IdmRoleCatalogueRole rc where rc.role = e and rc.roleCatalogue.forestIndex.lft BETWEEN ?#{[0].roleCatalogue == null ? null : [0].roleCatalogue.lft} and ?#{[0].roleCatalogue == null ? null : [0].roleCatalogue.rgt})"
	        + " ) "
	        + " and"
	        + "	("
	        	+ "?#{[0].guarantee} is null"
	        	+ " or"
	        	+ " exists (from IdmRoleGuarantee rg where rg.role = e and rg.guarantee = ?#{[0].guarantee})"
        	+ " )"
        	+ " and "
 	  	    + " ("
 	  	    	+ " ?#{[0].property} is null "
 	  	    	+ " or (?#{[0].property} = 'name' and e.name = ?#{[0].value})"
 	        + " )")
	Page<IdmRole> find(RoleFilter filter, Pageable pageable);
	
	IdmRole findOneByName(@Param("name") String name);
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
	@Query(value = "select e from #{#entityName} e where e = :role")
	IdmRole getPersistedRole(@Param("role") IdmRole role);

	@Query("select s.sub from #{#entityName} e join e.subRoles s where e.id = :roleId")
	List<IdmRole> getSubroles(@Param("roleId") UUID roleId);
	
}
