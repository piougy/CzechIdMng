package eu.bcvsolutions.idm.core.model.entity;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.joda.time.DateTime;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Persistent information about last change in identity's application authorities.
 * 
 * TODO: rename to IdmAuthenticationToken and persist created tokens - we lost little of "stateless", but we need this for logout feature etc.
 * 
 * @author Jan Helbich
 *
 */
@Entity
@Table(name = "idm_authority_change", indexes = {
	@Index(name = "idx_idm_authority_change_identity", columnList = "identity_id")
})
public class IdmAuthorityChange implements BaseEntity {

	private static final long serialVersionUID = -4307406770937264480L;

	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	@Column(name = "id")
	private UUID id;

	@Column(name = "auth_change_timestamp")
	private DateTime authChangeTimestamp;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "identity_id",
		referencedColumnName = "id",
		foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmIdentity identity;

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public void setId(Serializable id) {
		if (id != null) {
			Assert.isInstanceOf(UUID.class, id,
					"Only UUID identifier is supported.");
		}
		this.id = (UUID) id;
	}

	public DateTime getAuthChangeTimestamp() {
		return authChangeTimestamp;
	}

	public void setAuthChangeTimestamp(DateTime authChangeTimestamp) {
		this.authChangeTimestamp = authChangeTimestamp;
	}

	public IdmIdentity getIdentity() {
		return identity;
	}

	public void setIdentity(IdmIdentity identity) {
		this.identity = identity;
	}
	
	public void authoritiesChanged() {
		this.authChangeTimestamp = DateTime.now();
	}
	
	public boolean isAuthorizationValid(DateTime authorizedTime) {
		if (authChangeTimestamp == null) {
			return true;
		}
		return authorizedTime != null && authorizedTime.isAfter(authChangeTimestamp);
	}

}
