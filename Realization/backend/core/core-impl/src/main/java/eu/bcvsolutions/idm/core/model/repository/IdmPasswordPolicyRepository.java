package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.dto.filter.PasswordPolicyFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;
import eu.bcvsolutions.idm.core.rest.projection.IdmPasswordPolicyExcerpt;
/**
 * Password policy cointain set of rule, for create, update user password.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RepositoryRestResource(
		collectionResourceRel = "passwordPolicy",
		itemResourceRel = "passwordPolicy",
		collectionResourceDescription = @Description("Password policy"),
		itemResourceDescription = @Description("Password policy"),
		excerptProjection = IdmPasswordPolicyExcerpt.class,
		exported = false
	)
public interface IdmPasswordPolicyRepository extends AbstractEntityRepository<IdmPasswordPolicy, PasswordPolicyFilter> {
	
	@Override
	@Query(value = "select e from IdmPasswordPolicy e" +
	        " where"
	        + " ("
		        + " ?#{[0].text} is null"
		        + " or lower(e.name) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}"
	        + " )")
	Page<IdmPasswordPolicy> find(PasswordPolicyFilter filter, Pageable pageable);
}
