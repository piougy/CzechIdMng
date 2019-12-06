package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.acc.dto.SysAttributeControlledValueDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;

/**
 * Filter - Controlled value for attribute DTO. Is using in the provisioning
 * merge.
 * 
 * @author Vít Švanda
 *
 */
public class SysAttributeControlledValueFilter extends DataFilter {

	private UUID attributeMappingId;
	private Boolean historicValue;
	
	public SysAttributeControlledValueFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public SysAttributeControlledValueFilter(MultiValueMap<String, Object> data) {
		super(SysAttributeControlledValueDto.class, data);
	}

	public UUID getAttributeMappingId() {
		return attributeMappingId;
	}

	public void setAttributeMappingId(UUID attributeMappingId) {
		this.attributeMappingId = attributeMappingId;
	}

	public Boolean getHistoricValue() {
		return historicValue;
	}

	public void setHistoricValue(Boolean historicValue) {
		this.historicValue = historicValue;
	}
}
