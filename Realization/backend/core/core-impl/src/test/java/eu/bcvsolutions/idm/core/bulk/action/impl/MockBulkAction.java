package eu.bcvsolutions.idm.core.bulk.action.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Mock bulk action for testing action setting:
 * - showWithSelection
 * - showWithoutSelection
 * 
 * @author Radek Tomiška
 *
 */
@Component(MockBulkAction.NAME)
@Enabled(property = MockBulkAction.PROPERTY_ADDITIONAL_ENABLED)
public class MockBulkAction extends AbstractBulkAction<IdmIdentityDto, IdmIdentityFilter> {

	public static final String NAME = "test-mock-bulk-action";
	public static final String PROPERTY_ADDITIONAL_ENABLED = "idm.sec.core.test.test-mock-bulk-action.enabled";
	//
	@Autowired private IdmIdentityService identityService;

	@Override
	public ReadWriteDtoService<IdmIdentityDto, IdmIdentityFilter> getService() {
		return identityService;
	}
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected OperationResult processDto(IdmIdentityDto dto) {
		// nothing
		return null;
	}
}