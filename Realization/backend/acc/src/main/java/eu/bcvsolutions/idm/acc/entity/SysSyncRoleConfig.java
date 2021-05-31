package eu.bcvsolutions.idm.acc.entity;

import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.hibernate.envers.Audited;

/**
 * Keeps specific information about role synchronization configuration.
 *
 * @author Vít Švanda
 * @since 11.1.0
 */
@Entity
@Table(name = "sys_sync_role_config", indexes = {
		@Index(name = "idx_sys_s_role_m_s_map", columnList = "member_sys_mapping"),
		@Index(name = "idx_sys_s_role_m_id_att", columnList = "member_identifier_attribute"),
		@Index(name = "idx_sys_s_role_m_of_att", columnList = "members_of_attribute"),
		@Index(name = "idx_sys_s_role_r_cat_p", columnList = "remove_catalogue_parent"),
		@Index(name = "idx_sys_s_role_m_cat_n", columnList = "main_catalogue_node")
})
public class SysSyncRoleConfig extends SysSyncConfig {

	private static final long serialVersionUID = 1L;

	/**
	 * Provisioning mapping of a system with members (identities) -> it is a different system than this one.
	 */
	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "member_sys_mapping", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private SysSystemMapping memberSystemMapping;

	/**
	 * Schema attribute on system with identities. Member's attribute with identifier (DN).
	 */
	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "member_identifier_attribute", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private SysSchemaAttribute memberIdentifierAttribute;

	/**
	 * Attribute on system with identities. Multivalued attribute with all role identifiers where identity is member of roles. 
	 */
	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "members_of_attribute", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private SysSystemAttributeMapping memberOfAttribute;

	/**
	 * Switch for activate forward an account management feature. Every role, which manages role membership on the connected system, has the option forward account management.
	 */
	@Audited
	@NotNull
	@Column(name = "forward_acm_switch", nullable = false)
	private boolean forwardAcmSwitch = false;

	/**
	 * Switch for activate a membership feature.
	 */
	@Audited
	@NotNull
	@Column(name = "membership_switch", nullable = false)
	private boolean membershipSwitch = false;

	/**
	 * Switch for activate an assign role by external system feature.
	 */
	@Audited
	@NotNull
	@Column(name = "assign_role_switch", nullable = false)
	private boolean assignRoleSwitch = false;

	/**
	 * Switch for activate feature for remove assigned role missing in external system.
	 */
	@Audited
	@NotNull
	@Column(name = "assign_role_remove_switch", nullable = false)
	private boolean assignRoleRemoveSwitch = false;

	/**
	 * Switch for activate skip value if is contract excluded feature.
	 */
	@Audited
	@NotNull
	@Column(name = "skip_value_if_ex_switch", nullable = false)
	private boolean skipValueIfExcludedSwitch = false;

	/**
	 * Switch for activate 'an assign role to a catalogue' feature.
	 */
	@Audited
	@NotNull
	@Column(name = "assign_catalogue_switch", nullable = false)
	private boolean assignCatalogueSwitch = false;

	/**
	 * Switch for activate removing role from a catalogue.
	 */
	@Audited
	@NotNull
	@Column(name = "remove_catalogue_switch", nullable = false)
	private boolean removeCatalogueRoleSwitch = false;

	/**
	 * Role from a catalogue will be removed only under this node.
	 */
	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "remove_catalogue_parent", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private IdmRoleCatalogue removeCatalogueRoleParentNode;

	/**
	 * Role catalogues will be created under this node.
	 */
	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "main_catalogue_node", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private IdmRoleCatalogue mainCatalogueRoleNode;

	public SysSystemMapping getMemberSystemMapping() {
		return memberSystemMapping;
	}

	public void setMemberSystemMapping(SysSystemMapping memberSystemMapping) {
		this.memberSystemMapping = memberSystemMapping;
	}

	public SysSchemaAttribute getMemberIdentifierAttribute() {
		return memberIdentifierAttribute;
	}

	public void setMemberIdentifierAttribute(SysSchemaAttribute memberIdentifierAttribute) {
		this.memberIdentifierAttribute = memberIdentifierAttribute;
	}

	public SysSystemAttributeMapping getMemberOfAttribute() {
		return memberOfAttribute;
	}

	public void setMemberOfAttribute(SysSystemAttributeMapping memberOfAttribute) {
		this.memberOfAttribute = memberOfAttribute;
	}

	public boolean isForwardAcmSwitch() {
		return forwardAcmSwitch;
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

	public boolean isSkipValueIfExcludedSwitch() {
		return skipValueIfExcludedSwitch;
	}

	public void setSkipValueIfExcludedSwitch(boolean skipValueIfExcludedSwitch) {
		this.skipValueIfExcludedSwitch = skipValueIfExcludedSwitch;
	}

	public boolean isAssignRoleRemoveSwitch() {
		return assignRoleRemoveSwitch;
	}

	public void setAssignRoleRemoveSwitch(boolean assignRoleRemoveSwitch) {
		this.assignRoleRemoveSwitch = assignRoleRemoveSwitch;
	}

	public boolean isAssignCatalogueSwitch() {
		return assignCatalogueSwitch;
	}

	public void setAssignCatalogueSwitch(boolean assignCatalogueSwitch) {
		this.assignCatalogueSwitch = assignCatalogueSwitch;
	}

	public boolean isRemoveCatalogueRoleSwitch() {
		return removeCatalogueRoleSwitch;
	}

	public void setRemoveCatalogueRoleSwitch(boolean removeCatalogueRoleSwitch) {
		this.removeCatalogueRoleSwitch = removeCatalogueRoleSwitch;
	}

	public IdmRoleCatalogue getRemoveCatalogueRoleParentNode() {
		return removeCatalogueRoleParentNode;
	}

	public void setRemoveCatalogueRoleParentNode(IdmRoleCatalogue removeCatalogueRoleParentNode) {
		this.removeCatalogueRoleParentNode = removeCatalogueRoleParentNode;
	}

	public IdmRoleCatalogue getMainCatalogueRoleNode() {
		return mainCatalogueRoleNode;
	}

	public void setMainCatalogueRoleNode(IdmRoleCatalogue mainCatalogueRoleNode) {
		this.mainCatalogueRoleNode = mainCatalogueRoleNode;
	}
}
