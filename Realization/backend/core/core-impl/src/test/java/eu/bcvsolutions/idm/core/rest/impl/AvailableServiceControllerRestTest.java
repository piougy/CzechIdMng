package eu.bcvsolutions.idm.core.rest.impl;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.AvailableMethodDto;
import eu.bcvsolutions.idm.core.api.dto.AvailableServiceDto;
import eu.bcvsolutions.idm.core.api.dto.filter.AvailableServiceFilter;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Available services tests.
 * 
 * 
 * @author Ondrej Husnik
 *
 */
public class AvailableServiceControllerRestTest extends AbstractRestTest {

	@Autowired
	private ApplicationContext applicationContext;
	
	/**
	 * Method providing a REST request which returns obtained List<AvailableServiceDto> objects meeting the filter 
	 * 
	 * @param filter
	 * @return List<AvailableServiceDto>
	 */
	protected List<AvailableServiceDto> find(AvailableServiceFilter filter) {
		try {
			String response = getMockMvc().perform(get(BaseController.BASE_PATH + "/available-service")
	        		.with(authentication(getAdminAuthentication()))
	        		.params(toQueryParams(filter))
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isOk())
	                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
	                .andReturn()
	                .getResponse()
	                .getContentAsString();
			//
			return toDtos(response, AvailableServiceDto.class);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to find entities", ex);
		}
	}
	
	/**
	 * This method creates an AvailableServiceDto serving as a pattern 
	 * 
	 * @return AvailableServiceDto
	 */
	protected AvailableServiceDto createTestServiceDto () {
		
		final String serviceAnnotationName = "identityService";
		AvailableServiceDto serviceDto = new AvailableServiceDto();
		List<AvailableMethodDto> methodDtos = new ArrayList<AvailableMethodDto>();
		
		Map<String, IdmIdentityService> beans = applicationContext.getBeansOfType(IdmIdentityService.class);
		IdmIdentityService bean = beans.get(serviceAnnotationName);
		Assert.assertNotNull(bean); // bean needs to be found
		Class<?> serviceClass = AutowireHelper.getTargetClass(bean);
	
		// creates list of dtos of all public methods
		List<Method> methods = Lists.newArrayList(serviceClass.getMethods());
		List<Method> omittedMethods = Lists.newArrayList(Object.class.getMethods());
		methods.removeAll(omittedMethods); // methods form Object class are not required
		
		for (Method method : methods) {
			if (!Modifier.isPublic(method.getModifiers())) {
				continue;
			}
			List<Class<?>> params = Arrays.asList(method.getParameters())
			.stream()
			.map((param) -> {return param.getType();})
			.collect(Collectors.toList());
			
			AvailableMethodDto methodDto = new AvailableMethodDto();
			methodDto.setMethodName(method.getName());
			methodDto.setReturnType(method.getReturnType());
			methodDto.setArguments(params);
			methodDtos.add(methodDto);
		}
		methodDtos.sort((AvailableMethodDto d1, AvailableMethodDto d2) -> {return d1.getMethodName().compareTo(d2.getMethodName()); });
		
		serviceDto.setMethods(methodDtos);
		serviceDto.setId(serviceAnnotationName);
		serviceDto.setServiceName(serviceClass.getSimpleName());
		return serviceDto;
	}
	
	
	/**
	 * Test finds a service by name via REST and tests whether received service is correct and complete.
	 * 
	 */
	@Test
	public void testFoundServiceIsComplete () {
		
		AvailableServiceDto correctServiceDto = createTestServiceDto();
		List<AvailableServiceDto> testedServiceDtos = null;
		
		AvailableServiceFilter filter = new AvailableServiceFilter();
		filter.setText(correctServiceDto.getServiceName());
		testedServiceDtos = find(filter);		
		AvailableServiceDto testedServiceDto = testedServiceDtos
				.stream()
				.filter((service) -> {return service.getId().equals(correctServiceDto.getId());})
				.findFirst()
				.orElse(null);

		Assert.assertNotNull(testedServiceDto);
		Assert.assertTrue(correctServiceDto.getId().equals(testedServiceDto.getId()));
		Assert.assertTrue(correctServiceDto.getServiceName().equals(testedServiceDto.getServiceName()));
		Assert.assertTrue(testedServiceDto.getMethods().equals(correctServiceDto.getMethods()));
	}
}
