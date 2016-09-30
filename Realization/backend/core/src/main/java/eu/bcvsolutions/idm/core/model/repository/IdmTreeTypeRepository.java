package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.repository.projection.IdmTreeTypeExcerpt;

/**
 * Repository for tree types
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@RepositoryRestResource(
		collectionResourceRel = "treetypes",
		path = "treetypes",
		itemResourceRel = "treetype",
		collectionResourceDescription = @Description("Tree types"),
		itemResourceDescription = @Description("Tree types"),
		excerptProjection = IdmTreeTypeExcerpt.class,
		exported = false
	)
public interface IdmTreeTypeRepository extends BaseRepository<IdmTreeType> {
	
	IdmTreeType findOneByName(@Param("name") String name);
	
	@Query(value = "select e from IdmTreeType e" +
	        " where" +
	        "(:name is null or lower(e.name) like :#{#name == null ? '%' : '%'.concat(#name.toLowerCase()).concat('%')})")
	@RestResource(path = "quick", rel = "quick")
	Page<IdmTreeType> findByName(@Param(value = "name") String name, Pageable pageable);
}
