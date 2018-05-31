package eu.bcvsolutions.idm.ic;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.AbstractModuleDescriptor;
import eu.bcvsolutions.idm.core.api.domain.ResultCode;
import eu.bcvsolutions.idm.ic.domain.IcResultCode;

@Component
public class IcModuleDescriptor extends AbstractModuleDescriptor {

	public static final String MODULE_ID = "ic";
	
	@Override
	public String getId() {
		return MODULE_ID;
	}
	
	/**
	 * Its lib "only" now
	 */
	@Override
	public boolean isDisableable() {
		return false;
	}

	@Override
	public List<ResultCode> getResultCodes() {
		return Arrays.asList(IcResultCode.values());
	}
}
