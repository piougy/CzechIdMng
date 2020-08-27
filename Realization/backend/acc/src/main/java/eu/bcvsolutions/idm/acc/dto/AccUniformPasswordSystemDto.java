package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * Connection for uniform password and systems.
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Relation(collectionRelation = "uniformPasswordSystems")
public class AccUniformPasswordSystemDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	@Embedded(dtoClass = AccUniformPasswordDto.class)
	private UUID uniformPassword;
	@Embedded(dtoClass = SysSystemDto.class)
	private UUID system;

	public UUID getUniformPassword() {
		return uniformPassword;
	}

	public void setUniformPassword(UUID uniformPassword) {
		this.uniformPassword = uniformPassword;
	}

	public UUID getSystem() {
		return system;
	}

	public void setSystem(UUID system) {
		this.system = system;
	}

}
