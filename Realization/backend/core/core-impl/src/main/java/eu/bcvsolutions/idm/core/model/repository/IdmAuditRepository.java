package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.repository.BaseRepository;
import eu.bcvsolutions.idm.core.model.dto.AuditFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmAudit;

@RepositoryRestResource(//
		collectionResourceRel = "audits", //
		path = "audits", //
		itemResourceRel = "audit", //
		exported = false // 
	)
public interface IdmAuditRepository extends BaseRepository<IdmAudit, AuditFilter> {
	
	// TODO: timestamp and datetime colision!
	@Override
	@Query(value = "SELECT e "
				+ "FROM "
					+ "IdmAudit e "
				+ "WHERE "
					+ "("
						+ "?#{[0].modification} IS null "
						+ "OR lower(e.modification) = ?#{[0].modification} "
					+ ")"
					+ " AND "
					+ "("
						+ "?#{[0].text} IS null "
						+ "OR lower(e.changedAttributes) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')} "
					+ ")"
					+ " AND "
					+ "("
						+ "?#{[0].from} IS null "
						+ "OR e.timestamp = ?#{[0].from} "
					+ ")"
					+ " AND "
					+ "("
						+ "?#{[0].to} IS null "
						+ "OR e.timestamp = ?#{[0].to} "
					+ ")"
					+ " AND "
					+ "("
						+ "?#{[0].entity} IS null "
						+ "OR e.type like ?#{[0].entity == null ? '%' : '%'.concat([0].entity.getName().toLowerCase()).concat('%')} "
					+ ")" )
	Page<IdmAudit> find(AuditFilter filter, Pageable pageable);
}
