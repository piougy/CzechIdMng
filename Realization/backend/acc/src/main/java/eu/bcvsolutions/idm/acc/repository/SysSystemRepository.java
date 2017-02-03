package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.dto.filter.SysSystemFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.rest.projection.SysSystemExcerpt;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Target system configuration
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "systems", //
		path = "systems", //
		itemResourceRel = "system", //
		excerptProjection = SysSystemExcerpt.class,
		exported = false // we are using repository metadata, but we want expose rest endpoint manually
	)
public interface SysSystemRepository extends AbstractEntityRepository<SysSystem, SysSystemFilter> {

	SysSystem findOneByName(@Param("name") String name);
	
	@Override
	@Query(value = "select e from SysSystem e" +
	        " where" +
	        "(?#{[0].text} is null or lower(e.name) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}) "
	        + "and "
	        + "(?#{[0].passwordPolicyValidationId} is null or e.passwordPolicyValidate.id = ?#{[0].passwordPolicyValidationId})"
	        + "and "
	        + "(?#{[0].passwordPolicyGenerationId} is null or e.passwordPolicyGenerate.id = ?#{[0].passwordPolicyGenerationId})")
	Page<SysSystem> find(SysSystemFilter filter, Pageable pageable);
	
}
