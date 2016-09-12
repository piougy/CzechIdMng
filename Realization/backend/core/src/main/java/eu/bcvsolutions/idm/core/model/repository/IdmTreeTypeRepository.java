package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;

/**
 * Repository for tree types
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "treetypes", //
		path = "treetypes", //
		itemResourceRel = "treetype", //
		collectionResourceDescription = @Description("Tree types"),
		itemResourceDescription = @Description("Tree types")
	)
public interface IdmTreeTypeRepository extends BaseRepository<IdmTreeType> {
	
	IdmTreeType findOneByName(@Param("name") String name);
}
