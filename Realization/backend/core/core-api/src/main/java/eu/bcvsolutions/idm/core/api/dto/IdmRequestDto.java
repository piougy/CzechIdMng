package eu.bcvsolutions.idm.core.api.dto;

import javax.validation.constraints.Size;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.RequestState;

/**
 * DTO for request
 *
 * @author svandav
 */
@Relation(collectionRelation = "requests")
public class IdmRequestDto extends AbstractRequestDto {

	private static final long serialVersionUID = 1L;

	@Size(min = 1, max = DefaultFieldLengths.NAME)
	private String requestType;
	@Size(max = DefaultFieldLengths.NAME)
	private String name;

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public RequestState getState() {
		return state;
	}

	public void setState(RequestState state) {
		this.state = state;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}