package eu.bcvsolutions.idm.core.api.repository.generator;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.UUIDGenerator;

/**
 * Preserve given uuid, if was given. Otherwise generate new uuid.
 * 
 * @author Radek Tomiška
 *
 */
public class PreserveUuidGenerator extends UUIDGenerator {

	/**
	 * 
	 * Preserve given uuid, if was given. Otherwise generate new uuid.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public Serializable generate(SessionImplementor session, Object object) throws HibernateException {
		Serializable id = session.getEntityPersister(null, object).getClassMetadata().getIdentifier(object, session);
		return id != null ? id : super.generate(session, object);
	}
}
