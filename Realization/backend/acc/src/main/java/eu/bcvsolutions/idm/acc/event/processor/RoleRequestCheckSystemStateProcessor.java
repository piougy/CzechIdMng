package eu.bcvsolutions.idm.acc.event.processor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.ResultCode;
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

/**
 * Processor for check state on systems for given role-request (provisioning
 * operation)
 * 
 * @author Vít Švanda
 *
 */
@Component
@Description("Processor for check state on systems for given role-request (provisioning operation)")
public class RoleRequestCheckSystemStateProcessor extends CoreEventProcessor<IdmRoleRequestDto>
		implements RoleRequestProcessor {
	public static final String PROCESSOR_NAME = "acc-role-request-check-system-state-processor";

	@Autowired
	private SysProvisioningOperationService provisioningOperationService;
	@Autowired
	private SysProvisioningArchiveService provisioningArchiveService;
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
		OperationResultDto systemState = requestDto.getSystemState();
		if (systemState == null) {
			IdmRoleRequestDto currentRequest = event.getOriginalSource();
			systemState = currentRequest.getSystemState();
		}
		if (systemState != null && systemState.getModel() != null
				&& !AccModuleDescriptor.MODULE_ID.equals(systemState.getModel().getModule())) {
			// System state was sets from different module, so we skip this core processor (optimalization)
			return false;
		}
		
		return super.conditional(event);
	}

	@Override
	public EventResult<IdmRoleRequestDto> process(EntityEvent<IdmRoleRequestDto> event) {
		IdmRoleRequestDto request = event.getContent();
		IdmRoleRequestDto currentRequest = event.getOriginalSource();
		Assert.notNull(request);
		Assert.notNull(request.getId());

		SysProvisioningOperationFilter provisioningOperationFilter = new SysProvisioningOperationFilter();
		provisioningOperationFilter.setRoleRequestId(request.getId());

		// Some active operation for that request has state CREATED -> we will wait for execution of all operations!
		provisioningOperationFilter.setResultState(OperationState.CREATED);
		long countCreatedOperations = provisioningOperationService.count(provisioningOperationFilter);
		if (countCreatedOperations > 0) {
			request.setSystemState(new OperationResultDto(OperationState.RUNNING));
			DefaultEventResult<IdmRoleRequestDto> result = new DefaultEventResult<>(event, this);
			result.getEvent().getProperties().put(SYSTEM_STATE_RESOLVED_KEY, Boolean.TRUE);
			
			return result;
		}

		provisioningOperationFilter.setResultState(null);
		long countOperations = provisioningOperationService.count(provisioningOperationFilter);
		long countArchives = provisioningArchiveService.count(provisioningOperationFilter);
		long allOperations = countOperations + countArchives;

		// Everything was done (in provisioning)
		if (countOperations == 0 && countArchives > 0) {
			List<IdmConceptRoleRequestDto> concepts = null;
			
			List<SysProvisioningArchiveDto> canceledOperations = Lists.newArrayList();
			// We want to mark canceled (archives with exception state) concepts
			canceledOperations.addAll(findCanceledArchvieOperations(request, OperationState.EXCEPTION));
			// We want to mark canceled (archives with blocked state) concepts
			canceledOperations.addAll(findCanceledArchvieOperations(request, OperationState.BLOCKED));
			// We want to mark canceled (archives with not executed state) concepts
			canceledOperations.addAll(findCanceledArchvieOperations(request, OperationState.NOT_EXECUTED));
			
			if (canceledOperations.size() > 0) {
				concepts = loadConcepts(request, concepts);
				
				Set<SysSystemDto> systems = canceledOperations.stream().map(archive -> {
					return provisioningArchiveService.getSystem(archive);
				}).distinct().collect(Collectors.toSet());
				
				for(SysSystemDto system : systems) {
					List<IdmConceptRoleRequestDto> canceledConcepts = roleSystemService.getConceptsForSystem(concepts, system.getId());
					
					// This concepts should have system-state set to CANCELED
					canceledConcepts.stream() //
							.filter(concept -> !(concept.getSystemState() != null
									&& OperationState.CANCELED == concept.getSystemState().getState())) //
							.forEach(concept -> { //
								OperationResultDto systemResult = new OperationResultDto.Builder(OperationState.CANCELED)
										.setModel(new DefaultResultModel(AccResultCode.ROLE_REQUEST_OPERATION_CANCELED,
												ImmutableMap.of("system", system.getCode())))
										.build();
								concept.setSystemState(systemResult);
								// Save role concept
								conceptRoleRequestService.save(concept);
					});
				}
			}

			// Change state for all concepts with exception to null
			OperationResultDto beforeSystemState = currentRequest.getSystemState();
			if (beforeSystemState != null && OperationState.EXCEPTION == beforeSystemState.getState() && 
					(AccResultCode.ROLE_REQUEST_ALL_OPERATIONS_FAILED.getCode().equals(beforeSystemState.getCode())
					|| AccResultCode.ROLE_REQUEST_OPERATION_FAILED.getCode().equals(beforeSystemState.getCode())
					|| AccResultCode.ROLE_REQUEST_SOME_OPERATIONS_FAILED.getCode().equals(beforeSystemState.getCode()))) {
				// Now is request executed (without exceptions), we will set all concepts with
				// exception to null
				concepts = loadConcepts(request, concepts);
				concepts.stream() // Find concepts with provisioning exception and set them system state to null
						.filter(concept -> concept.getSystemState() != null
								&& OperationState.EXCEPTION == concept.getSystemState().getState()
								&& AccResultCode.ROLE_REQUEST_OPERATION_FAILED.getCode()
										.equals(concept.getSystemState().getCode()))
						.forEach(concept -> {
							concept.setSystemState(null);
							// Save role concept
							conceptRoleRequestService.save(concept);
						});
			}
			
			request.setSystemState(new OperationResultDto(OperationState.EXECUTED));
			return new DefaultEventResult<>(event, this);
		}

		// Nothing was done
		if (countOperations == 0 && countArchives == 0) {
			request.setSystemState(null);
			DefaultEventResult<IdmRoleRequestDto> result = new DefaultEventResult<>(event, this);
			result.getEvent().getProperties().put(SYSTEM_STATE_RESOLVED_KEY, Boolean.TRUE);

			return result;
		}
		
		provisioningOperationFilter.setResultState(OperationState.EXCEPTION);
		long countFailedOperations = provisioningOperationService.count(provisioningOperationFilter);
		if (countFailedOperations == allOperations) {
			// All operations failed
			return createResult(event, //
					request, //
					OperationState.EXCEPTION, //
					AccResultCode.ROLE_REQUEST_ALL_OPERATIONS_FAILED, //
					ImmutableMap.of("requestId", request.getId()), //
					null);
		} else if (countFailedOperations > 0) {
			// Some operations failed
			List<SysProvisioningOperationDto> failedOperations = provisioningOperationService
					.find(provisioningOperationFilter, null).getContent();
			
			List<IdmConceptRoleRequestDto> concepts = loadConcepts(request, null);
			
			failedOperations.forEach(operation -> {
				Assert.notNull(operation.getSystem());
				UUID systemId = operation.getSystem();
				
				List<IdmConceptRoleRequestDto> failedConcepts = roleSystemService.getConceptsForSystem(concepts, systemId);
				failedConcepts.forEach(concept -> {
					OperationResultDto systemResult = new OperationResultDto.Builder(OperationState.EXCEPTION)
							.setModel(new DefaultResultModel(AccResultCode.ROLE_REQUEST_OPERATION_FAILED,
									ImmutableMap.of("system", provisioningOperationService.getSystem(operation).getCode())))
							.build();
					concept.setSystemState(systemResult);
					// Save role concept
					conceptRoleRequestService.save(concept);
				});
			});

			String failedSystems = getSystems(failedOperations);
			if (failedOperations.size() == 1) {
				// Exist only one failed operation -> we can add exception
				return createResult(event, //
						request, //
						OperationState.EXCEPTION, //
						AccResultCode.ROLE_REQUEST_OPERATION_FAILED, //
						ImmutableMap.of("system", failedSystems), //
						failedOperations.get(0).getResult().getCause());
			} else {
				return createResult(event, //
						request, //
						OperationState.EXCEPTION, //
						AccResultCode.ROLE_REQUEST_SOME_OPERATIONS_FAILED, //
						ImmutableMap.of("systems", failedSystems), //
						null);
			}
			
		}
		
		provisioningOperationFilter.setResultState(OperationState.BLOCKED);
		long countBlockedOperations = provisioningOperationService.count(provisioningOperationFilter);
		if (countBlockedOperations > 0) {
			// Some operations are blocked
			List<SysProvisioningOperationDto> blockedOperations = provisioningOperationService
					.find(provisioningOperationFilter, null).getContent();
			String blockedSystems = getSystems(blockedOperations);

			return createResult(event, //
					request, //
					OperationState.BLOCKED, //
					AccResultCode.ROLE_REQUEST_SOME_OPERATIONS_BLOCKED, //
					ImmutableMap.of("systems", blockedSystems), //
					null);
		}

		// Some operations are not-executed
		provisioningOperationFilter.setResultState(OperationState.NOT_EXECUTED);
		long countNotExecutedOperations = provisioningOperationService.count(provisioningOperationFilter);
		if (countNotExecutedOperations > 0) {
			request.setSystemState(new OperationResultDto(OperationState.NOT_EXECUTED));
			DefaultEventResult<IdmRoleRequestDto> result = new DefaultEventResult<>(event, this);
			result.getEvent().getProperties().put(SYSTEM_STATE_RESOLVED_KEY, Boolean.TRUE);

			return result;
		}

		// Some operation are running
		provisioningOperationFilter.setResultState(OperationState.RUNNING);
		long countRunningOperations = provisioningOperationService.count(provisioningOperationFilter);
		if (countRunningOperations > 0) {
			request.setSystemState(new OperationResultDto(OperationState.RUNNING));
			DefaultEventResult<IdmRoleRequestDto> result = new DefaultEventResult<>(event, this);
			result.getEvent().getProperties().put(SYSTEM_STATE_RESOLVED_KEY, Boolean.TRUE);

			return result;
		}

		return new DefaultEventResult<>(event, this);
	}

	/**
	 * We need to only failed operations without repaired executed archive operation
	 * 
	 * @param request
	 * @param state
	 * @return
	 */
	private List<SysProvisioningArchiveDto> findCanceledArchvieOperations(IdmRoleRequestDto request,
			OperationState state) {
		
		SysProvisioningOperationFilter provisioningOperationFilter = new SysProvisioningOperationFilter();
		provisioningOperationFilter.setRoleRequestId(request.getId());
		provisioningOperationFilter.setResultState(state);

		List<SysProvisioningArchiveDto> canceledOperations = provisioningArchiveService.find(provisioningOperationFilter, null).getContent();
		
		// We need to only failed operations without repaired executed archive operation
		return canceledOperations.stream() //
				.filter(operation -> { //
					// Check if exist some executed archive operation with for same entity/account
					// for this request (retry mechanism ..).
					SysProvisioningOperationFilter filterArchive = new SysProvisioningOperationFilter();
					filterArchive.setRoleRequestId(request.getId());
					filterArchive.setSystemId(operation.getSystem());
					filterArchive.setEntityIdentifier(operation.getEntityIdentifier());
					filterArchive.setEntityType(operation.getEntityType());
					filterArchive.setResultState(OperationState.EXECUTED);

					return provisioningArchiveService.count(filterArchive) == 0;
				}) //
				.collect(Collectors.toList());
	}

	private List<IdmConceptRoleRequestDto> loadConcepts(IdmRoleRequestDto request, List<IdmConceptRoleRequestDto> concepts) {
		if (concepts != null) {
			return concepts;
		}
		
		return conceptRoleRequestService
				.findAllByRoleRequest(request.getId());
	}

	private String getSystems(List<SysProvisioningOperationDto> operations) {
		return operations.stream() //
				.map(operation -> { //
					return provisioningOperationService.getSystem(operation).getCode();
				}) //
				.distinct() //
				.collect(Collectors.joining(","));
	}

	private EventResult<IdmRoleRequestDto> createResult(EntityEvent<IdmRoleRequestDto> event,
			IdmRoleRequestDto request, OperationState state, ResultCode resultCode, Map<String, Object> properties, String cause) {
		OperationResultDto systemResult = new OperationResultDto.Builder(state)
				.setModel(new DefaultResultModel(resultCode,
						properties))
				.build();
		systemResult.setCause(cause);
		request.setSystemState(systemResult);
		DefaultEventResult<IdmRoleRequestDto> result = new DefaultEventResult<>(event, this);
		result.getEvent().getProperties().put(SYSTEM_STATE_RESOLVED_KEY, Boolean.TRUE);

		return result;
	}

	@Override
	public int getOrder() {
		return 1000;
	}
}
