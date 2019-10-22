package eu.bcvsolutions.idm.core.config;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityNotFoundException;

import org.modelmapper.MappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.exception.ModelMapperServiceInitException;
import eu.bcvsolutions.idm.core.security.api.exception.ConfigurationDisabledException;
import eu.bcvsolutions.idm.core.workflow.service.impl.DefaultWorkflowHistoricProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.impl.DefaultWorkflowHistoricTaskInstanceService;

/**
 * Check model mapper is properly initialized to prevent:
 * org.modelmapper.MappingException: ModelMapper mapping errors: Converter org.modelmapper.internal.converter.CollectionConverter@7214dbf8 failed to convert 
 * 
 * @author Radek Tomiška
 * @since 9.7.9
 */
@Component
public class ModelMapperChecker {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ModelMapperChecker.class);
	public static final String PROPERTY_ENABLED = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.modelmapper.checker.enabled";
	public static final boolean DEFAULT_ENABLED = true;
	//
	@Autowired private ApplicationContext context;
	@Autowired private ConfigurationService configurationService;
	@Autowired
	@Qualifier("objectMapper")
	private ObjectMapper mapper;
	
	/**
	 * Check registered services and their conversions to dto provided by model mapper.
	 * Throws Exception, if check does not pass.
	 * 
	 * @throws ConfigurationDisabledException if check is disabled by configuration.
	 * @throws ResultCodeException if service check failed (referential integrity is broken or other IdM exception occurs).
	 * @throws ModelMapperServiceInitException if mapper is wrongly inited.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void verify() {
		if (!configurationService.getBooleanValue(PROPERTY_ENABLED, DEFAULT_ENABLED)) {
			LOG.warn("Init: check registered IdM services is disabled.");
			//
			throw new ConfigurationDisabledException(ModelMapperChecker.PROPERTY_ENABLED);
		}
		long start = System.currentTimeMillis();
		int modelMapperUsed = 0;
		Map<String, ReadDtoService> services = context.getBeansOfType(ReadDtoService.class);
		for (ReadDtoService service : services.values()) {
			if ((service instanceof DefaultWorkflowHistoricTaskInstanceService)
					|| (service instanceof DefaultWorkflowHistoricProcessInstanceService)) {
				LOG.debug("Workflow history service [{}] will not be checked - find method with pagination is not fully supported and took long time."
						, service.getClass());
				continue;
			}
			//
			LOG.trace("Service [{}] will be checked.", service.getClass());
			try {
				BaseFilter filter = null;
				if (service.getFilterClass() != null) { // some services could not define filter
					try {
						if (service.getFilterClass().equals(DataFilter.class)) {
							filter = new DataFilter(service.getDtoClass());
						} else {
							filter = (BaseFilter) mapper.convertValue(new HashMap(), service.getFilterClass());
						}
					} catch (Exception ex) {
						LOG.debug("Service [{}] filter [{}] cannot be constructed. Find method will be checked without filter.",
								service.getClass(), service.getFilterClass());
					}
				}
				if (!service.find(filter, new PageRequest(0, 1)).getContent().isEmpty()) {
					modelMapperUsed++;
				}
			} catch (UnsupportedOperationException ex) {
				LOG.debug("Service [{}] does not support find method. Check will be skipped.", service.getClass());
			} catch (MappingException ex) {
				throw new ModelMapperServiceInitException(AutowireHelper.getTargetType(service), null);
			} catch (EntityNotFoundException ex) {
				throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", ex.getMessage()));
			} catch (ResultCodeException ex) {
				throw ex;
			} catch (Exception ex) {
				if (ex.getCause() instanceof EntityNotFoundException) {
					throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", ex.getMessage()));
				}
				LOG.error("Service [{}] cannot be checked. Find method cannot be called.", service.getClass(), ex);
			}
			LOG.trace("Service [{}] was checked.", service.getClass());
		}
		LOG.info("Init: all registered IdM services [{}]. "
				+ "Services usage were checked [{}] (agenda contains some records) [took: {}ms]."
				, services.size(), modelMapperUsed, System.currentTimeMillis() - start);
	}

}
