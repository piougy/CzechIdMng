package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmConfiguration;

/**
 * Application configuration
 * 
 * @author Radek Tomiška 
 *
 */
@RepositoryRestResource( //
		collectionResourceRel = "configurations", // 
		path = "configurations", //
		itemResourceRel = "configuration",
		exported = false)
public interface IdmConfigurationRepository extends AbstractEntityRepository<IdmConfiguration, QuickFilter> {

	/**
	 * Public configurations only
	 * 
	 * @return
	 */
	List<IdmConfiguration> findAllBySecuredIsFalse();
	
	/**
	 * Returns configuration by given name - for internal purpose.
	 * 
	 * @param name
	 * @return
	 */
	@Query(value = "select e from #{#entityName} e" +
	        " where "
	        + "e.name = :name")
	IdmConfiguration get(@Param("name") String name);
	
	/**
	 * Returns configuration by given name. Security is evaluated.
	 * 
	 * @param name
	 * @return
	 */
	@PostAuthorize("returnObject == null or returnObject.secured == false or hasAuthority('" + IdmGroupPermission.CONFIGURATIONSECURED_READ + "')")
	IdmConfiguration findOneByName(@Param("name") String name);
	
	/**
	 * Returns pageable configurations based on current user authorities
	 * 
	 * @param text
	 * @param pageable
	 * @return
	 */
	@Override
	@Query(value = "select e from IdmConfiguration e" +
	        " where " +
	        "(?#{[0].text} is null or lower(e.name) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}) "
	        + "and "
	        + "( "
	        	+ "e.secured = :#{hasAuthority('" + IdmGroupPermission.CONFIGURATIONSECURED_READ + "')} "
	        	+ "or e.secured = false"
	        + ")")
	Page<IdmConfiguration> find(QuickFilter filter, Pageable pageable);
	
	/**
	 * Returns all configurations based on current user authorities
	 */
	@Override
	@PostFilter("filterObject.secured == false or hasAuthority('" + IdmGroupPermission.CONFIGURATIONSECURED_READ + "')")
	Iterable<IdmConfiguration> findAll();
	
	/**
	 * Returns all configurations based on current user authorities
	 */
	@Override
	@Query(value = "select e from IdmConfiguration e" +
	        " where "
	        + "( "
	        	+ "e.secured = :#{hasAuthority('" + IdmGroupPermission.CONFIGURATIONSECURED_READ + "')} "
	        	+ "or e.secured = false"
	        + ")")
	Page<IdmConfiguration> findAll(Pageable pageable);
	
	/**
	 * Returns all configurations based on current user authorities
	 */
	@Override
	@PostFilter("filterObject.secured == false or hasAuthority('" + IdmGroupPermission.CONFIGURATIONSECURED_READ + "')")
	Iterable<IdmConfiguration> findAll(Sort sort);	
	
	/**
	 * Returns configuration by given identifier. Security is evaluated.
	 * 
	 * @param id
	 * @return
	 */
	@Override
	@PostAuthorize("returnObject == null or returnObject.secured == false or hasAuthority('" + IdmGroupPermission.CONFIGURATIONSECURED_READ + "')")
	IdmConfiguration findOne(@Param("id") UUID id);
	
	
	@Override
	@SuppressWarnings("unchecked")
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.CONFIGURATIONSECURED_WRITE + "') or (hasAuthority('" + IdmGroupPermission.CONFIGURATION_WRITE + "') and #entity?.secured == false)")
	IdmConfiguration save(@Param("entity") IdmConfiguration entity);
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.CONFIGURATIONSECURED_DELETE + "') or (hasAuthority('" + IdmGroupPermission.CONFIGURATION_DELETE + "') and @idmConfigurationRepository.findOne(#id)?.secured == false)")
	void delete(@Param("id") UUID id);
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.CONFIGURATIONSECURED_DELETE + "') or (hasAuthority('" + IdmGroupPermission.CONFIGURATION_DELETE + "') and #entity?.secured == false)")
	void delete(@Param("entity") IdmConfiguration entity);
}
