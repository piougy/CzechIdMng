package eu.bcvsolutions.idm.acc.dto;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import org.springframework.hateoas.core.Relation;

/**
 * Role sync configuration DTO
 *
 * @author Vít Švanda
 * @since 11.1.0
 */
@Relation(collectionRelation = "synchronizationConfigs")
public class SysSyncRoleConfigDto extends AbstractSysSyncConfigDto {

	private static final long serialVersionUID = 1L;

	/**
	 * Switch for activate forward an account management feature. Every role, which manages role membership on the connected system, has the option forward account management.
	 */
	@NotNull
	private boolean forwardAcmSwitch = false;
	/**
	 * Is filled only if exists mapping attribute for 'forward ACM'.
	 */
	@Embedded(dtoClass = SysSystemAttributeMappingDto.class)
	private UUID forwardAcmMappingAttribute;
	/**
	 * Switch for activate skip value if is contract excluded feature.
	 */
	@NotNull
	private boolean skipValueIfExcludedSwitch = false;
	/**
	 * Is filled only if exists mapping attribute for 'skipValueIfExcluded'.
	 */
	@Embedded(dtoClass = SysSystemAttributeMappingDto.class)
	private UUID skipValueIfExcludedMappingAttribute;
	/**
	 * Switch for activate a membership feature.
	 */
	@NotNull
	private boolean membershipSwitch = false;
	/**
	 * Is filled only if exists mapping attribute for 'roleIdentifiers'.
	 */
	@Embedded(dtoClass = SysSystemAttributeMappingDto.class)
	private UUID roleIdentifiersMappingAttribute;
	/**
	 * Switch for activate an assign role by external system feature.
	 */
	@NotNull
	private boolean assignRoleSwitch = false;
	/**
	 * Is filled only if exists mapping attribute for 'roleMembers'.
	 */
	@Embedded(dtoClass = SysSystemAttributeMappingDto.class)
	private UUID roleMembersMappingAttribute;
	/**
	 * Switch for activate 'an assign role to a catalogue' feature.
	 */
	@NotNull
	private boolean assignCatalogueSwitch = false;
	/**
	 * Is filled only if exists mapping attribute for 'assignCatalogue'.
	 */
	@Embedded(dtoClass = SysSystemAttributeMappingDto.class)
	private UUID assignCatalogueMappingAttribute;
	
	/**
	 * Switch for activate removing role from a catalogue.
	 */
	@NotNull
	private boolean removeCatalogueRoleSwitch = false;

	/**
	 * Role from a catalogue will be removed only under this node.
	 */
	@Embedded(dtoClass = IdmRoleCatalogueDto.class)
	private UUID removeCatalogueRoleParentNode;

	/**
	 * Role catalogues will be created under this node.
	 */
	@Embedded(dtoClass = IdmRoleCatalogueDto.class)
	private UUID mainCatalogueRoleNode;
	
	/**
	 * Switch for activate feature for remove assigned role missing in external system.
	 */
	@NotNull
	private boolean assignRoleRemoveSwitch = false;
	/**
	 * On system with identities. Provisioning mapping of a system with members (identities) -> it is a different system than this one.
	 */
	@Embedded(dtoClass = SysSystemMappingDto.class)
	private UUID memberSystemMapping;
	/**
	 * Attribute on system with identities. Multivalued attribute with all role identifiers where identity is member of roles. 
	 */
	@Embedded(dtoClass = SysSystemAttributeMappingDto.class)
	private UUID memberOfAttribute;
	/**
	 * Member's attribute with identifier (DN).
	 */
	@Embedded(dtoClass = SysSchemaAttributeDto.class)
	private UUID memberIdentifierAttribute;

	public boolean isRemoveCatalogueRoleSwitch() {
		return removeCatalogueRoleSwitch;
	}

	public void setRemoveCatalogueRoleSwitch(boolean removeCatalogueRoleSwitch) {
		this.removeCatalogueRoleSwitch = removeCatalogueRoleSwitch;
	}

	public UUID getRemoveCatalogueRoleParentNode() {
		return removeCatalogueRoleParentNode;
	}

