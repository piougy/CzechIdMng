package eu.bcvsolutions.idm.core.config;

import java.util.HashMap;
import java.util.Map;

import org.modelmapper.MappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;

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
	//
	@Autowired private ApplicationContext context;
	@Autowired
	@Qualifier("objectMapper")
	private ObjectMapper mapper;
	
	/**
	 * Check registered services and their conversions to dto provided by model mapper.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void verify() {
		long start = System.currentTimeMillis();
		int modelMapperUsed = 0;
		Map<String, ReadDtoService> services = context.getBeansOfType(ReadDtoService.class);
		for (ReadDtoService service : services.values()) {
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
				// Throw exception => prevent to IdM starts in invalid state.
				throw new CoreException(
						String.format("Service [%s] cannot be used, model mapper is wrongly inited, try to restart server.", service.getClass()), 
						ex);
			} catch (Exception ex) {
				LOG.error("Service [{}] cannot be checked. Find method cannot be called.", service.getClass(), ex);
			}
		}
		LOG.info("Init: all registered IdM services [{}]. "
				+ "Services usage were checked [{}] (agenda contains some records) [took: {}ms]."
				, services.size(), modelMapperUsed, System.currentTimeMillis() - start);
	}

}
