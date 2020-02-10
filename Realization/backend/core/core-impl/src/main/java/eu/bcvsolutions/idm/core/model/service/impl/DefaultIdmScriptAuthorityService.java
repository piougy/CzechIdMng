package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.ScriptAuthorityType;
import eu.bcvsolutions.idm.core.api.dto.AvailableMethodDto;
import eu.bcvsolutions.idm.core.api.dto.AvailableServiceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptAuthorityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.AvailableServiceFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmScriptAuthorityFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.IdmScriptAuthorityService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmScriptAuthority;
import eu.bcvsolutions.idm.core.model.repository.IdmScriptAuthorityRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Default implementation for {@link IdmScriptAuthorityService}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Ondrej Husnik 
 */
@Service("scriptAuthorityService")
public class DefaultIdmScriptAuthorityService 
		extends AbstractReadWriteDtoService<IdmScriptAuthorityDto, IdmScriptAuthority, IdmScriptAuthorityFilter> 
		implements IdmScriptAuthorityService {
	
	private final ApplicationContext applicationContext;
	private List<AvailableServiceDto> services;
	private IdmScriptAuthorityRepository repository;
	
	@Autowired
	public DefaultIdmScriptAuthorityService(
			IdmScriptAuthorityRepository repository,
			ApplicationContext applicationContext) {
		super(repository);
		//
		Assert.notNull(applicationContext, "Context is required.");
		//
		this.repository = repository;
		this.applicationContext = applicationContext;
	}
	
	@Override
	protected Page<IdmScriptAuthority> findEntities(IdmScriptAuthorityFilter filter, Pageable pageable, BasePermission... permission) {
		if (filter == null) {
			return getRepository().findAll(pageable);
		}
		return repository.find(filter, pageable);
	}
	
	@Override
	public IdmScriptAuthorityDto save(IdmScriptAuthorityDto dto, BasePermission... permission) {
		// check if class is accessible
		if (dto.getType() == ScriptAuthorityType.CLASS_NAME) {
			try {
				Class.forName(dto.getClassName());
			} catch (ClassNotFoundException e) {
				throw new ResultCodeException(
						CoreResultCode.GROOVY_SCRIPT_NOT_ACCESSIBLE_CLASS,
						ImmutableMap.of("class", dto.getClassName()), e);
			}
		} else {
			// check service name with available services
			if (!isServiceReachable(dto.getService(), dto.getClassName())) {
				throw new ResultCodeException(
						CoreResultCode.GROOVY_SCRIPT_NOT_ACCESSIBLE_SERVICE,
						ImmutableMap.of("service", dto.getService()));
			}
		}
		return super.save(dto, permission);
	}

	@Override
	public void deleteAllByScript(UUID scriptId) {
		IdmScriptAuthorityFilter filter = new IdmScriptAuthorityFilter();
		filter.setScriptId(scriptId);
		//
		// remove internal by id each script authority
		find(filter, null).getContent().forEach(scriptAuthority -> this.deleteInternalById(scriptAuthority.getId()));
	}

	@Override
	public List<AvailableServiceDto> findServices(String serviceName) {
		List<AvailableServiceDto> result = new ArrayList<>();
		// BaseDtoService, not all services implemented this
		if (this.services != null && !this.services.isEmpty()) {
			return this.services;
		}
		Map<String, ScriptEnabled> services = applicationContext.getBeansOfType(ScriptEnabled.class);
		//
		for (Entry<String, ScriptEnabled> entry : services.entrySet()) {
			if (serviceName == null || serviceName.isEmpty()) {
				result.add(new AvailableServiceDto(entry.getKey()));
			} else if (entry.getKey().matches(".*" + serviceName + ".*")) {
				result.add(new AvailableServiceDto(entry.getKey()));
			}
		}
		//
		Collections.sort(result, new Comparator<AvailableServiceDto>(){
		    public int compare(AvailableServiceDto o1, AvailableServiceDto o2) {
		        return o1.getServiceName().compareToIgnoreCase(o2.getServiceName());
		    }
		});
		//
		this.services = result;
		return this.services;
	}
	
	@Override
	public List<AvailableServiceDto> findServices(AvailableServiceFilter filter) {
		List<AvailableServiceDto> result = new ArrayList<>();
		Map<String, ScriptEnabled> services = applicationContext.getBeansOfType(ScriptEnabled.class);

		for (Entry<String, ScriptEnabled> entry : services.entrySet()) {
			Class<?> clazz = AutowireHelper.getTargetClass(entry.getValue());
			String className = clazz.getSimpleName();

			if (filter == null || StringUtils.isEmpty(filter.getText())
					|| className.toLowerCase().contains(filter.getText().toLowerCase())) {
				AvailableServiceDto dto = new AvailableServiceDto();
				dto.setId(entry.getKey());
				dto.setModule(EntityUtils.getModule(clazz));
				dto.setServiceName(className);
				dto.setPackageName(clazz.getCanonicalName());
				dto.setMethods(getServiceMethods(clazz));
				result.add(dto);
			}
		}
		return result;
	}
	
	private List<AvailableMethodDto> getServiceMethods(Class<?> service) {	
		List<AvailableMethodDto> methodDtos = new ArrayList<>();
		List<Method> omittedMethods = Lists.newArrayList(Object.class.getMethods());
		List<Method> methods = Lists.newArrayList(service.getMethods());
		methods.removeAll(omittedMethods);// methods form Object class are not required

		for (Method method : methods) {
			if (!Modifier.isPublic(method.getModifiers())) {
				continue;
			}
			AvailableMethodDto dto = new AvailableMethodDto();
			dto.setMethodName(method.getName());
			dto.setReturnType(method.getReturnType().getCanonicalName());
			
			List<String> params = Arrays.asList(method.getParameters())
					.stream()
					.map((param) -> {return param.getType().getCanonicalName();})
					.collect(Collectors.toList());
			dto.setArguments(params);
			methodDtos.add(dto);
		}
		methodDtos.sort((AvailableMethodDto d1, AvailableMethodDto d2) -> {return d1.getMethodName().compareTo(d2.getMethodName()); });
		return methodDtos;
	}
	

	@Override
	public boolean isServiceReachable(String serviceName, String className) {
		//
		return !this.findServices((String)null).stream()
				.filter(
						service -> (
								service.getServiceName().equals(serviceName)
								)
						).collect(Collectors.toList()).isEmpty();
	}

}
