package eu.bcvsolutions.idm.core.api.exception;

import java.util.Map;
import java.util.UUID;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.ResultCode;
import eu.bcvsolutions.idm.core.ecm.api.entity.AttachableEntity;

/**
 * Operation content was deleted in the meantime.
 * 
 * @author Radek Tomiška
 * @since 10.4.0
 */
public class ContentDeletedException extends ResultCodeException {
	
	private static final long serialVersionUID = 1L;
	
	public ContentDeletedException(ResultCode resultCode, Map<String, Object> parameters) {
		super(new DefaultErrorModel(resultCode, parameters));
	}
	
	public ContentDeletedException(UUID ownerId, String ownerType) {
		this(CoreResultCode.CONTENT_DELETED, ImmutableMap.of(
				AttachableEntity.PARAMETER_OWNER_ID, ownerId,
				AttachableEntity.PARAMETER_OWNER_TYPE, ownerType));
	}
}
