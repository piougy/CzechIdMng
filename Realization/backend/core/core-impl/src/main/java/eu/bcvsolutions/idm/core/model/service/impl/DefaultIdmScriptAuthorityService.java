package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.ScriptAuthorityType;
import eu.bcvsolutions.idm.core.model.dto.AvailableServiceDto;
import eu.bcvsolutions.idm.core.model.dto.IdmScriptAuthorityDto;
import eu.bcvsolutions.idm.core.model.dto.filter.ScriptAuthorityFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmScriptAuthority;
import eu.bcvsolutions.idm.core.model.repository.IdmScriptAuthorityRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmScriptAuthorityService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Deafault implementation for {@link IdmScriptAuthorityService}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service("scriptAuthorityService")
public class DefaultIdmScriptAuthorityService extends AbstractReadWriteDtoService<IdmScriptAuthorityDto, IdmScriptAuthority, ScriptAuthorityFilter> implements IdmScriptAuthorityService {
	
	private final ApplicationContext applicationContext;
	private List<AvailableServiceDto> services;
	
	@Autowired
	public DefaultIdmScriptAuthorityService(
			IdmScriptAuthorityRepository repository,
			ApplicationContext applicationContext) {
		super(repository);
		//
		Assert.notNull(applicationContext);
		//
		this.applicationContext = applicationContext;
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
		}
		return super.save(dto, permission);
	}

	@Override
	public void deleteAllByScript(UUID scriptId) {
		ScriptAuthorityFilter filter = new ScriptAuthorityFilter();
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
				result.add(new AvailableServiceDto(entry.getKey(), getServiceClassName(entry.getValue())));
			} else if (entry.getKey().matches(".*" + serviceName + ".*")) {
				result.add(new AvailableServiceDto(entry.getKey(), getServiceClassName(entry.getValue())));
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
	
	/**
	 * Method get service class name from proxy
	 * @param value
	 * @return
	 */
	private String getServiceClassName(Object value) {
		return AopUtils.getTargetClass(value).getName();
	}

}
