package eu.bcvsolutions.idm.core.config.domain;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.envers.EntityTrackingRevisionListener;
import org.hibernate.envers.RevisionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.AutowireHelper;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmAudit;
import eu.bcvsolutions.idm.core.model.service.IdmAuditService;
import eu.bcvsolutions.idm.security.api.service.SecurityService;

@Configurable
public class IdmAuditListener implements EntityTrackingRevisionListener {
	
	@Autowired
	private IdmAuditService auditService;
	
	@Autowired
	private SecurityService securityService;
	
	@PersistenceContext
	private EntityManager entityManger;
	
	
	@Override
	public void newRevision(Object revisionEntity) {
		// nothing ...
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	@Transactional
	public void entityChanged(Class entityClass, String entityName, Serializable entityId, RevisionType revisionType,
			Object revisionEntity) {
		
		List<String> changedColumns;
		AbstractEntity currentEntity = null;
		
		// autowire services
		autowireServices();
		
		// if revision type is MOD - modification, get and set changed columns
		if (revisionType == RevisionType.MOD) {
			currentEntity = (AbstractEntity) entityManger.find(entityClass, (long)entityId);
			changedColumns = auditService.getNameChangedColumns(entityClass, (long)entityId, ((IdmAudit)revisionEntity).getId(), currentEntity);
			((IdmAudit)revisionEntity).addChanged(changedColumns);
		}
		
        // name of entity class - full name. 
        ((IdmAudit)revisionEntity).setType(entityName);
        // revision type - MOD, DEL, ADD
        ((IdmAudit)revisionEntity).setModification(revisionType.name());
        // actual modifier
     	((IdmAudit)revisionEntity).setModifier(securityService.getUsername());
     	// original modifier before switch
     	((IdmAudit)revisionEntity).setOriginalModifier(securityService.getOriginalUsername());
     	// entity id TODO: link from collection to master.
     	((IdmAudit)revisionEntity).setEntityId((long)entityId);
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
		if (this.securityService == null) {
			AutowireHelper.autowire(this, this.securityService);
		}
	}

}
