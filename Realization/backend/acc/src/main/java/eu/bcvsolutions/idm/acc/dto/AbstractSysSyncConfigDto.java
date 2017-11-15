package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import eu.bcvsolutions.idm.acc.domain.ReconciliationMissingAccountActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationLinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationMissingEntityActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationUnlinkedActionType;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.ic.domain.IcFilterOperationType;

/**
 * DTO for {@link SysSyncConfig}.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author svandav
 *
 */

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "_type")
@JsonSubTypes({
	@JsonSubTypes.Type(value = SysSyncConfigDto.class),
	@JsonSubTypes.Type(value = SysSyncContractConfigDto.class),
	@JsonSubTypes.Type(value = SysSyncIdentityConfigDto.class)
})
@Relation(collectionRelation = "synchronizationConfigs")
public abstract class AbstractSysSyncConfigDto extends AbstractDto {

	private static final long serialVersionUID = 7425419787855747415L;

	private boolean enabled = true;
	private String name;
	private String description;
	private boolean reconciliation = false;
	private boolean customFilter = false;
	private String token;
	private String customFilterScript;
	private String rootsFilterScript;
	@Embedded(dtoClass = SysSystemAttributeMappingDto.class)
	private UUID correlationAttribute;
	@Embedded(dtoClass = SysSystemAttributeMappingDto.class)
	private UUID tokenAttribute;
	@Embedded(dtoClass = SysSystemAttributeMappingDto.class)
	private UUID filterAttribute;
	private IcFilterOperationType filterOperation = IcFilterOperationType.GREATER_THAN;
	@Embedded(dtoClass = SysSystemMappingDto.class)
	private UUID systemMapping;
	private SynchronizationLinkedActionType linkedAction = SynchronizationLinkedActionType.UPDATE_ENTITY;
	private SynchronizationUnlinkedActionType unlinkedAction = SynchronizationUnlinkedActionType.LINK_AND_UPDATE_ACCOUNT;
	private SynchronizationMissingEntityActionType missingEntityAction = SynchronizationMissingEntityActionType.CREATE_ENTITY;
	private ReconciliationMissingAccountActionType missingAccountAction = ReconciliationMissingAccountActionType.IGNORE;
	private String linkedActionWfKey;
	private String unlinkedActionWfKey;
	private String missingEntityActionWfKey;
	private String missingAccountActionWfKey;

	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isReconciliation() {
		return reconciliation;
	}

	public void setReconciliation(boolean reconciliation) {
		this.reconciliation = reconciliation;
	}

	public boolean isCustomFilter() {
		return customFilter;
	}

	public void setCustomFilter(boolean customFilter) {
		this.customFilter = customFilter;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getCustomFilterScript() {
		return customFilterScript;
	}

	public void setCustomFilterScript(String customFilterScript) {
		this.customFilterScript = customFilterScript;
	}

	public String getRootsFilterScript() {
		return rootsFilterScript;
	}

	public void setRootsFilterScript(String rootsFilterScript) {
		this.rootsFilterScript = rootsFilterScript;
	}

	public UUID getCorrelationAttribute() {
		return correlationAttribute;
	}

	public void setCorrelationAttribute(UUID correlationAttribute) {
		this.correlationAttribute = correlationAttribute;
	}

	public UUID getTokenAttribute() {
		return tokenAttribute;
	}

	public void setTokenAttribute(UUID tokenAttribute) {
		this.tokenAttribute = tokenAttribute;
	}

	public UUID getFilterAttribute() {
		return filterAttribute;
	}

	public void setFilterAttribute(UUID filterAttribute) {
		this.filterAttribute = filterAttribute;
	}

	public IcFilterOperationType getFilterOperation() {
		return filterOperation;
	}

	public void setFilterOperation(IcFilterOperationType filterOperation) {
		this.filterOperation = filterOperation;
	}

	public UUID getSystemMapping() {
		return systemMapping;
	}

	public void setSystemMapping(UUID systemMapping) {
		this.systemMapping = systemMapping;
	}

	public SynchronizationLinkedActionType getLinkedAction() {
		return linkedAction;
	}

	public void setLinkedAction(SynchronizationLinkedActionType linkedAction) {
		this.linkedAction = linkedAction;
	}

	public SynchronizationUnlinkedActionType getUnlinkedAction() {
		return unlinkedAction;
	}

	public void setUnlinkedAction(SynchronizationUnlinkedActionType unlinkedAction) {
		this.unlinkedAction = unlinkedAction;
	}

	public SynchronizationMissingEntityActionType getMissingEntityAction() {
		return missingEntityAction;
	}

	public void setMissingEntityAction(SynchronizationMissingEntityActionType missingEntityAction) {
		this.missingEntityAction = missingEntityAction;
	}

	public ReconciliationMissingAccountActionType getMissingAccountAction() {
		return missingAccountAction;
	}

	public void setMissingAccountAction(ReconciliationMissingAccountActionType missingAccountAction) {
		this.missingAccountAction = missingAccountAction;
	}

	public String getLinkedActionWfKey() {
		return linkedActionWfKey;
	}

	public void setLinkedActionWfKey(String linkedActionWfKey) {
		this.linkedActionWfKey = linkedActionWfKey;
	}

	public String getUnlinkedActionWfKey() {
		return unlinkedActionWfKey;
	}

	public void setUnlinkedActionWfKey(String unlinkedActionWfKey) {
		this.unlinkedActionWfKey = unlinkedActionWfKey;
	}

	public String getMissingEntityActionWfKey() {
		return missingEntityActionWfKey;
	}

	public void setMissingEntityActionWfKey(String missingEntityActionWfKey) {
		this.missingEntityActionWfKey = missingEntityActionWfKey;
	}

	public String getMissingAccountActionWfKey() {
		return missingAccountActionWfKey;
	}

	public void setMissingAccountActionWfKey(String missingAccountActionWfKey) {
		this.missingAccountActionWfKey = missingAccountActionWfKey;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
