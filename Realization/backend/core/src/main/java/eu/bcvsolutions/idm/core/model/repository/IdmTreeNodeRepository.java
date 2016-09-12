package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.repository.projection.IdmTreeNodeExcerpt;

@RepositoryRestResource(//
	collectionResourceRel = "treenodes", //
	path = "treenodes", //
	itemResourceRel = "treenode", //
	collectionResourceDescription = @Description("Tree nodes") , //
	itemResourceDescription = @Description("Tree nodes"), //
	excerptProjection = IdmTreeNodeExcerpt.class
)
public interface IdmTreeNodeRepository extends BaseRepository<IdmTreeNode> {
	
	@Query(value = "select e from IdmTreeNode e" +
	        " where" +
	        "(:text is null or lower(e.name) like :#{#text == null ? '%' : '%'.concat(#text.toLowerCase()).concat('%')})" + 
	        " and (:parent is null or e.parent = :parent)")
	@RestResource(path = "quick", rel = "quick")
	Page<IdmIdentity> findByNameOrParentName(@Param(value = "text") String text, @Param(value = "parent") IdmTreeNode parent, Pageable pageable);

	@Query(value = "select e from IdmTreeNode e" +
			" where" +
			" (:parent is null and e.parent.id IS NULL) or (e.parent.id = :parent)")
	@RestResource(path = "children", rel = "children")
	List<IdmIdentity> findChildrenByParent(@Param(value = "parent") Long parent);
	
	@RestResource(exported = false)
	IdmTreeNode findOneByParentIsNull();
}
