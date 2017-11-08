package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.acc.dto.filter.SysSystemFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;

/**
 * Target system configuration
 * 
 * @author Radek Tomiška
 *
 */
public interface SysSystemRepository extends AbstractEntityRepository<SysSystem> {

	SysSystem findOneByName(@Param("name") String name);
	
	/**
	 * 
	 * @param filter
	 * @param pageable
	 * @return
	 * @deprecated use {@link SysSystemService#find(SysSystemFilter, Pageable, eu.bcvsolutions.idm.core.security.api.domain.BasePermission...)}
	 */
	@Deprecated
	@Query(value = "select e from SysSystem e" +
	        " where" +
	        "(?#{[0].text} is null or lower(e.name) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}) "
	        + "and "
	        + "(?#{[0].passwordPolicyValidationId} is null or e.passwordPolicyValidate.id = ?#{[0].passwordPolicyValidationId})"
	        + "and "
	        + "(?#{[0].passwordPolicyGenerationId} is null or e.passwordPolicyGenerate.id = ?#{[0].passwordPolicyGenerationId})"
	        + "and "
	        + "(?#{[0].virtual} is null or e.virtual = ?#{[0].virtual})")
	Page<SysSystem> find(SysSystemFilter filter, Pageable pageable);
	
	/**
	 * Query remove all references to password policy, passwordPolicyGenerate and passwordPolicyGenerate
	 * 
	 * @param entityId
	 * @return
	 */
	@Modifying
	@Query("UPDATE SysSystem e SET e.passwordPolicyValidate = NULL, e.passwordPolicyGenerate = NULL WHERE "
			+ "(:entity is null or e.passwordPolicyValidate = :entity) "
			+ "OR "
			+ "(:entity is null or e.passwordPolicyGenerate = :entity)")
	int clearPasswordPolicy(@Param("entity") IdmPasswordPolicy entity);
}
