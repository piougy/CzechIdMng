package eu.bcvsolutions.idm.core.security.api.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;

/**
 * Evaluator dto - definition only
 * 
 * @author Radek Tomiška
 *
 */
@Relation(collectionRelation = "authorizationEvaluators")
public class AuthorizationEvaluatorDto implements BaseDto {

	private static final long serialVersionUID = -5895458403703525351L;
	@NotEmpty
	private String module;
	@NotEmpty
	private String entityType;
	@NotEmpty
	private String evaluatorType;
	private List<String> parameters;
	private String description;
	
	@Override
	public String getId() {
		return evaluatorType;
	}
	
	@Override
	public void setId(Serializable id) {
		evaluatorType = (String) id;	
	}
	
	public String getModule() {
		return module;
	}
	
	public void setModule(String module) {
		this.module = module;
	}
	
	public String getEntityType() {
		return entityType;
	}
	
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
	
	public String getEvaluatorType() {
		return evaluatorType;
	}
	
	public void setEvaluatorType(String evaluatorType) {
		this.evaluatorType = evaluatorType;
	}
	
	public List<String> getParameters() {
		if (parameters == null) {
			parameters = new ArrayList<>();
		}
		return parameters;
	}
	
	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
}
