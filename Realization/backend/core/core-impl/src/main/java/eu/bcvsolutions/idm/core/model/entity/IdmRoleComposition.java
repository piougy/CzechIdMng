package eu.bcvsolutions.idm.core.model.entity;

import java.util.UUID;

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
import eu.bcvsolutions.idm.core.api.entity.EntityComposition;

/**
 * SuperiorRole inculde subRole (Admin > User)
 * 
 * @author Radek Tomiška 
 *
 */
@Entity
@Table(name = "idm_role_composition", indexes = {
		@Index(name = "ux_idm_role_composition_susu", columnList = "superior_id,sub_id", unique = true),
		@Index(name = "idx_idm_role_composition_sub", columnList = "sub_id"),
		@Index(name = "idx_idm_role_composition_super", columnList = "superior_id"),
		@Index(name = "idx_idm_role_composition_e_id", columnList = "external_id")
})
public class IdmRoleComposition extends AbstractEntity implements ExternalIdentifiable, EntityComposition<IdmRole> {

	private static final long serialVersionUID = -1594762884461330895L;
	
	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "external_id", length = DefaultFieldLengths.NAME)
	private String externalId;
	
	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "superior_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )	
	private IdmRole superior;
	
	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "sub_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )	
	private IdmRole sub;

	public IdmRoleComposition() {
	}

	public IdmRoleComposition(UUID id) {
		super(id);
	}
	
	public IdmRoleComposition(IdmRole superiorRole, IdmRole subRole) {
		this.superior = superiorRole;
		this.sub = subRole;
	}

	@Override
	public IdmRole getSuperior() {
		return superior;
	}

	public void setSuperior(IdmRole superiorRole) {
		this.superior = superiorRole;
	}

	@Override
	public IdmRole getSub() {
		return sub;
	}

	public void setSub(IdmRole subRole) {
		this.sub = subRole;
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