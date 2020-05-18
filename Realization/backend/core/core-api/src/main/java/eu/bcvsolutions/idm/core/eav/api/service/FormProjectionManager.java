package eu.bcvsolutions.idm.core.eav.api.service;

import java.util.List;

import eu.bcvsolutions.idm.core.eav.api.dto.FormProjectionRouteDto;

/**
 * Provides supported form projections.
 * 
 * @author Radek Tomiška
 * @since 10.3.0
 */
public interface FormProjectionManager {
	
	/**
	 * Returns supported form projection routes.
	 * 
	 * @return
	 */
	List<FormProjectionRouteDto> getSupportedRoutes();
}
