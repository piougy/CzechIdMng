package eu.bcvsolutions.idm.core.api.audit.criteria;

import org.hibernate.envers.query.criteria.AuditCriterion;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;

/**
 * Password self relation to not accessible field identity. This field is another entity and is in 
 * relation with the entity. By search by identity_id is get directly the column.
 * Expression compare modifier id with identity_id. This create self relation query and return only
 * audit records that is modified by original owner.
 *
 * @author Ondrej Kopr
 *
 */
public class IdmPasswordSelfRelationWithOwnerExpression extends IdmRelationAuditExpression implements AuditCriterion {

	public IdmPasswordSelfRelationWithOwnerExpression() {
		super(AbstractEntity_.modifierId.getName(), EQUALS_SIGN, "identity_id");
	}

}
