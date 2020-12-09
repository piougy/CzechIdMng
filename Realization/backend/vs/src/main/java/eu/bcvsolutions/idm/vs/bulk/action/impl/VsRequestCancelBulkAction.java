package eu.bcvsolutions.idm.vs.bulk.action.impl;

import java.util.List;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.vs.VirtualSystemModuleDescriptor;
import eu.bcvsolutions.idm.vs.domain.VirtualSystemGroupPermission;
import eu.bcvsolutions.idm.vs.dto.VsRequestDto;
import eu.bcvsolutions.idm.vs.dto.filter.VsRequestFilter;
import eu.bcvsolutions.idm.vs.service.api.VsRequestService;

/**
 * Bulk operation for cancel virtual system's requests.
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
@Enabled(VirtualSystemModuleDescriptor.MODULE_ID)
@Component("vsRequestCancelBulkAction")
@Description("Bulk operation for cancel virtual system's requests.")
public class VsRequestCancelBulkAction extends AbstractBulkAction<VsRequestDto, VsRequestFilter> {

	public static final String NAME = "vs-request-cancel-bulk-action";
	public static final String REASON = "reason";

	@Autowired
	private VsRequestService requestService;

	@Override
	protected OperationResult processDto(VsRequestDto dto) {
		Assert.notNull(dto, "Request is required!");
		Assert.notNull(dto.getId(), "Id of system is required!");
		Object reasonObj = this.getProperties().get(REASON);
		Assert.isTrue(reasonObj instanceof String && Strings.isNotBlank((String) reasonObj),
				"Reason cannot be empty for the cancel operation.");
		// Check rights
		requestService.checkAccess(requestService.get(dto.getId(), IdmBasePermission.READ), IdmBasePermission.UPDATE);
		// Cancel the request.
		requestService.cancel(dto, (String) reasonObj);

		return new OperationResult(OperationState.EXECUTED);
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(VirtualSystemGroupPermission.VS_REQUEST_UPDATE);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> formAttributes = super.getFormAttributes();

		IdmFormAttributeDto reason = new IdmFormAttributeDto(
				REASON,
				REASON,
				PersistentType.TEXT);
		reason.setFaceType(BaseFaceType.TEXTAREA);
		reason.setRequired(true);
		formAttributes.add(reason);

		return formAttributes;
	}

	@Override
	public int getOrder() {
		return super.getOrder() + 50;
	}

	@Override
	protected boolean requireNewTransaction() {
		return true;
	}

	@Override
	public ReadWriteDtoService<VsRequestDto, VsRequestFilter> getService() {
		return requestService;
	}
}
