package eu.bcvsolutions.idm.core.config.domain;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.envers.EntityTrackingRevisionListener;
import org.hibernate.envers.RevisionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import eu.bcvsolutions.idm.core.api.AutowireHelper;
import eu.bcvsolutions.idm.core.model.entity.IdmAudit;
import eu.bcvsolutions.idm.core.model.service.IdmAuditService;

@Configurable
public class IdmAuditListener implements EntityTrackingRevisionListener {
	
	@Autowired
	private IdmAuditService auditService;
	
	@Autowired
	private EntityManager entityManger;
	
	
	@Override
	public void newRevision(Object revisionEntity) {
		// nothing ...
	}
	
	// TODO: suppressWarnings is necessary?
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void entityChanged(Class entityClass, String entityName, Serializable entityId, RevisionType revisionType,
			Object revisionEntity) {
		List<String> changedColumns;
		// if revision type is MOD - modification, get and set changed columns
		// TODO: GUID? revision id?
		if (revisionType == RevisionType.MOD) {
			autowireServices();
			changedColumns = auditService.getNameChangedColumns(entityClass, (long)entityId, ((IdmAudit)revisionEntity).getId(), entityManger.find(entityClass, (long)entityId));
			((IdmAudit)revisionEntity).addChanged(changedColumns);
		}
		
        ((IdmAudit)revisionEntity).setModification(revisionType.name());
        ((IdmAudit)revisionEntity).setType(entityName);
        
	}
	
	/**
	 * Method autowire audit service to this listener.
	 * 
	 */
	private void autowireServices() {
		if (this.auditService == null) {
			AutowireHelper.autowire(this, this.auditService);
		}
		if (this.entityManger == null) {
			AutowireHelper.autowire(this, this.auditService);
		}
	}

}
