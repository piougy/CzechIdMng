package eu.bcvsolutions.idm.acc.dto.filter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.acc.domain.EmptyProvisioningType;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
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
	public static final String PARAMETER_EMPTY_PROVISIONING_TYPE = "emptyProvisioningType"; // provisioning attributes are empty / non empty / not computed
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
		return getParameterConverter().toDateTime(getData(), PARAMETER_FROM);
	}

	public void setFrom(ZonedDateTime from) {
		set(PARAMETER_FROM, from);
	}

	public ZonedDateTime getTill() {
		return getParameterConverter().toDateTime(getData(), PARAMETER_TILL);
	}

	public void setTill(ZonedDateTime till) {
		set(PARAMETER_TILL, till);
	}

	public UUID getSystemId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_SYSTEM_ID);
	}

	public void setSystemId(UUID systemId) {
		set(PARAMETER_SYSTEM_ID, systemId);
	}
	
	public ProvisioningEventType getOperationType() {
		return getParameterConverter().toEnum(getData(), PARAMETER_OPERATION_TYPE, ProvisioningEventType.class);
	}

	public void setOperationType(ProvisioningEventType operationType) {
		set(PARAMETER_OPERATION_TYPE, operationType);
	}

	public SystemEntityType getEntityType() {
		return getParameterConverter().toEnum(getData(), PARAMETER_ENTITY_TYPE, SystemEntityType.class);
	}

	public void setEntityType(SystemEntityType entityType) {
		set(PARAMETER_ENTITY_TYPE, entityType);
	}

	public OperationState getResultState() {
		return getParameterConverter().toEnum(getData(), PARAMETER_RESULT_STATE, OperationState.class);
	}
	

	public void setResultState(OperationState resultState) {
		set(PARAMETER_RESULT_STATE, resultState);
	}

	public OperationState getNotInState() {
		return getParameterConverter().toEnum(getData(), PARAMETER_NOT_IN_STATE, OperationState.class);
	}
	
	public void setNotInState(OperationState resultState) {
		set(PARAMETER_NOT_IN_STATE, resultState);
	}
	
	public UUID getEntityIdentifier() {
		return getParameterConverter().toUuid(getData(), PARAMETER_ENTITY_IDENTIFIER);
	}

	public void setEntityIdentifier(UUID entityIdentifier) {
		set(PARAMETER_ENTITY_IDENTIFIER, entityIdentifier);
	}
	
	public UUID getSystemEntity() {
		return getParameterConverter().toUuid(getData(), PARAMETER_SYSTEM_ENTITY_ID);
	}

	public void setSystemEntity(UUID systemEntity) {
		set(PARAMETER_SYSTEM_ENTITY_ID, systemEntity);
	}

	public void setBatchId(UUID batchId) {
		set(PARAMETER_BATCH_ID, batchId);
	}
	
	public UUID getBatchId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_BATCH_ID);
	}

	public String getSystemEntityUid() {
		return getParameterConverter().toString(getData(), PARAMETER_SYSTEM_ENTITY_UID);
	}

	public void setSystemEntityUid(String systemEntityUid) {
		set(PARAMETER_SYSTEM_ENTITY_UID, systemEntityUid);
	}
	
	/**
	 * Updated (or created) attributes in provisioning context connector attributes.
	 * 
	 * @return
	 * @since 9.6.3
	 */
	public List<String> getAttributeUpdated() {
		return getParameterConverter().toStrings(getData(), PARAMETER_ATTRIBUTE_UPDATED);
	}
	
	/**
	 * Updated (or created) attributes in provisioning context connector attributes.
	 * 
	 * @param attributeUpdated
	 * @since 9.6.3
	 */
	public void setAttributeUpdated(List<String> attributeUpdated) {
		put(PARAMETER_ATTRIBUTE_UPDATED, attributeUpdated);
	}

	/**
	 * Removed attributes in provisioning context connector attributes (null or empty values send in connector attributes).
	 * 
	 * @return
	 * @since 9.6.3
	 */
	public List<String> getAttributeRemoved() {
		return getParameterConverter().toStrings(getData(), PARAMETER_ATTRIBUTE_REMOVED);
	}

	/**
	 * Removed attributes in provisioning context connector attributes (null or empty values send in connector attributes).
	 * 
	 * @param attributeRemoved
	 * @since 9.6.3
	 */
	public void setAttributeRemoved(List<String> attributeRemoved) {
		put(PARAMETER_ATTRIBUTE_REMOVED, attributeRemoved);
	}
	
	/**
	 * Provisioning context with connector attributes is empty.
	 * 
	 * @return
	 * @since 9.6.3
	 */
	public Boolean getEmptyProvisioning() {
		return getParameterConverter().toBoolean(getData(), PARAMETER_EMPTY_PROVISIONING);
	}
	
	/**
	 * Provisioning context with connector attributes is empty.
	 * 
	 * @param emptyProvisioning
	 * @since 9.6.3
	 */
	public void setEmptyProvisioning(Boolean emptyProvisioning) {
		set(PARAMETER_EMPTY_PROVISIONING, emptyProvisioning);
	}
	
	/**
	 * Provisioning context with connector attributes is empty / non empty / not computed.
	 * 
	 * @return type
	 * @since 10.6.0
	 */
	public EmptyProvisioningType getEmptyProvisioningType() {
		return getParameterConverter().toEnum(getData(), PARAMETER_EMPTY_PROVISIONING_TYPE, EmptyProvisioningType.class);
	}
	
	/**
	 * Provisioning context with connector attributes is empty / non empty / not computed.
	 * 
	 * @param emptyProvisioningType
	 * @since 10.6.0
	 */
	public void setEmptyProvisioningType(EmptyProvisioningType emptyProvisioningType) {
		set(PARAMETER_EMPTY_PROVISIONING_TYPE, emptyProvisioningType);
	}
	
	public UUID getRoleRequestId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_ROLE_REQUEST_ID);
	}

	public void setRoleRequestId(UUID roleRequestId) {
		set(PARAMETER_ROLE_REQUEST_ID, roleRequestId);
	}
}