	public void setRemoveCatalogueRoleParentNode(UUID removeCatalogueRoleParentNode) {
		this.removeCatalogueRoleParentNode = removeCatalogueRoleParentNode;
	}

	public UUID getMainCatalogueRoleNode() {
		return mainCatalogueRoleNode;
	}

	public void setMainCatalogueRoleNode(UUID mainCatalogueRoleNode) {
		this.mainCatalogueRoleNode = mainCatalogueRoleNode;
	}

	public UUID getMemberSystemMapping() {
		return memberSystemMapping;
	}

	public void setMemberSystemMapping(UUID memberSystemMapping) {
		this.memberSystemMapping = memberSystemMapping;
	}

	public UUID getMemberIdentifierAttribute() {
		return memberIdentifierAttribute;
	}

	public void setMemberIdentifierAttribute(UUID memberIdentifierAttribute) {
		this.memberIdentifierAttribute = memberIdentifierAttribute;
	}

	public UUID getMemberOfAttribute() {
		return memberOfAttribute;
	}

	public void setMemberOfAttribute(UUID memberOfAttribute) {
		this.memberOfAttribute = memberOfAttribute;
	}

	public boolean isForwardAcmSwitch() {
		return forwardAcmSwitch;
	}

	public boolean isSkipValueIfExcludedSwitch() {
		return skipValueIfExcludedSwitch;
	}

	public void setSkipValueIfExcludedSwitch(boolean skipValueIfExcludedSwitch) {
		this.skipValueIfExcludedSwitch = skipValueIfExcludedSwitch;
	}

	public void setForwardAcmSwitch(boolean forwardAcmSwitch) {
		this.forwardAcmSwitch = forwardAcmSwitch;
	}

	public boolean isMembershipSwitch() {
		return membershipSwitch;
	}

	public void setMembershipSwitch(boolean membershipSwitch) {
		this.membershipSwitch = membershipSwitch;
	}

	public boolean isAssignRoleSwitch() {
		return assignRoleSwitch;
	}

	public void setAssignRoleSwitch(boolean assignRoleSwitch) {
		this.assignRoleSwitch = assignRoleSwitch;
	}

	public boolean isAssignCatalogueSwitch() {
		return assignCatalogueSwitch;
	}

	public void setAssignCatalogueSwitch(boolean assignCatalogueSwitch) {
		this.assignCatalogueSwitch = assignCatalogueSwitch;
	}

	public boolean isAssignRoleRemoveSwitch() {
		return assignRoleRemoveSwitch;
	}

	public void setAssignRoleRemoveSwitch(boolean assignRoleRemoveSwitch) {
		this.assignRoleRemoveSwitch = assignRoleRemoveSwitch;
	}

	public UUID getForwardAcmMappingAttribute() {
		return forwardAcmMappingAttribute;
	}

	public void setForwardAcmMappingAttribute(UUID forwardAcmMappingAttribute) {
		this.forwardAcmMappingAttribute = forwardAcmMappingAttribute;
	}

	public UUID getSkipValueIfExcludedMappingAttribute() {
		return skipValueIfExcludedMappingAttribute;
	}

	public void setSkipValueIfExcludedMappingAttribute(UUID skipValueIfExcludedMappingAttribute) {
		this.skipValueIfExcludedMappingAttribute = skipValueIfExcludedMappingAttribute;
	}

	public UUID getRoleIdentifiersMappingAttribute() {
		return roleIdentifiersMappingAttribute;
	}

	public void setRoleIdentifiersMappingAttribute(UUID roleIdentifiersMappingAttribute) {
		this.roleIdentifiersMappingAttribute = roleIdentifiersMappingAttribute;
	}

	public UUID getRoleMembersMappingAttribute() {
		return roleMembersMappingAttribute;
	}

	public void setRoleMembersMappingAttribute(UUID roleMembersMappingAttribute) {
		this.roleMembersMappingAttribute = roleMembersMappingAttribute;
	}

	public UUID getAssignCatalogueMappingAttribute() {
		return assignCatalogueMappingAttribute;
	}

	public void setAssignCatalogueMappingAttribute(UUID assignCatalogueMappingAttribute) {
		this.assignCatalogueMappingAttribute = assignCatalogueMappingAttribute;
	}
}
