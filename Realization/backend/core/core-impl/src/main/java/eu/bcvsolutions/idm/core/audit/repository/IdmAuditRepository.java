package eu.bcvsolutions.idm.core.audit.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.audit.entity.IdmAudit;

@RepositoryRestResource(//
		collectionResourceRel = "audits", //
		path = "audits", //
		itemResourceRel = "audit", //
		exported = false // 
	)
public interface IdmAuditRepository extends AbstractEntityRepository<IdmAudit> {
	
	
	/**
	 * Query get previous version, from entity id and id current revision.
	 * This method is @Deprecated please use {@link #getPreviousVersion(UUID, Long)}.
	 *
	 * @param entityId
	 * @param revId
	 * @param pageable
	 * @return
	 */
	@Deprecated
	@Query(value = "SELECT e "
			+ "FROM "
				+ "#{#entityName} e "
			+ "WHERE "
				+ "("
					+ "e.entityId = :entityId "
				+ ")"
				+ " AND "
				+ "("
					+ " e.id < :revId "
				+ ") "
				+ " ORDER BY e.id DESC " )
	Page<IdmAudit> getPreviousVersion(@Param(value = "entityId") UUID entityId, @Param(value = "revId") Long revId, Pageable pageable);

	/**
	 * Query get previous version, from entity id and id current revision.
	 *
	 * @param entityId
	 * @param revId
	 * @return
	 */
	@Query(value = "SELECT e "
			+ "FROM "
				+ "#{#entityName} e "
			+ "WHERE "
				+ "( "
					+ "e.id = (SELECT max(z.id) FROM #{#entityName} z where z.entityId = :entityId AND z.id < :revId)"
				+ ") ")
	IdmAudit getPreviousVersion(@Param(value = "entityId") UUID entityId, @Param(value = "revId") Long revId);
	
	@Query(value = "SELECT DISTINCT(e.ownerId) "
			+ "FROM "
				+ "#{#entityName} e "
			+ "WHERE "
				+ ":ownerType = e.ownerType "
			+ "AND "
				+ ":ownerCode IS null or lower(e.ownerCode) like CONCAT('%', lower(:ownerCode), '%') " )
	List<String> findDistinctOwnerIdByOwnerTypeAndOwnerCode(@Param(value = "ownerType") String ownerType, @Param(value = "ownerCode") String ownerCode);
	
	/**
	 * Find one {@link IdmAudit} by ID. There can't be used normal method findOne.
	 * {@link IdmAudit} has long identifier not UUID.
	 * 
	 * @param id
	 * @return
	 */
	IdmAudit findOneById(@Param(value = "id") Long id);
}
