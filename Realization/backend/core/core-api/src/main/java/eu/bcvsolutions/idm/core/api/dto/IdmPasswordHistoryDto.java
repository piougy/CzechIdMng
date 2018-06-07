package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;

/**
 * DTO with password history, that determines the number of unique new
 * passwords.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Relation(collectionRelation = "passwordHistories")
public class IdmPasswordHistoryDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	@NotNull
	private String password;
	@NotNull
	@Embedded(dtoClass = IdmIdentityDto.class)
	private UUID identity;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public UUID getIdentity() {
		return identity;
	}

	public void setIdentity(UUID identity) {
		this.identity = identity;
	}

}
