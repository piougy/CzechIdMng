package eu.bcvsolutions.idm.core.model.repository.listener;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.envers.EntityTrackingRevisionListener;
import org.hibernate.envers.RevisionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import eu.bcvsolutions.idm.core.api.dto.IdmAuditDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.entity.IdmAudit;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuditService;
import eu.bcvsolutions.idm.core.security.api.domain.AbstractAuthentication;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Creates records to global idm audit (searching through all entities, etc.)
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
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
		// nothing ... entityChanged is called instead this method
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void entityChanged(Class entityClass, String entityName, Serializable entityId, RevisionType revisionType,
			Object revisionEntity) {
		// autowire services
		autowireServices();

		if (((IdmAudit) revisionEntity).getEntityId() != null) { // child revision
			IdmAuditDto childRevision = new IdmAuditDto();
			childRevision.setTimestamp(((IdmAudit) revisionEntity).getTimestamp());
			this.changeRevisionDto((Class<AbstractEntity>) entityClass, entityName, (UUID) entityId, childRevision,
					revisionType);
			this.auditService.save(childRevision);
		} else { // parent revision
			this.changeRevisionEntity((Class<AbstractEntity>) entityClass, entityName, (UUID) entityId,
					((IdmAudit) revisionEntity), revisionType);
		}
	}

	private void changeRevisionDto(Class<AbstractEntity> entityClass, String entityName, UUID entityId,
		IdmAuditDto revisionEntity, RevisionType revisionType) {
		// List<String> changedColumns;

		// name of entity class - full name.
		revisionEntity.setType(entityName);
		// revision type - MOD, DEL, ADD
		revisionEntity.setModification(revisionType.name());
		// action executer identity
		AbstractAuthentication authentication = securityService.getAuthentication();
		IdmIdentityDto currentModifierIdentity = authentication == null ? null : authentication.getCurrentIdentity();
		IdmIdentityDto originalModifierIdentity = authentication == null ? null : authentication.getOriginalIdentity();
		//
		revisionEntity.setModifier(securityService.getUsername());
		revisionEntity.setModifierId(currentModifierIdentity == null ? null : currentModifierIdentity.getId());
		// original action executer identity (before switch)
		revisionEntity.setOriginalModifier(securityService.getOriginalUsername());
		revisionEntity.setOriginalModifierId(originalModifierIdentity == null ? null : originalModifierIdentity.getId());
		// entity id
		revisionEntity.setEntityId((UUID) entityId);

		// if revision type is MOD - modification, get and set changed columns
		// Problem with two changes for one entity in one transaction
		// envers not found entity snapshot for previous version - this version
		// is not committed
		// try to fix this problem with getting this entity from session?
		// if (revisionType == RevisionType.MOD) {
		// AbstractEntity currentEntity = (AbstractEntity)
		// entityManger.find(entityClass, entityId);
		// changedColumns = auditService.getNameChangedColumns(entityClass,
		// entityId, null, currentEntity);
		// revisionEntity.addChanged(changedColumns);
		// }
	}
	
	private void changeRevisionEntity(Class<AbstractEntity> entityClass, String entityName, UUID entityId,
		IdmAudit revisionEntity, RevisionType revisionType) {
		// name of entity class - full name.
		revisionEntity.setType(entityName);
		// revision type - MOD, DEL, ADD
		revisionEntity.setModification(revisionType.name());
		// action executer identity
		AbstractAuthentication authentication = securityService.getAuthentication();
		IdmIdentityDto currentIdentity = authentication == null ? null : authentication.getCurrentIdentity();
		IdmIdentityDto originalIdentity = authentication == null ? null : authentication.getOriginalIdentity();
		//
		revisionEntity.setModifier(securityService.getUsername());
		revisionEntity.setModifierId(currentIdentity == null ? null : currentIdentity.getId());
		// original action executer identity (before switch)
		revisionEntity.setOriginalModifier(securityService.getOriginalUsername());
		revisionEntity.setOriginalModifierId(originalIdentity == null ? null : originalIdentity.getId());
		// entity id
		revisionEntity.setEntityId((UUID) entityId);
	}

	/**
	 * Method autowire audit service to this listener.
	 * 
	 */
	private void autowireServices() {
		AutowireHelper.autowire(this, this.auditService, this.entityManger, this.securityService);
	}

}
