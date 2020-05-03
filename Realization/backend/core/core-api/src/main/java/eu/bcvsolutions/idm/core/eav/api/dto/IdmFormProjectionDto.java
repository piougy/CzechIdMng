package eu.bcvsolutions.idm.core.eav.api.dto;

import javax.validation.constraints.Size;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * Form projection - entity can be created / edited by different form projection.
 * 
 * @author Radek Tomiška
 * @since 10.2.0
 */
@Relation(collectionRelation = "formProjections")
public class IdmFormProjectionDto 
		extends AbstractDto 
		implements Codeable, Disableable {
	
	private static final long serialVersionUID = 1L;
	//
	private String code;
	@Size(max = DefaultFieldLengths.NAME)
	private String ownerType;
	@Size(max = DefaultFieldLengths.NAME)
	private String module;
	@Size(max = DefaultFieldLengths.NAME)
	private String route; // route type
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	private String description;
	private boolean disabled;
	private String basicFields;
	private String formDefinitions;
	private ConfigurationMap properties;
	
	@Override
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}
	
	public String getRoute() {
		return route;
	}
	
	public void setRoute(String route) {
		this.route = route;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public boolean isDisabled() {
		return disabled;
	}
	
	@Override
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	/**
	 * Enabled form definitions and attributes.
	 * Json representation - [{ definition: uuid,  attributes: [ uuid, uuid ] }].
	 * 
	 * @return
	 */
	public String getFormDefinitions() {
		return formDefinitions;
	}
	
	/**
	 * Enabled form definitions and attributes.
	 * Json representation - [{ definition: uuid,  attributes: [ uuid, uuid ] }].
	 * 
	 * @param formDefinitions
	 */
	public void setFormDefinitions(String formDefinitions) {
		this.formDefinitions = formDefinitions;
	}
	
	/**
	 * Rendered basic fields.
	 * e.q. username, email
	 * 
	 * @return attributes joined by comma
	 */
	public String getBasicFields() {
		return basicFields;
	}
	
	/**
	 * Rendered basic fields.
	 * e.q. username, email
	 * 
	 * @param basicFields attributes joined by comma
	 */
	public void setBasicFields(String basicFields) {
		this.basicFields = basicFields;
	}
	
	/**
	 * Additional projection properties (by owner type).
	 * 
	 * @return configured properties
	 */
	public ConfigurationMap getProperties() {
		return properties;
	}
	
	/**
	 * Additional projection properties (by owner type).
	 * 
	 * @param properties configured properties
	 */
	public void setProperties(ConfigurationMap properties) {
		this.properties = properties;
	}
}
