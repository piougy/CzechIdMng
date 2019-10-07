package eu.bcvsolutions.idm.core.api.repository.listener;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import eu.bcvsolutions.idm.core.api.domain.Auditable;
import eu.bcvsolutions.idm.core.api.domain.TransactionContextHolder;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.security.api.domain.AbstractAuthentication;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Sets {@link Auditable} properties.
 * 
 * @author Radek Tomiška
 * @see {@link AuditingEntityListener}
 */
@Configurable
public class AuditableEntityListener {

	@Autowired
	private SecurityService securityService;
	
	/**
	 * Sets creation date and creator on the target object in case it implements {@link Auditable} on
	 * persist events.
	 * 
	 * @param target
	 */
	@PrePersist
	public void touchForCreate(Object target) {
		if (!(target instanceof Auditable)) {
			return;
		}
		//
		AutowireHelper.autowire(this, this.securityService);
		//
		ZonedDateTime date = ZonedDateTime.now();
		Auditable entity = (Auditable) target;
		// set created date - can be preset (e.g. preserve created date of original operation moved some where info archive)
		if (entity.getCreated() == null) {
			entity.setCreated(date);
		}
		//
		AbstractAuthentication authentication = securityService.getAuthentication();
		IdmIdentityDto currentIdentity = authentication == null ? null : authentication.getCurrentIdentity();
		IdmIdentityDto originalIdentity = authentication == null ? null : authentication.getOriginalIdentity();
		if (entity.getCreator() == null) {
			String creator = currentIdentity == null ? securityService.getUsername() : currentIdentity.getUsername();
			entity.setCreator(creator);
			//
			UUID creatorId = currentIdentity == null ? null : currentIdentity.getId();
			entity.setCreatorId(creatorId);
		}
		// could be filled in wf (applicant) ...
		if (entity.getOriginalCreator() == null) {
			String originalCreator = originalIdentity == null ? null : originalIdentity.getUsername();
			entity.setOriginalCreator(originalCreator);
			//
			UUID originalCreatorId = originalIdentity == null ? null : originalIdentity.getId();
			entity.setOriginalCreatorId(originalCreatorId);
		}
		// set transaction id from context holder
		entity.setTransactionId(TransactionContextHolder.getContext().getTransactionId());
	}

	/**
	 * Sets modification date and modifier on the target object in case it implements {@link Auditable} on
	 * update events.
	 * 
	 * @param target
	 */
	@PreUpdate
	public void touchForUpdate(Object target) {
		if (!(target instanceof Auditable)) {
			return;
		}
		//
		AutowireHelper.autowire(this, this.securityService);
		//
		ZonedDateTime date = ZonedDateTime.now();
		Auditable entity = (Auditable) target;
		//
		entity.setModified(date);
		//
		AbstractAuthentication authentication = securityService.getAuthentication();
		//
		IdmIdentityDto currentIdentity = authentication == null ? null : authentication.getCurrentIdentity();
		IdmIdentityDto originalIdentity = authentication == null ? null : authentication.getOriginalIdentity();
		//
		String modifier = currentIdentity == null ? securityService.getUsername() : currentIdentity.getUsername();
		entity.setModifier(modifier);
		//
		UUID modifierId = currentIdentity == null ? null : currentIdentity.getId();
		entity.setModifierId(modifierId);
		//
		// could be filled in wf (applicant) ...
		if (entity.getOriginalModifier() == null) {
			String originalModifier = originalIdentity == null ? null : originalIdentity.getUsername();
			entity.setOriginalModifier(originalModifier);
			//
			UUID originalModifierId = originalIdentity == null ? null : originalIdentity.getId();
			entity.setOriginalModifierId(originalModifierId);
		}
		// set transaction id from context holder
		entity.setTransactionId(TransactionContextHolder.getContext().getTransactionId());
	}
}