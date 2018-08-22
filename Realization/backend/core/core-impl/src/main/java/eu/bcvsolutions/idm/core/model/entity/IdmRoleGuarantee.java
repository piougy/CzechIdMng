package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Intersection table beetween role and identity - guarantee of role
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "idm_role_guarantee", indexes = {
				@Index(name = "idx_idm_role_guarantee_role", columnList = "role_id"),
				@Index(name = "idx_idm_role_guarantee_gnt", columnList = "guarantee_id"),
				@Index(name = "idx_idm_role_guarantee_ext_id", columnList = "external_id")} )
public class IdmRoleGuarantee extends AbstractEntity implements ExternalIdentifiable {

	private static final long serialVersionUID = 6106304497345109366L;

	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "external_id", length = DefaultFieldLengths.NAME)
	private String externalId;
	
	@NotNull
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmRole role; // owner
	
	
	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "guarantee_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmIdentity guarantee; // guarantee as identity

	/**
	 * Role owner
	 * 
	 * @return
	 */
	public IdmRole getRole() {
		return role;
	}

	/**
	 * Role owner
	 * 
	 * @param role
	 */
	public void setRole(IdmRole role) {
		this.role = role;
	}
	
	/**
	 * Guarantee as identity
	 * 
	 * @return
	 */
	public IdmIdentity getGuarantee() {
		return guarantee;
	}

	/**
	 * Guarantee as identity
	 * 
	 * @param guarantee
	 */
	public void setGuarantee(IdmIdentity guarantee) {
		this.guarantee = guarantee;
	}
	
	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	@Override
	public String getExternalId() {
		return externalId;
	}
}
