package eu.bcvsolutions.idm.core.model.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleCatalogueFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.rest.projection.IdmRoleCatalogueExcerpt;

/**
 * Role catalogue repository
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@RepositoryRestResource( //
		collectionResourceRel = "roleCatalogues", // 
		path = "role-catalogues", //
		itemResourceRel = "roleCatalogue", //
		excerptProjection = IdmRoleCatalogueExcerpt.class,
		exported = false,
		collectionResourceDescription = @Description("Role catalogues"))
public interface IdmRoleCatalogueRepository extends AbstractEntityRepository<IdmRoleCatalogue, RoleCatalogueFilter> {
	
	/*
	 * (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository#find(eu.bcvsolutions.idm.core.api.dto.BaseFilter, Pageable)
	 */
	@Override
	@Query(value = "select e from IdmRoleCatalogue e left join e.parent p" +
	        " where" +
	        " (" +
				" lower(e.name) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}" +
		        " or lower(e.code) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}" +
	        " )" +
	        " and " +
	        " (?#{[0].code} is null or lower(e.code) like ?#{[0].code == null ? '%' : '%'.concat([0].code.toLowerCase()).concat('%')})" +
	        " and " +
	        " (?#{[0].name} is null or lower(e.name) like ?#{[0].name == null ? '%' : '%'.concat([0].name.toLowerCase()).concat('%')})" +
	        " and (?#{[0].parentId} is null or p.id = ?#{[0].parentId})")
	Page<IdmRoleCatalogue> find(RoleCatalogueFilter filter, Pageable pageable);
	
	IdmRoleCatalogue findOneByName(@Param("name") String name);
	
	@Query(value = "select e from IdmRoleCatalogue e" +
			" where" +
			" (:parentId is null and e.parent.id IS NULL) or (e.parent.id = :parentId)")
	Page<IdmRoleCatalogue> findChildren(@Param(value = "parentId") UUID parentId, Pageable pageable);
}
