package eu.bcvsolutions.idm.core.audit.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.audit.dto.filter.IdmAuditFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.audit.entity.IdmAudit;

@RepositoryRestResource(//
		collectionResourceRel = "audits", //
		path = "audits", //
		itemResourceRel = "audit", //
		exported = false // 
	)
public interface IdmAuditRepository extends AbstractEntityRepository<IdmAudit> {
	
	@Query(value = "SELECT e "
				+ "FROM "
					+ "#{#entityName} e "
				+ "WHERE "
					+ "("
						+ " ?#{[0].id == null ? 'null' : ''} = 'null' or e.id = ?#{[0].id} "
					+ ")"
					+ " AND "
					+ "("
						+ "?#{[0].text} IS null "
						+ "OR CAST(e.id as string) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')} "
					+ ")"	
					+ " AND "
					+ "("
						+ "?#{[0].modification} IS null "
						+ "OR lower(e.modification) like ?#{[0].modification == null ? '%' : '%'.concat([0].modification.toLowerCase()).concat('%')} "
					+ ")"
					+ " AND "
					+ "("
						+ "?#{[0].changedAttributes} IS null "
						+ "OR lower(e.changedAttributes) like ?#{[0].changedAttributes == null ? '%' : '%'.concat([0].changedAttributes.toLowerCase()).concat('%')} "
					+ ")"
					+ " AND "
					+ "("
						+ "?#{[0].modifier} IS null "
						+ "OR "
						+ "lower(e.modifier) like ?#{[0].modifier == null ? '%' : '%'.concat([0].modifier.toLowerCase()).concat('%')} "
						+ "OR "
						+ "lower(e.originalModifier) like ?#{[0].modifier == null ? '%' : '%'.concat([0].modifier.toLowerCase()).concat('%')} "
					+ ")"
					+ " AND "
					+ "("
						+ "?#{[0].entityId} IS null "
						+ "OR e.entityId = ?#{[0].entityId} "
					+ ")"
					+ " AND "
					+ "(" 
						+ "?#{[0].from == null ? 'null' : ''} = 'null' or e.timestamp >= ?#{[0].from == null ? null : [0].from.getMillis()} "
					+ ")"
					+ " AND "
					+ "("
						+ "?#{[0].till == null ? 'null' : ''} = 'null' or e.timestamp <= ?#{[0].till == null ? null : [0].till.getMillis()} "
					+ ")"
					+ " AND "
					+ "("
						+ "?#{[0].type} IS null "
						+ "OR lower(e.type) like ?#{[0].type == null ? '%' : '%'.concat([0].type.toLowerCase())} "
					+ ") "
					// owner + subOwner attributes
					+ "AND "
					+ "( "
						+ "?#{[0].ownerCode} IS null "
						+ "OR lower(e.ownerCode) like ?#{[0].ownerCode == null ? '%' : '%'.concat([0].ownerCode.toLowerCase())} "
					+ ") "
					+ "AND "
					+ "( "
						+ "?#{[0].ownerType} IS null "
						+ "OR lower(e.ownerType) like ?#{[0].ownerType == null ? '%' : '%'.concat([0].ownerType.toLowerCase())} "
					+ ") "
					+ "AND "
					+ "( "
						+ "?#{[0].ownerId} IS null "
						+ "OR e.ownerId = ?#{[0].ownerId} "
					+ ") "
					+ "AND "
					+ "( "
						+ " (?#{[0].ownerIds == null ? 0 : [0].ownerIds.size()} = 0 OR e.ownerId IN (?#{[0].ownerIds})) "
					+ ") "
					+ "AND "
					+ "( "
						+ "?#{[0].subOwnerId} IS null "
						+ "OR e.subOwnerId = ?#{[0].subOwnerId} "
					+ ") "
					+ "AND "
					+ "( "
						+ "?#{[0].subOwnerCode} IS null "
						+ "OR lower(e.subOwnerCode) like ?#{[0].subOwnerCode == null ? '%' : '%'.concat([0].subOwnerCode.toLowerCase())} "
					+ ") "
					+ "AND "
					+ "( "
						+ "?#{[0].subOwnerType} IS null "
						+ "OR lower(e.subOwnerType) like ?#{[0].subOwnerType == null ? '%' : '%'.concat([0].subOwnerType.toLowerCase())} "
					+ ") ")
	Page<IdmAudit> find(IdmAuditFilter filter, Pageable pageable);
	
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
