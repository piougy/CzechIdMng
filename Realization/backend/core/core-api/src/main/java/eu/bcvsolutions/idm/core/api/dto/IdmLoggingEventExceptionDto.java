package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;

/**
 * Dto for entity IdmLoggingEventException
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Relation(collectionRelation = "loggingEventExceptions")
public class IdmLoggingEventExceptionDto implements BaseDto {

	private static final long serialVersionUID = 7785506028503517861L;

	private Long id;
	@Embedded(dtoClass = IdmLoggingEventDto.class)
	private UUID event;
	private String traceLine;
}
