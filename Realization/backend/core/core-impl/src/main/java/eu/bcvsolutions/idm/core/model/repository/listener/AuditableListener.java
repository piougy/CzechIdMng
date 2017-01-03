package eu.bcvsolutions.idm.core.model.repository.listener;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.AbstractPreDatabaseOperationEvent;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.Auditable;
import eu.bcvsolutions.idm.core.api.dto.IdentityDto;
import eu.bcvsolutions.idm.core.api.repository.listener.AuditableEntityListener;
import eu.bcvsolutions.idm.security.api.domain.AbstractAuthentication;
import eu.bcvsolutions.idm.security.api.service.SecurityService;

/**
 * Sets auditable properties
 * 
 * @author Radek Tomiška
 *
 * @Deprecated see {@link AuditableEntityListener}
 */
@Deprecated
public class AuditableListener implements PreInsertEventListener, PreUpdateEventListener {

	private static final long serialVersionUID = 6634361715588505690L;
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AuditableListener.class);

	@Autowired
	private HibernateEntityManagerFactory entityManagerFactory;

	@Autowired
	private transient SecurityService securityService;

	@Override
	public boolean onPreInsert(PreInsertEvent event) {
		if (event.getEntity() instanceof Auditable) {
			DateTime date = new DateTime();
			Auditable entity = (Auditable) event.getEntity();
			//
			setValue(event.getState(), event, Auditable.PROPERTY_CREATED, date);
			entity.setCreated(date);
			//
			AbstractAuthentication authentication = securityService.getAuthentication();
			IdentityDto currentIdentity = authentication == null ? null : authentication.getCurrentIdentity();
			IdentityDto originalIdentity = authentication == null ? null : authentication.getOriginalIdentity();
			if (entity.getCreator() == null) {
				String creator = currentIdentity == null ? securityService.getUsername()
						: currentIdentity.getUsername();
				setValue(event.getState(), event, Auditable.PROPERTY_CREATOR, creator);
				entity.setCreator(creator);
				//
				UUID creatorId = currentIdentity == null ? null : currentIdentity.getId();
				setValue(event.getState(), event, Auditable.PROPERTY_CREATOR_ID, creatorId);
				entity.setCreatorId(creatorId);
			}
			// could be filled in wf (applicant) ...
			if (entity.getOriginalCreator() == null) {
				String originalCreator = originalIdentity == null ? null : originalIdentity.getUsername();
				setValue(event.getState(), event, Auditable.PROPERTY_ORIGINAL_CREATOR, originalCreator);
				entity.setOriginalCreator(originalCreator);
				//
				UUID originalCreatorId = originalIdentity == null ? null : originalIdentity.getId();
				setValue(event.getState(), event, Auditable.PROPERTY_ORIGINAL_CREATOR_ID, originalCreatorId);
				entity.setOriginalCreatorId(originalCreatorId);
			}
		}
		return false;
	}

	@Override
	public boolean onPreUpdate(PreUpdateEvent event) {
		if (event.getEntity() instanceof Auditable) {
			DateTime date = new DateTime();
			Auditable entity = (Auditable) event.getEntity();
			//
			setValue(event.getState(), event, Auditable.PROPERTY_MODIFIED, date);
			entity.setModified(date);
			//
			AbstractAuthentication authentication = securityService.getAuthentication();
			//
			IdentityDto currentIdentity = authentication == null ? null : authentication.getCurrentIdentity();
			IdentityDto originalIdentity = authentication == null ? null : authentication.getOriginalIdentity();
			//
			String modifier = currentIdentity == null ? securityService.getUsername() : currentIdentity.getUsername();
			setValue(event.getState(), event, Auditable.PROPERTY_MODIFIER, modifier);
			entity.setModifier(modifier);
			//
			UUID modifierId = currentIdentity == null ? null : currentIdentity.getId();
			setValue(event.getState(), event, Auditable.PROPERTY_MODIFIER_ID, modifierId);
			entity.setModifierId(modifierId);
			//
			// could be filled in wf (applicant) ...
			if (entity.getOriginalModifier() == null) {
				String originalModifier = originalIdentity == null ? null : originalIdentity.getUsername();
				setValue(event.getState(), event, Auditable.PROPERTY_ORIGINAL_MODIFIER, originalModifier);
				entity.setOriginalModifier(originalModifier);
				//
				UUID originalModifierId = originalIdentity == null ? null : originalIdentity.getId();
				setValue(event.getState(), event, Auditable.PROPERTY_ORIGINAL_MODIFIER_ID, originalModifierId);
				entity.setOriginalModifierId(originalModifierId);
			}
		}
		return false;
	}

	/**
	 * Set parameters for insert / update query
	 *
	 * @param currentState
	 * @param event
	 * @param propertyToSet
	 * @param value
	 */
	private void setValue(Object[] currentState, AbstractPreDatabaseOperationEvent event, String propertyToSet,
			Object value) {
		String[] propertyNames = event.getPersister().getPropertyNames();
		int index = ArrayUtils.indexOf(propertyNames, propertyToSet);
		if (index >= 0) {
			currentState[index] = value;
		} else {
			LOG.error("Field [{}] not found on entity [{}].", propertyToSet, event.getEntity().getClass());
		}
	}

	/**
	 * Register listener to hibernate
	 */
	@PostConstruct
	public void register() {
		LOG.debug("Registering auditor listener [{}]", AuditableListener.class.getSimpleName());

		SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) entityManagerFactory.getSessionFactory();
		EventListenerRegistry registry = sessionFactoryImpl.getServiceRegistry()
				.getService(EventListenerRegistry.class);
		registry.getEventListenerGroup(EventType.PRE_INSERT).appendListener(this);
		registry.getEventListenerGroup(EventType.PRE_UPDATE).appendListener(this);

		LOG.debug("Registered auditor listener [{}]", AuditableListener.class.getSimpleName());
	}
}
