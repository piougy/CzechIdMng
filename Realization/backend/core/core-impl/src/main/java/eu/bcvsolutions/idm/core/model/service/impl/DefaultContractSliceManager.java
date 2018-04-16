package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.service.ContractSliceManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;

/**
 * Manager for automatic role
 * 
 * @author svandav
 *
 */
@Service("contractSliceManager")
public class DefaultContractSliceManager implements ContractSliceManager {

	@Autowired
	private IdmIdentityContractService contractService;

	@Override
	public IdmIdentityContractDto createContractBySlice(IdmContractSliceDto currentSlice, List<IdmContractSliceDto> slices) {
		Assert.notNull(currentSlice, "Contract slice cannot be null!");
		Assert.notNull(currentSlice.getIdentity());
		Assert.notNull(slices);
		
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		// Contract reuses audit fields from slice
		EntityUtils.copyAuditFields(currentSlice, contract);
		
		// Get valid interval of whole contract
		LocalDate minValidFrom = slices.stream().filter(s -> s.getValidFrom() != null)
				.map(IdmIdentityContractDto::getValidFrom).min(LocalDate::compareTo).orElse(null);
		LocalDate maxValidTill = slices.stream().filter(s -> s.getValidTill() != null)
				.map(IdmIdentityContractDto::getValidTill).max(LocalDate::compareTo).orElse(null);

		contract.setIdentity(currentSlice.getIdentity());
		contract.setMain(currentSlice.isMain());
		contract.setPosition(currentSlice.getPosition());
		contract.setWorkPosition(currentSlice.getWorkPosition());
		contract.setRealmId(currentSlice.getRealmId());
		contract.setState(currentSlice.getState());
		contract.setTrimmed(currentSlice.isTrimmed());
		contract.setValidFrom(currentSlice.getValidFrom());
		contract.setValidTill(currentSlice.getValidTill());
		contract.setExterne(currentSlice.isExterne());
		contract.setDescription(currentSlice.getDescription());
		// Create contract
		return contractService.save(contract);
	}
	

}
