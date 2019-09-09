package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.dto.SysSyncItemLogDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncItemLogFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass_;
import eu.bcvsolutions.idm.acc.entity.SysSyncActionLog_;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig_;
import eu.bcvsolutions.idm.acc.entity.SysSyncItemLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncItemLog_;
import eu.bcvsolutions.idm.acc.entity.SysSyncLog_;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping_;
import eu.bcvsolutions.idm.acc.repository.SysSyncItemLogRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSyncItemLogService;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;

/**
 * Default synchronization item log service
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysSyncItemLogService
		extends AbstractReadWriteDtoService<SysSyncItemLogDto, SysSyncItemLog, SysSyncItemLogFilter>
		implements SysSyncItemLogService {
	
	@Autowired
	public DefaultSysSyncItemLogService(SysSyncItemLogRepository repository) {
		super(repository);
	}
	
	
	
	@Query(value = "select e from SysSyncItemLog e"+ 
			" where" +
	        " (?#{[0].syncActionLogId} is null or e.syncActionLog.id = ?#{[0].syncActionLogId})" +
	        " and" +
	        " (lower(e.displayName) like ?#{[0].displayName == null ? '%' : '%'.concat([0].displayName.toLowerCase()).concat('%')})"
			)
	@Override
	protected List<Predicate> toPredicates(Root<SysSyncItemLog> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			SysSyncItemLogFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		
		// Display name
		String text = filter.getDisplayName();
		if (StringUtils.isNotEmpty(text)) {
			text = text.toLowerCase();
			predicates.add(builder.or(
					builder.like(builder.lower(root.get(SysSyncItemLog_.displayName)), "%" + text + "%")
					));
		}
		
		// Sync-action-log ID
		UUID syncActionId = filter.getSyncActionLogId();
		if (syncActionId != null) {
			predicates
					.add(builder.equal(root.get(SysSyncItemLog_.syncActionLog).get(AbstractEntity_.id), syncActionId));
		}

		// System ID
		UUID systemId = filter.getSystemId();
		if (systemId != null) {
			predicates.add(builder.equal(root.get(SysSyncItemLog_.syncActionLog).get(SysSyncActionLog_.syncLog)
					.get(SysSyncLog_.synchronizationConfig) //
					.get(SysSyncConfig_.systemMapping) //
					.get(SysSystemMapping_.objectClass) //
					.get(SysSchemaObjectClass_.system) //
					.get(AbstractEntity_.id), systemId));
		}

		// Modified from
		if (filter.getModifiedFrom() != null) {
			predicates.add(builder.greaterThanOrEqualTo(root.get(SysSyncItemLog_.modified), filter.getModifiedFrom()));
		}

		return predicates;
	}

}
