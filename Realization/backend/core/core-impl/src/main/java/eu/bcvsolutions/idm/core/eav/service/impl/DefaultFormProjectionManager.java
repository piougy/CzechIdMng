package eu.bcvsolutions.idm.core.eav.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.rest.lookup.FormProjectionLookup;
import eu.bcvsolutions.idm.core.eav.api.dto.FormProjectionRouteDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormProjectionManager;
import eu.bcvsolutions.idm.core.eav.api.service.FormProjectionRoute;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;

/**
 * Provides supported form projections.
 * 
 * @author Radek Tomiška
 * @since 10.3.0
 */
public class DefaultFormProjectionManager implements FormProjectionManager {

	@Autowired private ApplicationContext context;
	@Autowired @Lazy private EnabledEvaluator enabledEvaluator;
	//
	private final PluginRegistry<FormProjectionLookup<?>, Class<?>> formProjectionLookups;
	
	@Autowired
	public DefaultFormProjectionManager(List<? extends FormProjectionLookup<?>> formProjectionLookups) {
		Assert.notNull(formProjectionLookups, "Projection lookupsare required");
		//
		this.formProjectionLookups = OrderAwarePluginRegistry.create(formProjectionLookups);
	}
	
	@Override
	public List<FormProjectionRouteDto> getSupportedRoutes() {
		return context
			.getBeansOfType(FormProjectionRoute.class)
			.values()
			.stream()
			.filter(enabledEvaluator::isEnabled)
			.sorted(Comparator.comparing(FormProjectionRoute::getOrder))
			.map(route -> {
				FormProjectionRouteDto routeDto = new FormProjectionRouteDto();
				routeDto.setId(route.getId());
				routeDto.setName(route.getName());
				routeDto.setOwnerType(route.getOwnerType().getCanonicalName());
				routeDto.setModule(route.getModule());
				routeDto.setDescription(route.getDescription());
				routeDto.setFormDefinition(route.getFormDefinition());
				//
				return routeDto;
			})
			.collect(Collectors.toList());
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public IdmFormDefinitionDto getBasicFieldsDefinition(BaseDto dto) {
		FormProjectionLookup<BaseDto> lookup = (FormProjectionLookup<BaseDto>) formProjectionLookups.getPluginFor(dto.getClass());
		if (lookup == null) {
			return null;
		}
		return lookup.lookupBasicFieldsDefinition(dto);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public IdmFormInstanceDto getBasicFieldsInstance(BaseDto dto) {
		FormProjectionLookup<BaseDto> lookup = (FormProjectionLookup<BaseDto>) formProjectionLookups.getPluginFor(dto.getClass());
		if (lookup == null) {
			return null;
		}
		return lookup.lookupBasicFieldsInstance(dto);
	}

}
