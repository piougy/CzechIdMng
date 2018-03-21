package eu.bcvsolutions.idm.core.event.domain;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Test event owner
 * - AbstractEntity is used just for simplify test. Don't use entities as event contents! 
 * 
 * @author Radek Tomiška
 *
 */
public class MockOwner extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	private UUID id = UUID.randomUUID();
	
	@Override
	public UUID getId() {
		return id;
	}

}
