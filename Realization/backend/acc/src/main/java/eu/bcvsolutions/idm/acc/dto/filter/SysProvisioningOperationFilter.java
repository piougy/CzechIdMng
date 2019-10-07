package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import java.time.ZonedDateTime;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Provisioning operation filter
 * 
 * @author Radek Tomiška
 *
 */
public class SysProvisioningOperationFilter extends DataFilter {

	public static final String PARAMETER_FROM = "from"; // created from
	public static final String PARAMETER_TILL = "till"; // created till
	public static final String PARAMETER_SYSTEM_ID = "systemId";
	public static final String PARAMETER_OPERATION_TYPE = "operationType";
	public static final String PARAMETER_ENTITY_TYPE = "entityType";
	public static final String PARAMETER_RESULT_STATE = "resultState";
	public static final String PARAMETER_ENTITY_IDENTIFIER = "entityIdentifier";
	public static final String PARAMETER_SYSTEM_ENTITY_ID = "systemEntity";
	public static final String PARAMETER_SYSTEM_ENTITY_UID = "systemEntityUid";
	public static final String PARAMETER_BATCH_ID = "batchId";
	public static final String PARAMETER_ATTRIBUTE_UPDATED = "attributeUpdated"; // list - OR
	public static final String PARAMETER_ATTRIBUTE_REMOVED = "attributeRemoved"; // list - OR
	public static final String PARAMETER_EMPTY_PROVISIONING = "emptyProvisioning"; // provisioning attributes are empty
	public static final String PARAMETER_ROLE_REQUEST_ID = "roleRequestId";
	public static final String PARAMETER_NOT_IN_STATE = "notInState";

	public SysProvisioningOperationFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public SysProvisioningOperationFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public SysProvisioningOperationFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(null, data, parameterConverter); // FIXME: filter used for two DTOs - operation and archive
	}

	public ZonedDateTime getFrom() {
		return getParameterConverter().toDateTime(data, PARAMETER_FROM);
	}

	public void setFrom(ZonedDateTime from) {
		data.set(PARAMETER_FROM, from);
	}

	public ZonedDateTime getTill() {
		return getParameterConverter().toDateTime(data, PARAMETER_TILL);
	}

	public void setTill(ZonedDateTime till) {
		data.set(PARAMETER_TILL, till);
	}

	public UUID getSystemId() {
		return getParameterConverter().toUuid(data, PARAMETER_SYSTEM_ID);
	}

	public void setSystemId(UUID systemId) {
		data.set(PARAMETER_SYSTEM_ID, systemId);
	}

	public ProvisioningEventType getOperationType() {
		return getParameterConverter().toEnum(data, PARAMETER_OPERATION_TYPE, ProvisioningEventType.class);
	}

	public void setOperationType(ProvisioningEventType operationType) {
		data.set(PARAMETER_OPERATION_TYPE, operationType);
	}

	public SystemEntityType getEntityType() {
		return getParameterConverter().toEnum(data, PARAMETER_ENTITY_TYPE, SystemEntityType.class);
	}

	public void setEntityType(SystemEntityType entityType) {
		data.set(PARAMETER_ENTITY_TYPE, entityType);
	}

	public OperationState getResultState() {
		return getParameterConverter().toEnum(data, PARAMETER_RESULT_STATE, OperationState.class);
	}
	

	public void setResultState(OperationState resultState) {
		data.set(PARAMETER_RESULT_STATE, resultState);
	}

	public OperationState getNotInState() {
		return getParameterConverter().toEnum(data, PARAMETER_NOT_IN_STATE, OperationState.class);
	}
	
	public void setNotInState(OperationState resultState) {
		data.set(PARAMETER_NOT_IN_STATE, resultState);
	}
	
	public UUID getEntityIdentifier() {
		try {
			return getParameterConverter().toUuid(data, PARAMETER_ENTITY_IDENTIFIER);
		} catch (ClassCastException ex) {
			throw new ResultCodeException(CoreResultCode.BAD_FILTER, ex);
		}
	}

	public void setEntityIdentifier(UUID entityIdentifier) {
		data.set(PARAMETER_ENTITY_IDENTIFIER, entityIdentifier);
	}
	
	public UUID getSystemEntity() {
		return getParameterConverter().toUuid(data, PARAMETER_SYSTEM_ENTITY_ID);
	}

	public void setSystemEntity(UUID systemEntity) {
		data.set(PARAMETER_SYSTEM_ENTITY_ID, systemEntity);
	}

	public void setBatchId(UUID batchId) {
		data.set(PARAMETER_BATCH_ID, batchId);
	}
	
	public UUID getBatchId() {
		return getParameterConverter().toUuid(data, PARAMETER_BATCH_ID);
	}

	public String getSystemEntityUid() {
		return getParameterConverter().toString(data, PARAMETER_SYSTEM_ENTITY_UID);
	}

	public void setSystemEntityUid(String systemEntityUid) {
		data.set(PARAMETER_SYSTEM_ENTITY_UID, systemEntityUid);
	}
	
	/**
	 * Updated (or created) attributes in provisioning context connector attributes.
	 * 
	 * @return
	 * @since 9.6.3
	 */
	public List<String> getAttributeUpdated() {
		return getParameterConverter().toStrings(data, PARAMETER_ATTRIBUTE_UPDATED);
	}
	
	/**
	 * Updated (or created) attributes in provisioning context connector attributes.
	 * 
	 * @param attributeUpdated
	 * @since 9.6.3
	 */
	public void setAttributeUpdated(List<String> attributeUpdated) {
		data.put(PARAMETER_ATTRIBUTE_UPDATED, attributeUpdated == null ? null : new ArrayList<Object>(attributeUpdated));
	}

	/**
	 * Removed attributes in provisioning context connector attributes (null or empty values send in connector attributes).
	 * 
	 * @return
	 * @since 9.6.3
	 */
	public List<String> getAttributeRemoved() {
		return getParameterConverter().toStrings(data, PARAMETER_ATTRIBUTE_REMOVED);
	}

	/**
	 * Removed attributes in provisioning context connector attributes (null or empty values send in connector attributes).
	 * 
	 * @param attributeRemoved
	 * @since 9.6.3
	 */
	public void setAttributeRemoved(List<String> attributeRemoved) {
		data.put(PARAMETER_ATTRIBUTE_REMOVED, attributeRemoved == null ? null : new ArrayList<Object>(attributeRemoved));
	}
	
	/**
	 * Provisioning context with connector attributes is empty.
	 * 
	 * @return
	 * @since 9.6.3
	 */
	public Boolean getEmptyProvisioning() {
		return getParameterConverter().toBoolean(data, PARAMETER_EMPTY_PROVISIONING);
	}
	
	/**
	 * Provisioning context with connector attributes is empty.
	 * 
	 * @param emptyProvisioning
	 * @since 9.6.3
	 */
	public void setEmptyProvisioning(Boolean emptyProvisioning) {
		data.set(PARAMETER_EMPTY_PROVISIONING, emptyProvisioning);
	}
	
	public UUID getRoleRequestId() {
		return getParameterConverter().toUuid(data, PARAMETER_ROLE_REQUEST_ID);
	}

	public void setRoleRequestId(UUID roleRequestId) {
		data.set(PARAMETER_ROLE_REQUEST_ID, roleRequestId);
	}
}
