package eu.bcvsolutions.idm.vs.event.processor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.RoleRequestProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent.RoleRequestEventType;
import eu.bcvsolutions.idm.vs.domain.VsRequestState;
import eu.bcvsolutions.idm.vs.dto.VsRequestDto;
import eu.bcvsolutions.idm.vs.dto.filter.VsRequestFilter;
import eu.bcvsolutions.idm.vs.exception.VsResultCode;
import eu.bcvsolutions.idm.vs.service.api.VsRequestService;

/**
 * Processor for check state on systems for given role-request (virtual systems)
 * 
 * @author Vít Švanda
 *
 */
@Component(RoleRequestCheckSystemStateProcessor.PROCESSOR_NAME)
@Description("Processor for check state on systems for given role-request (virtual systems)")
public class RoleRequestCheckSystemStateProcessor extends CoreEventProcessor<IdmRoleRequestDto>
		implements RoleRequestProcessor {
	public static final String PROCESSOR_NAME = "vs-role-request-check-system-state-processor";
	@Autowired
	private VsRequestService requestService;
	@Autowired
	private IdmConceptRoleRequestService conceptRoleRequestService;
	@Autowired
	private SysRoleSystemService roleSystemService;

	@Autowired
	public RoleRequestCheckSystemStateProcessor(IdmRoleRequestService service) {
		super(RoleRequestEventType.REFRESH_SYSTEM_STATE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public boolean conditional(EntityEvent<IdmRoleRequestDto> event) {
		IdmRoleRequestDto requestDto = event.getContent();
		if (RoleRequestState.EXECUTED != requestDto.getState()) {
			return false;
		}
		
		if(this.getBooleanProperty(SYSTEM_STATE_RESOLVED_KEY, event.getProperties())){
			// State was resolved in previous processors
			return false;
		}
		
		return super.conditional(event);
	}

	@Override
	public EventResult<IdmRoleRequestDto> process(EntityEvent<IdmRoleRequestDto> event) {
		IdmRoleRequestDto request = event.getContent();
		Assert.notNull(request, "Request is required.");
		Assert.notNull(request.getId(), "Request identifier is required.");
		
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setRoleRequestId(request.getId());
		
		// Some active requests for that request has state IN_PROGRESS -> we will wait for execution of all requests!
		requestFilter.setState(VsRequestState.IN_PROGRESS);
		List<VsRequestDto> inprogressRequests = requestService.find(requestFilter, null).getContent();
		if (inprogressRequests.size() > 0) {
			OperationResultDto systemResult = new OperationResultDto.Builder(OperationState.RUNNING)
					.setModel(new DefaultResultModel(VsResultCode.ROLE_REQUEST_VS_REQUEST_IN_PROGRESS,
							ImmutableMap.of("systems", this.getSystems(inprogressRequests))))
					.build();
			request.setSystemState(systemResult);
			DefaultEventResult<IdmRoleRequestDto> result = new DefaultEventResult<>(event, this);
			
			return result;
		}

		requestFilter.setState(null);
		long countAllRequests = requestService.count(requestFilter);
		requestFilter.setOnlyArchived(Boolean.TRUE);
		long countArchives = requestService.count(requestFilter);

		// Nothing was done
		if (countAllRequests == 0) {
			DefaultEventResult<IdmRoleRequestDto> result = new DefaultEventResult<>(event, this);
			
			return result;
		}

		// Everything was done (in VS)
		if (countAllRequests == countArchives && countArchives > 0) {
			request.setSystemState(new OperationResultDto(OperationState.EXECUTED));
			
			requestFilter.setOnlyArchived(Boolean.TRUE);
			requestFilter.setState(VsRequestState.CANCELED);
			// Check rejected requests - if exists some rejected request, we need to save this
			// information to role-concepts using role with this system.

			List<VsRequestDto> rejectedRequests = requestService.find(requestFilter, null).getContent();
			if (!rejectedRequests.isEmpty()) {
				List<IdmConceptRoleRequestDto> concepts = conceptRoleRequestService
						.findAllByRoleRequest(request.getId());
				rejectedRequests.forEach(vsRequest -> {
					Assert.notNull(vsRequest.getSystem(), "System is required.");
					UUID systemId = vsRequest.getSystem();

					List<IdmConceptRoleRequestDto> rejectedConcepts = roleSystemService.getConceptsForSystem(concepts, systemId);
					rejectedConcepts.forEach(concept -> {
						OperationResultDto systemResult = new OperationResultDto.Builder(OperationState.CANCELED)
								.setModel(new DefaultResultModel(VsResultCode.ROLE_REQUEST_VS_REQUEST_REJECTED,
										ImmutableMap.of("system", requestService.getSystem(vsRequest).getCode())))
								.build();
						concept.setSystemState(systemResult);
						// Save role concept
						conceptRoleRequestService.save(concept);
					});
				});
			}
		}

		return new DefaultEventResult<>(event, this);
	}
	
	private String getSystems(List<VsRequestDto> requests) {
		String systems = requests.stream() //
				.map(operation -> { //
					return requestService.getSystem(operation).getCode();
				}) //
				.distinct() //
				.collect(Collectors.joining(","));
		return systems;
	}

	@Override
	/**
	 * Run after ACC processor
	 */
	public int getOrder() {
		return 2000;
	}
}
