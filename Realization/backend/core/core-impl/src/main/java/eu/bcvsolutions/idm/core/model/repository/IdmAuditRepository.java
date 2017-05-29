package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.dto.filter.AuditFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmAudit;

@RepositoryRestResource(//
		collectionResourceRel = "audits", //
		path = "audits", //
		itemResourceRel = "audit", //
		exported = false // 
	)
public interface IdmAuditRepository extends AbstractEntityRepository<IdmAudit, AuditFilter> {
	
	@Override
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
						+ "("
							+ "lower(e.modifier) like ?#{[0].modifier == null ? '%' : '%'.concat([0].modifier.toLowerCase()).concat('%')} "
							+ "AND "
							+ "lower(e.originalModifier) like ?#{[0].modifier == null ? '%' : '%'.concat([0].modifier.toLowerCase()).concat('%')} "
						+ ")"
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
					+ ")" )
	Page<IdmAudit> find(AuditFilter filter, Pageable pageable);
	
	@Query(value = "SELECT e "
			+ "FROM "
				+ "#{#entityName} e "
			+ "WHERE "
				+ "("
					+ "e.id in (:ids) "
				+ ")")
	Page<IdmAudit> findByIds(@Param(value = "ids") List<Long> ids, Pageable pageable);
	
	// Query get previous version, from entity id and id current revision
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
}
