package eu.bcvsolutions.idm.core.ecm.api.entity;

import java.nio.charset.Charset;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Entity can have attachments
 * 
 * @author Radek Tomiška
 * @since 7.6.0
 */
public interface AttachableEntity extends BaseEntity {

	static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
	static final String DEFAULT_ENCODING = DEFAULT_CHARSET.name();
	
	/**
	 * Entity with {@link UUID} identifier only
	 */
	@Override
	UUID getId();
}
