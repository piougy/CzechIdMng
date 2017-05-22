package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmConfiguration;

/**
 * Application configuration
 * 
 * @author Radek Tomiška 
 *
 */
public interface IdmConfigurationRepository extends AbstractEntityRepository<IdmConfiguration, DataFilter> {

	/**
	 * @deprecated Use IdmConfugurationService (uses criteria api)
	 */
	@Override
	@Deprecated
	@Query(value = "select e from #{#entityName} e")
	default Page<IdmConfiguration> find(DataFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Use IdmConfugurationService (uses criteria api)");
	}
	
	/**
	 * Public configurations only
	 * 
	 * @return
	 */
	List<IdmConfiguration> findAllBySecuredIsFalse();
	
	/**
	 * Returns configuration by given name.
	 * 
	 * @param name
	 * @return
	 */
	IdmConfiguration findOneByName(@Param("name") String name);
	
	/**
	 * Returns configurations by given keyPrefix.
	 * 
	 * @param keyPrefix
	 * @param pageable
	 * @return
	 */
	Page<IdmConfiguration> findByNameStartingWith(@Param("name") String keyPrefix, Pageable pageable);
}
