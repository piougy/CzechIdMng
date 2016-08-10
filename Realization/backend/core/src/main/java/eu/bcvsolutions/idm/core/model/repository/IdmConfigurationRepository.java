package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import eu.bcvsolutions.idm.core.model.entity.IdmConfiguration;

@RepositoryRestResource( //
		collectionResourceRel = "configurations", // 
		path = "configurations", //
		itemResourceRel = "configuration")
public interface IdmConfigurationRepository extends BaseRepository<IdmConfiguration> {

	IdmConfiguration findOneByName(@Param("name") String name);
	
	@Query(value = "select e from IdmConfiguration e" +
	        " where" +
	        " (:text is null or lower(e.name) like :#{#text == null ? '%' : '%'.concat(#text.toLowerCase()).concat('%')})")
	@RestResource(path = "quick", rel = "quick")
	Page<IdmConfiguration> findByQuick(@Param(value = "text") String text, Pageable pageable);
}
