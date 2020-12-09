package eu.bcvsolutions.idm.vs.bulk.action.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
 * Bulk operation for mark virtual system's requests as realized.
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
@Enabled(VirtualSystemModuleDescriptor.MODULE_ID)
@Component("vsRequestRealizeBulkAction")
@Description("Bulk operation for mark virtual system's requests as realized.")
public class VsRequestRealizeBulkAction extends AbstractBulkAction<VsRequestDto, VsRequestFilter> {

	public static final String NAME = "vs-request-realize-bulk-action";
	public static final String REASON = "reason";

	@Autowired
	private VsRequestService requestService;

	@Override
	protected OperationResult processEntities(Collection<UUID> entitiesId) {

		// TODO: Potentially memory problem.
		// Load all vs-requests. We need to order requests by
		// date of creation, because create requests have to be first.
		ArrayList<VsRequestDto> requests = Lists.newArrayList();
		entitiesId.forEach(uuid -> {
			VsRequestDto vsRequestDto = requestService.get(uuid);
			if (vsRequestDto != null) {
				requests.add(vsRequestDto);
			}
		});
		// Sort by created.
		List<UUID> sortedIds = requests.stream()
				.sorted(Comparator.comparing(VsRequestDto::getCreated))
				.map(VsRequestDto::getId)
				.collect(Collectors.toList());

		return super.processEntities(sortedIds);
	}

	@Override
	protected OperationResult processDto(VsRequestDto dto) {
		Assert.notNull(dto, "Request is required!");
		Assert.notNull(dto.getId(), "Id of system is required!");
		Object reasonObj = this.getProperties().get(REASON);
		// Check rights
		requestService.checkAccess(requestService.get(dto.getId(), IdmBasePermission.READ), IdmBasePermission.UPDATE);
		// Realize the request.
		requestService.realize(dto, (String) reasonObj);

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
		reason.setRequired(false);
		formAttributes.add(reason);

		return formAttributes;
	}

	@Override
	public int getOrder() {
		return super.getOrder() + 40;
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
