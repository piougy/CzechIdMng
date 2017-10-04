package eu.bcvsolutions.idm.vs.service.api;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.vs.dto.VsAccountDto;
import eu.bcvsolutions.idm.vs.dto.filter.VsAccountFilter;

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
	IcAttribute getIcAttribute(UUID accountId, String name, IdmFormDefinitionDto formDefinition);

	/**
	 * Load attributes for given VsAccount
	 * @param account
	 * @return
	 */
	List<IcAttribute> getIcAttributes(VsAccountDto account);

}
