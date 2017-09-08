package eu.bcvsolutions.idm.vs.service.api;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.impl.IcAttributeImpl;
import eu.bcvsolutions.idm.vs.repository.filter.VsAccountFilter;
import eu.bcvsolutions.idm.vs.service.api.dto.VsAccountDto;

/**
 * Service for accounts in virtual system
 * 
 * @author Svanda
 *
 */
public interface VsAccountService
		extends ReadWriteDtoService<VsAccountDto, VsAccountFilter>, AuthorizableService<VsAccountDto> {

	/**
	 * Find VS account by UID and System ID
	 * 
	 * @param uidValue
	 * @param systemId
	 * @return
	 */
	VsAccountDto findByUidSystem(String uidValue, UUID systemId);

	/**
	 * Load data from extended attribute and create IcAttribute
	 * 
	 * @param accountId
	 * @param name
	 * @return
	 */
	IcAttribute loadIcAttribute(UUID accountId, String name, IdmFormDefinitionDto formDefinition);

	/**
	 * Load attributes for given VsAccount
	 * @param account
	 * @return
	 */
	List<IcAttribute> loadIcAttributes(VsAccountDto account);

}
