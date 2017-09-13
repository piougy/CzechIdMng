package eu.bcvsolutions.idm.acc.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.OperationResultDto;
import eu.bcvsolutions.idm.acc.dto.ProvisioningAttributeDto;
import eu.bcvsolutions.idm.acc.dto.ProvisioningContextDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBatchDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningRequestDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.ProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningOperationRepository;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningRequestRepository;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningRequestService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.service.api.NotificationManager;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.ConfidentialString;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcPasswordAttribute;
import eu.bcvsolutions.idm.ic.impl.IcAttributeImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorObjectImpl;
import eu.bcvsolutions.idm.ic.impl.IcPasswordAttributeImpl;

/**
 * Persists provisioning operations
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultSysProvisioningOperationService
		extends AbstractReadWriteDtoService<SysProvisioningOperationDto, SysProvisioningOperation, ProvisioningOperationFilter> implements SysProvisioningOperationService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultSysProvisioningOperationService.class);
	private static final String CONFIDENTIAL_KEY_PATTERN = "%s:%s:%d";
	private static final String ACCOUNT_OBJECT_PROPERTY_PREFIX = "sys:account:";
	private static final String CONNECTOR_OBJECT_PROPERTY_PREFIX = "sys:connector:";
	
	private final SysProvisioningArchiveService provisioningArchiveService;
	private final SysProvisioningBatchService batchService;
	private final NotificationManager notificationManager;
	private final ConfidentialStorage confidentialStorage;
	private final SysProvisioningRequestService requestService;
	private final SysSystemService systemService;
	private final ModelMapper modelMapper;
	private final SysProvisioningRequestService provisioningRequestService;

	@Autowired
	public DefaultSysProvisioningOperationService(
			SysProvisioningOperationRepository repository,
			SysProvisioningRequestRepository provisioningRequestRepository,
			SysProvisioningArchiveService provisioningArchiveService,
			SysProvisioningBatchService batchService,
			NotificationManager notificationManager,
			ConfidentialStorage confidentialStorage,
			SysProvisioningRequestService requestService,
			SysSystemService systemService,
			ModelMapper modelMapper,
			SysProvisioningRequestService provisioningRequestService) {
		super(repository);
		//
		Assert.notNull(provisioningRequestRepository);
		Assert.notNull(provisioningArchiveService);
		Assert.notNull(batchService);
		Assert.notNull(notificationManager);
		Assert.notNull(confidentialStorage);
		Assert.notNull(requestService);
		Assert.notNull(systemService);
		Assert.notNull(modelMapper);
		Assert.notNull(provisioningRequestService);
		//
		this.provisioningArchiveService = provisioningArchiveService;
		this.batchService = batchService;
		this.notificationManager = notificationManager;
		this.confidentialStorage = confidentialStorage;
		this.requestService = requestService;
		this.systemService = systemService;
		this.modelMapper = modelMapper;
		this.provisioningRequestService = provisioningRequestService;
	}
	
	@Override
	protected SysProvisioningOperationDto toDto(SysProvisioningOperation entity, SysProvisioningOperationDto dto) {
		if (entity == null) {
			return null;
		}
		if (dto == null) {
			dto = modelMapper.map(entity, getDtoClass());
		} else {
			modelMapper.map(entity, dto);
		}
		
		if (dto != null) {
			dto.setRequest(requestService.findByOperationId(entity.getId()));
		}
		return dto;
	}
	
	@Override
	@Transactional(readOnly = true)
	public SysProvisioningOperationDto get(Serializable id, BasePermission... permission) {
		return super.get(id, permission);
	}
	
	@Override
	@Transactional
	public SysProvisioningOperationDto save(SysProvisioningOperationDto dto, BasePermission... permission) {
		// replace guarded strings to confidential strings (save to persist)
		Map<String, Serializable> confidentialValues = replaceGuardedStrings(dto.getProvisioningContext());
		//
		// ger request before save
		SysProvisioningRequestDto requestDto = dto.getRequest();
		// save operation
		dto = super.save(dto);
		// save prepared guarded strings into confidential storage 
		for(Entry<String, Serializable> entry : confidentialValues.entrySet()) {
			confidentialStorage.save(dto.getId(), SysProvisioningOperation.class, entry.getKey(), entry.getValue());
		}
		// set operation id to request, save and set back to operation
		requestDto.setOperation(dto.getId());
		requestDto = requestService.save(requestDto);
		dto.setRequest(requestDto);
		//
		return dto;
	}

	@Override
	@Transactional
	public void delete(SysProvisioningOperationDto provisioningOperation, BasePermission... permission) {
		Assert.notNull(provisioningOperation);
		//
		// delete persisted confidential storage values
		deleteConfidentialStrings(provisioningOperation);
		//
		// create archived operation
		provisioningArchiveService.archive(provisioningOperation);	
		//
		SysProvisioningRequestDto request = provisioningOperation.getRequest();

		SysProvisioningBatchDto batch = batchService.get(request.getBatch());
		
		if (requestService.findByBatchId(batch.getId(), null).getSize() <= 1) {
			batchService.delete(batch);
		}
		provisioningRequestService.delete(request);
		provisioningOperation.setRequest(null);
		//
		super.delete(provisioningOperation);
	}
	
	/**
	 * Returns fully loaded AccountObject with guarded strings.
	 * 
	 * @param provisioningOperation
	 * @return
	 */
	@Override
	public Map<ProvisioningAttributeDto, Object> getFullAccountObject(SysProvisioningOperationDto provisioningOperation) {
		if (provisioningOperation == null 
				|| provisioningOperation.getProvisioningContext() == null 
				|| provisioningOperation.getProvisioningContext().getAccountObject() == null) {
			return null;
		}
		//
		Map<ProvisioningAttributeDto, Object> fullAccountObject = new HashMap<>();
		Map<ProvisioningAttributeDto, Object> accountObject = provisioningOperation.getProvisioningContext().getAccountObject();
		for (Entry<ProvisioningAttributeDto, Object> entry : accountObject.entrySet()) {
			if (entry.getValue() == null) {
				fullAccountObject.put(entry.getKey(), entry.getValue());
				continue;
			}
			Object idmValue = entry.getValue();
			// single value
			if (idmValue instanceof ConfidentialString) {
				fullAccountObject.put(
						entry.getKey(), 
						confidentialStorage.getGuardedString(
								provisioningOperation.getId(), 
								SysProvisioningOperation.class, 
								((ConfidentialString)idmValue).getKey())
						);
				continue;
			}
			// array
			if(idmValue.getClass().isArray()) {
				if (!idmValue.getClass().getComponentType().isPrimitive()) { // objects only, we dont want pto proces byte, boolean etc.
					Object[] idmValues = (Object[]) idmValue;
					List<GuardedString> processedValues = new ArrayList<>();
					for(int j = 0; j < idmValues.length; j++) {
						Object singleValue = idmValues[j];
						if (singleValue instanceof ConfidentialString) {
							processedValues.add(confidentialStorage.getGuardedString(
									provisioningOperation.getId(), 
									SysProvisioningOperation.class, 
									((ConfidentialString)singleValue).getKey()));
						}
					}
					if (!processedValues.isEmpty()) {
						fullAccountObject.put(entry.getKey(), processedValues.toArray(new GuardedString[processedValues.size()]));
						continue;
					}
				}
			}
			// collection
			else if (idmValue instanceof Collection) {
				Collection<?> idmValues = (Collection<?>) idmValue;
				List<GuardedString> processedValues = new ArrayList<>();
				idmValues.forEach(singleValue -> {
					if (singleValue instanceof ConfidentialString) {													
						processedValues.add(confidentialStorage.getGuardedString(
								provisioningOperation.getId(), 
								SysProvisioningOperation.class, 
								((ConfidentialString)singleValue).getKey()));
					}
				});
				if (!processedValues.isEmpty()) {
					fullAccountObject.put(entry.getKey(), processedValues);
					continue;
				}
			}
			// copy value
			fullAccountObject.put(entry.getKey(), entry.getValue());
		}	
		return fullAccountObject;
	}
	
	/**
	 * Returns fully loaded ConnectorObject with guarded strings.
	 * 
	 * TODO: don't update connectorObject in provisioningOperation (needs attribute defensive clone)
	 * 
	 * @param provisioningOperation
	 * @return
	 */
	@Override
	public IcConnectorObject getFullConnectorObject(SysProvisioningOperationDto provisioningOperation) {
		if (provisioningOperation == null 
				|| provisioningOperation.getProvisioningContext() == null 
				|| provisioningOperation.getProvisioningContext().getConnectorObject() == null) {
			return null;
		}
		List<IcAttribute> attributes = new ArrayList<>();
		//
		IcConnectorObject connectorObject = provisioningOperation.getProvisioningContext().getConnectorObject();		
		connectorObject.getAttributes().forEach(attribute -> {
			IcAttribute attributeCopy = null;
			if (attribute.isMultiValue()) {
				List<Object> values = (List<Object>)attribute.getValues();
				attributeCopy = new IcAttributeImpl(attribute.getName(), values, true);
			} else if (attribute instanceof IcPasswordAttribute && attribute.getValue() != null) {
				attributeCopy = new IcPasswordAttributeImpl(
						attribute.getName(), 
						confidentialStorage.getGuardedString(
								provisioningOperation.getId(), 
								SysProvisioningOperation.class, 
								((ConfidentialString) attribute.getValue()).getKey()));
			} else if (attribute instanceof IcPasswordAttribute && attribute.getValue() == null) {
				attributeCopy = new IcPasswordAttributeImpl(attribute.getName(), (GuardedString) null);
			} else {
				attributeCopy = new IcAttributeImpl(attribute.getName(), attribute.getValue());
			}
			attributes.add(attributeCopy);
		});
		
		IcConnectorObject newConnectorObject = new IcConnectorObjectImpl(connectorObject.getUidValue(), connectorObject.getObjectClass(), attributes);
		return newConnectorObject;
	}
	
	@Override
	@Transactional
	public void handleFailed(SysProvisioningOperationDto operation, Exception ex) {
		SysSystemDto system = systemService.get(operation.getSystem());
		ResultModel resultModel = new DefaultResultModel(AccResultCode.PROVISIONING_FAILED, 
				ImmutableMap.of(
						"name", operation.getSystemEntityUid(), 
						"system", system.getName(),
						"operationType", operation.getOperationType(),
						"objectClass", operation.getProvisioningContext().getConnectorObject().getObjectClass().getType()));			
		LOG.error(resultModel.toString(), ex);
		//
		SysProvisioningRequestDto request = operation.getRequest();
		request.increaseAttempt();
		request.setMaxAttempts(6); // TODO: from configuration
		request.setResult(
				new OperationResultDto.Builder(OperationState.EXCEPTION).setCode(resultModel.getStatusEnum()).setModel(resultModel).setCause(ex).build());
		//
		request = requestService.save(request);
		operation = save(operation);
		//
		// calculate next attempt
		SysProvisioningRequestDto firstRequest = requestService.getFirstRequestByBatchId(request.getBatch());
		if (firstRequest.equals(request)) {
			SysProvisioningBatchDto batch = batchService.get(request.getBatch());
			batch.setNextAttempt(batchService.calculateNextAttempt(request));
			batch = batchService.save(batch);
		}
		//
		notificationManager.send(
				AccModuleDescriptor.TOPIC_PROVISIONING, new IdmMessageDto.Builder()
				.setModel(resultModel)
				.build());
	}
	
	@Override
	@Transactional
	public void handleSuccessful(SysProvisioningOperationDto operation) {
		SysSystemDto system = systemService.get(operation.getSystem());
		ResultModel resultModel = new DefaultResultModel(
				AccResultCode.PROVISIONING_SUCCEED, 
				ImmutableMap.of(
						"name", operation.getSystemEntityUid(), 
						"system", system.getName(),
						"operationType", operation.getOperationType(),
						"objectClass", operation.getProvisioningContext().getConnectorObject().getObjectClass().getType()));
		requestService.findByOperationId(operation.getId());
		SysProvisioningRequestDto request = operation.getRequest();
		request.setResult(new OperationResultDto.Builder(OperationState.EXECUTED).setModel(resultModel).build());
		request = requestService.save(request);
		operation = save(operation);
		//
		LOG.debug(resultModel.toString());
		notificationManager.send(AccModuleDescriptor.TOPIC_PROVISIONING, new IdmMessageDto.Builder()
				.setModel(resultModel)
				.build());
	}
	
	/**
	 * Replaces GuardedStrings as ConfidentialStrings in given {@link ProvisioningContextDto}. 
	 * 
	 * TODO: don't update accountObject in provisioningOperation (needs attribute defensive clone)
	 *
	 * @param context
	 * @return Returns values (key / value) to store in confidential storage. 
	 */
	protected Map<String, Serializable> replaceGuardedStrings(ProvisioningContextDto context) {
		try {
			Map<String, Serializable> confidentialValues = new HashMap<>();
			if (context == null) {
				return confidentialValues;
			}
			//
			Map<ProvisioningAttributeDto, Object> accountObject = context.getAccountObject();
			if (accountObject != null) {
				for (Entry<ProvisioningAttributeDto, Object> entry : accountObject.entrySet()) {
					if (entry.getValue() == null) {
						continue;
					}
					Object idmValue = entry.getValue();
					// single value
					if (idmValue instanceof GuardedString) {
						GuardedString guardedString = (GuardedString) entry.getValue();
						// save value into confidential storage
						String confidentialStorageKey = createAccountObjectPropertyKey(entry.getKey().getKey(), 0);
						confidentialValues.put(confidentialStorageKey, guardedString.asString());
						accountObject.put(entry.getKey(), new ConfidentialString(confidentialStorageKey));
					}
					// array
					else if(idmValue.getClass().isArray()) {
						if (!idmValue.getClass().getComponentType().isPrimitive()) {  // objects only, we dont want pto proces byte, boolean etc.
							Object[] idmValues = (Object[]) idmValue;
							List<ConfidentialString> processedValues = new ArrayList<>();
							for(int j = 0; j < idmValues.length; j++) {
								Object singleValue = idmValues[j];
								if (singleValue instanceof GuardedString) {
									GuardedString guardedString = (GuardedString) singleValue;
									// save value into confidential storage
									String confidentialStorageKey = createAccountObjectPropertyKey(entry.getKey().getKey(), j);
									confidentialValues.put(confidentialStorageKey, guardedString.asString());
									processedValues.add(new ConfidentialString(confidentialStorageKey));
								}
							}
							if (!processedValues.isEmpty()) {
								accountObject.put(entry.getKey(), processedValues.toArray(new ConfidentialString[processedValues.size()]));
							}
						}
					}
					// collection
					else if (idmValue instanceof Collection) {
						Collection<?> idmValues = (Collection<?>) idmValue;
						List<ConfidentialString> processedValues = new ArrayList<>();
						idmValues.forEach(singleValue -> {
							if (singleValue instanceof GuardedString) {
								GuardedString guardedString = (GuardedString) singleValue;
								// save value into confidential storage
								String confidentialStorageKey = createAccountObjectPropertyKey(entry.getKey().getKey(), processedValues.size());
								confidentialValues.put(confidentialStorageKey, guardedString.asString());							
								processedValues.add(new ConfidentialString(confidentialStorageKey));
							}
						});
						if (!processedValues.isEmpty()) {
							accountObject.put(entry.getKey(), processedValues);
						}
					}
					
				}
			}
			//
			IcConnectorObject connectorObject = context.getConnectorObject();
			if (connectorObject != null) {
				for(IcAttribute attribute : connectorObject.getAttributes()) {
					for(int j = 0; j < attribute.getValues().size(); j++) {
						Object attributeValue = attribute.getValues().get(j);
						if (attributeValue instanceof GuardedString) {
							GuardedString guardedString = (GuardedString) attributeValue;
							String confidentialStorageKey = createConnectorObjectPropertyKey(attribute, j);
							confidentialValues.put(confidentialStorageKey, guardedString.asString());
							attribute.getValues().set(j, new ConfidentialString(confidentialStorageKey));
						}
					}
				}
			}
			//
			return confidentialValues;
		} catch (Exception ex) {
			throw new CoreException("Replace guarded strings for provisioning operation failed.", ex);
		}
	}
	
	/**
	 * Creates account object property key into confidential storage
	 * 
	 * @param property
	 * @param index
	 * @return
	 */
	@Override
	public String createAccountObjectPropertyKey(String property, int index) {
		return String.format(CONFIDENTIAL_KEY_PATTERN, ACCOUNT_OBJECT_PROPERTY_PREFIX, property, index);
	}
	
	/**
	 * Creates connector object property key into confidential storage
	 * 
	 * @param property
	 * @param index
	 * @return
	 */
	@Override
	public String createConnectorObjectPropertyKey(IcAttribute property, int index) {
		return String.format(CONFIDENTIAL_KEY_PATTERN, CONNECTOR_OBJECT_PROPERTY_PREFIX, property.getName(), index);
	}
	
	/**
	 * Deletes persisted confidential storage values
	 * 
	 * @param provisioningOperation
	 */
	protected void deleteConfidentialStrings(SysProvisioningOperationDto provisioningOperation) {
		Assert.notNull(provisioningOperation);
		//
		ProvisioningContextDto context = provisioningOperation.getProvisioningContext();
		if (context == null) {
			return;
		}
		
		Map<ProvisioningAttributeDto, Object> accountObject = context.getAccountObject();
		if (accountObject != null) {
			for (Entry<ProvisioningAttributeDto, Object> entry : accountObject.entrySet()) {
				Object idmValue = entry.getValue();
				if (idmValue == null) {
					continue;
				}
				// single value
				if (idmValue instanceof ConfidentialString) {
					confidentialStorage.delete(provisioningOperation.getId(), SysProvisioningOperation.class, ((ConfidentialString)entry.getValue()).getKey());
				}
				// array
				else if(idmValue.getClass().isArray()) {
					if (!idmValue.getClass().getComponentType().isPrimitive()) {
						Object[] idmValues = (Object[]) idmValue;
						for(int j = 0; j < idmValues.length; j++) {
							Object singleValue = idmValues[j];
							if (singleValue instanceof ConfidentialString) {
								confidentialStorage.delete(
										provisioningOperation.getId(), 
										SysProvisioningOperation.class, 
										((ConfidentialString)singleValue).getKey());
							}
						}
					}
				}
				// collection
				else if (idmValue instanceof Collection) {
					Collection<?> idmValues = (Collection<?>) idmValue;
					idmValues.forEach(singleValue -> {
						if (singleValue instanceof ConfidentialString) {
							confidentialStorage.delete(
									provisioningOperation.getId(), 
									SysProvisioningOperation.class, 
									((ConfidentialString)singleValue).getKey());
						}
					});
				}		
			}
		}
		//
		IcConnectorObject connectorObject = context.getConnectorObject();
		if (connectorObject != null) {
			connectorObject.getAttributes().forEach(attribute -> {
				attribute.getValues().forEach(attributeValue -> {
					if (attributeValue instanceof ConfidentialString) {
						confidentialStorage.delete(
								provisioningOperation.getId(), 
								SysProvisioningOperation.class, 
								((ConfidentialString)attributeValue).getKey());
					}
				});	
			});
		}
	}
}
