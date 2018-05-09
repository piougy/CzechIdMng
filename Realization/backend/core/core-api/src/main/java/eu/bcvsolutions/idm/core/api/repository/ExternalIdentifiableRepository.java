package eu.bcvsolutions.idm.core.api.repository;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;

/**
 * Common repository for base entities
 * 
 * @param <E> entity type
 * @param <ID> entity identifier type
 * @author Radek Tomiška
 */
@NoRepositoryBean
public interface ExternalIdentifiableRepository<E extends ExternalIdentifiable, ID extends Serializable> 
		extends PagingAndSortingRepository<E, ID>, JpaSpecificationExecutor<E> {

	/**
	 * Returns count of entities with the same external id, with other id that given. 
	 * 
	 * @param Serializable
	 * @return
	 */
	@Query(value = "SELECT COUNT(1) FROM #{#entityName} e "
	        + "WHERE "
	        + "e.externalId = :externalId "
	        + "AND "
	        + "(:id is null OR e.id != :id)")
	Long countOtherByExternalId(@Param("externalId") String externalId, @Param("id") ID Serializable);
	
	/**
	 * Find record by given external id.
	 * 
	 * @param externalId
	 * @return
	 */
	E findOneByExternalId(String externalId);
}

