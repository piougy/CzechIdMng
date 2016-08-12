package eu.bcvsolutions.idm.configuration.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import eu.bcvsolutions.idm.configuration.entity.IdmConfiguration;
import eu.bcvsolutions.idm.core.model.repository.BaseRepository;

/**
 * Application configuration
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@RepositoryRestResource( //
		collectionResourceRel = "configurations", // 
		path = "configurations", //
		itemResourceRel = "configuration")
public interface IdmConfigurationRepository extends BaseRepository<IdmConfiguration> {

	/**
	 * Public configurations only
	 * 
	 * @return
	 */
	List<IdmConfiguration> findAllBySecuredIsFalse();
	
	/**
	 * Returns all configurations based on current user authorities
	 */
	@Override
	@PostFilter("filterObject.secured == false or hasAuthority('CONFIGURATIONSECURED_READ')")
	Iterable<IdmConfiguration> findAll();
	
	/**
	 * Returns all configurations based on current user authorities
	 */
	@Override
	@Query(value = "select e from IdmConfiguration e" +
	        " where "
	        + "( "
	        	+ "e.secured = :#{hasAuthority('CONFIGURATIONSECURED_READ')} "
	        	+ "or e.secured = false"
	        + ")")
	Page<IdmConfiguration> findAll(Pageable pageable);
	
	/**
	 * Returns all configurations based on current user authorities
	 */
	@Override
	@PostFilter("filterObject.secured == false or hasAuthority('CONFIGURATIONSECURED_READ')")
	Iterable<IdmConfiguration> findAll(Sort sort);	
	
	/**
	 * Returns configuration by given identifier. Security is evaluated.
	 * 
	 * @param id
	 * @return
	 */
	@Override
	@PostAuthorize("returnObject == null or returnObject.secured == false or hasAuthority('CONFIGURATIONSECURED_READ')")
	IdmConfiguration findOne(@Param("id") Long id);
	
	/**
	 * Returns configuration by given name. Security is evaluated.
	 * 
	 * @param name
	 * @return
	 */
	@PostAuthorize("returnObject == null or returnObject.secured == false or hasAuthority('CONFIGURATIONSECURED_READ')")
	IdmConfiguration findOneByName(@Param("name") String name);
	
	/**
	 * Returns configuration by given name - for internal purpose.
	 * 
	 * @param name
	 * @return
	 */
	@RestResource(exported = false)
	@Query(value = "select e from #{#entityName} e" +
	        " where "
	        + "e.name = :name")
	IdmConfiguration get(@Param("name") String name);
	
	/**
	 * Returns pageable configurations based on current user authorities
	 * 
	 * @param text
	 * @param pageable
	 * @return
	 */
	@Query(value = "select e from IdmConfiguration e" +
	        " where " +
	        "(:text is null or lower(e.name) like :#{#text == null ? '%' : '%'.concat(#text.toLowerCase()).concat('%')}) "
	        + "and "
	        + "( "
	        	+ "e.secured = :#{hasAuthority('CONFIGURATIONSECURED_READ')} "
	        	+ "or e.secured = false"
	        + ")")
	@RestResource(path = "quick", rel = "quick")
	Page<IdmConfiguration> findByQuick(@Param(value = "text") String text, Pageable pageable);
	
	@PreAuthorize("hasAuthority('CONFIGURATIONSECURED_WRITE') or (hasAuthority('CONFIGURATION_WRITE') and #entity?.secured == false)")
	<S extends IdmConfiguration> IdmConfiguration save(@Param("entity") IdmConfiguration entity);
	
	@PreAuthorize("hasAuthority('CONFIGURATIONSECURED_DELETE') or (hasAuthority('CONFIGURATION_DELETE') and @idmConfigurationRepository.findOne(#id)?.secured == false)")
	void delete(@Param("id") Long id);
	
	@PreAuthorize("hasAuthority('CONFIGURATIONSECURED_DELETE') or (hasAuthority('CONFIGURATION_DELETE') and #entity?.secured == false)")
	void delete(@Param("entity") IdmConfiguration entity);
}
