package eu.bcvsolutions.idm.core.eav.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import eu.bcvsolutions.idm.core.eav.api.dto.FormProjectionRouteDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormProjectionManager;
import eu.bcvsolutions.idm.core.eav.api.service.FormProjectionRoute;

/**
 * Provides supported form projections.
 * 
 * @author Radek Tomiška
 * @since 10.3.0
 */
public class DefaultFormProjectionManager implements FormProjectionManager {
	
	@Autowired private ApplicationContext context;
	
	@Override
	public List<FormProjectionRouteDto> getSupportedRoutes() {
		return context
			.getBeansOfType(FormProjectionRoute.class)
			.values()
			.stream()
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

}
