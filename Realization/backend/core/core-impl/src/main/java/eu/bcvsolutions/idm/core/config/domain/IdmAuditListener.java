package eu.bcvsolutions.idm.core.config.domain;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.envers.EntityTrackingRevisionListener;
import org.hibernate.envers.RevisionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import eu.bcvsolutions.idm.core.api.AutowireHelper;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmAudit;
import eu.bcvsolutions.idm.core.model.service.IdmAuditService;
import eu.bcvsolutions.idm.security.api.service.SecurityService;

@Configurable
public class IdmAuditListener implements EntityTrackingRevisionListener {
	
	private static final Long FIRST_REVISION = 1l; 
	
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
	public void entityChanged(Class entityClass, String entityName, Serializable entityId, RevisionType revisionType,
			Object revisionEntity) {
		
		List<String> changedColumns;
		AbstractEntity currentEntity = null;
		
		// autowire services
		autowireServices();
		
		// set revision number, AbstractEntity dont have implement version number, then we must get last revision number
		Number revisionNumber = this.auditService.getLastVersionNumber(entityClass, (UUID)entityId);
		if (revisionNumber == null) {
			((IdmAudit)revisionEntity).setRevisionNumber(FIRST_REVISION);
		} else {
			long number = revisionNumber.longValue();
			((IdmAudit)revisionEntity).setRevisionNumber(number++);
		}
		
		// if revision type is MOD - modification, get and set changed columns
		if (revisionType == RevisionType.MOD) {
			currentEntity = (AbstractEntity) entityManger.find(entityClass, (UUID)entityId);
			changedColumns = auditService.getNameChangedColumns(entityClass, (UUID)entityId, ((IdmAudit)revisionEntity).getRevisionId(), currentEntity);
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
     	((IdmAudit)revisionEntity).setEntityId((UUID)entityId);
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
