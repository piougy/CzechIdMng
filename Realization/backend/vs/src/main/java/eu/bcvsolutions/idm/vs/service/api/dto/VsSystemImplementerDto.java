package eu.bcvsolutions.idm.vs.service.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.vs.entity.VsRequest;
import io.swagger.annotations.ApiModel;

/**
 * DTO for request-implementer in virtual system
 * 
 * @author Svanda
 *
 */
@Relation(collectionRelation = "implementers")
@ApiModel(description = "Relation between virtual system request and identity")
public class VsRequestImplementerDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	@NotNull
	@Embedded(dtoClass = VsRequestDto.class)
	private UUID request;

	@NotNull
	@Embedded(dtoClass = IdmIdentityDto.class)
	private UUID identity;

	public UUID getRequest() {
		return request;
	}

	public void setRequest(UUID request) {
		this.request = request;
	}

	public UUID getIdentity() {
		return identity;
	}

	public void setIdentity(UUID identity) {
		this.identity = identity;
	}
}
