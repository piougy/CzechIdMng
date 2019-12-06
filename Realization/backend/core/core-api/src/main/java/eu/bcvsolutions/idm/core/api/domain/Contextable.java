package eu.bcvsolutions.idm.core.api.domain;

import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;

/**
 * Contextable DTO interface. Keeps additional informations. For example request altered that DTO.
 * 
 * @author Vít Švanda
 *
 */
public interface Contextable extends BaseDto {
	
	Map<String, Object> getContext();
	
	void setContext(Map<String, Object> context);

}