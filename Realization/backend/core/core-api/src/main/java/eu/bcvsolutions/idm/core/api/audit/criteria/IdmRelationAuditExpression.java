package eu.bcvsolutions.idm.core.api.audit.criteria;

import java.util.Map;

import org.hibernate.envers.boot.internal.EnversService;
import org.hibernate.envers.internal.reader.AuditReaderImplementor;
import org.hibernate.envers.internal.tools.query.Parameters;
import org.hibernate.envers.internal.tools.query.QueryBuilder;
import org.hibernate.envers.query.criteria.AuditCriterion;
import org.hibernate.envers.query.criteria.internal.IlikeAuditExpression;

/**
 * Product audit query criterion for comparing specific fields. That isn't accessible by
 * default audit criterions. For example {@link RelatedAuditExpression}, {@link IlikeAuditExpression}, ...
 *
 * @author Ondrej Kopr
 *
 */
public class IdmRelationAuditExpression implements AuditCriterion {

	public static final String EQUALS_SIGN = "=";

	private final String rightPropertyName;
	private final String leftPropertyName;
	private final String op;
	
	public IdmRelationAuditExpression(String leftPropertyName, String op, String rightPropertyName) {
		this.leftPropertyName = leftPropertyName;
		this.op = op;
		this.rightPropertyName = rightPropertyName;
	}
	
	@Override
	public void addToQuery(
			EnversService enversService, 
			AuditReaderImplementor versionsReader,
			Map<String, String> aliasToEntityNameMap, 
			String baseAlias, QueryBuilder qb, Parameters parameters) {
		parameters.addWhere(leftPropertyName, true, op, rightPropertyName, true);
	}
}
