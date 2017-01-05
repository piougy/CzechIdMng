package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.dto.filter.RuleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRule;
import eu.bcvsolutions.idm.core.rest.projection.IdmRuleExcerpt;

/**
 * Repository for rules.
 * @see {@link IdmRule}
 * @see {@link RuleFilter}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RepositoryRestResource(
		collectionResourceRel = "rules",
		itemResourceRel = "rules",
		collectionResourceDescription = @Description("Rules"),
		itemResourceDescription = @Description("Rules"),
		excerptProjection = IdmRuleExcerpt.class,
		exported = false
	)
public interface IdmRuleRepository extends AbstractEntityRepository<IdmRule, RuleFilter> {

	@Override
	@Query(value = "select e from IdmRule e" +
	        " where"
	        + " ("
		        + " ?#{[0].text} is null"
		        + " or lower(e.name) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}"
	        + " ) "
	        + "AND "
	        + " ("
	        	+ " ?#{[0].description} is null"
	        	+ " or lower(e.description) like ?#{[0].description == null ? '%' : '%'.concat([0].description.toLowerCase()).concat('%')}"
	        + " ) "
	        + "AND "
	        + " ("
	        	+ " ?#{[0].category} is null"
	        	+ " or lower(e.category) like ?#{[0].category == null ? '%' : '%'.concat([0].category.toLowerCase()).concat('%')}"
	        + " )")
	Page<IdmRule> find(RuleFilter filter, Pageable pageable);
}
