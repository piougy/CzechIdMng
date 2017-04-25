package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Identity's contract guarantee - manually defined  manager (if no tree structure is defined etc.)
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "idm_contract_guarantee", indexes = {
		@Index(name = "idm_contract_guarantee_contr", columnList = "identity_contract_id"),
		@Index(name = "idx_contract_guarantee_idnt", columnList = "guarantee_id")})
public class IdmContractGuarantee extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "identity_contract_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmIdentityContract identityContract;
	
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "guarantee_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmIdentity guarantee;
	
	public IdmIdentityContract getIdentityContract() {
		return identityContract;
	}
	
	public void setIdentityContract(IdmIdentityContract identityContract) {
		this.identityContract = identityContract;
	}
	
	/**
	 * Manually defined  manager (if no tree structure is defined etc.)
	 * 
	 * @return
	 */
	public IdmIdentity getGuarantee() {
		return guarantee;
	}

	public void setGuarantee(IdmIdentity guarantee) {
		this.guarantee = guarantee;
	}
}
