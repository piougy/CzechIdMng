package eu.bcvsolutions.idm.core.model.event;

import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDefinitionDto;
import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for save definition of a delegation.
 *
 * @author Vít Švanda
 *
 */
public class DelegationDefinitionEvent extends CoreEvent<IdmDelegationDefinitionDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported events
	 *
	 */
	public enum DelegationDefinitionEventType implements EventType {
		CREATE, UPDATE, DELETE
	}

	public DelegationDefinitionEvent(DelegationDefinitionEventType operation, IdmDelegationDefinitionDto content) {
		super(operation, content);
	}

	public DelegationDefinitionEvent(DelegationDefinitionEventType operation, IdmDelegationDefinitionDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}
