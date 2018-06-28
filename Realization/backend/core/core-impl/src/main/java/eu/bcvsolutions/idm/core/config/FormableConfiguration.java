package eu.bcvsolutions.idm.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.bcvsolutions.idm.core.api.config.domain.PrivateIdentityConfiguration;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.eav.entity.IdmForm;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormValue;
import eu.bcvsolutions.idm.core.eav.repository.AbstractFormValueRepository;
import eu.bcvsolutions.idm.core.eav.service.impl.AbstractFormValueService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmContractSlice;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmContractSliceFormValue;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmIdentityContractFormValue;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmIdentityFormValue;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmRoleFormValue;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmTreeNodeFormValue;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Configuration for eav
 * 
 * @author Radek Tomiška
 *
 */
@Configuration
public class FormableConfiguration {

	/**
	 * Eav attributes for identity
	 * 
	 * @param repository
	 * @param confidentialStorage
	 * @return
	 */
	@Bean
	public AbstractFormValueService<IdmIdentity, IdmIdentityFormValue> identityFormValueService(
			AbstractFormValueRepository<IdmIdentity, IdmIdentityFormValue> repository, 
			ConfidentialStorage confidentialStorage) {
		
		return new AbstractFormValueService<IdmIdentity, IdmIdentityFormValue>(repository, confidentialStorage) {
			
			@Autowired private PrivateIdentityConfiguration identityConfiguration;
			
			/**
			 * Identity form values supports authorization policies
			 */
			@Override
			public AuthorizableType getAuthorizableType() {
				if (!identityConfiguration.isFormAttributesSecured()) {
					return null;
				}
				return new AuthorizableType(CoreGroupPermission.FORMVALUE, getEntityClass());
			}
		};
	}
	
	
	/**
	 * Eav attributes for role
	 * 
	 * @param repository
	 * @param confidentialStorage
	 * @return
	 */
	@Bean
	public AbstractFormValueService<IdmRole, IdmRoleFormValue> roleFormValueService(
			AbstractFormValueRepository<IdmRole, IdmRoleFormValue> repository, 
			ConfidentialStorage confidentialStorage) {
		return new AbstractFormValueService<IdmRole, IdmRoleFormValue>(repository, confidentialStorage) {};
	}
	
	/**
	 * Eav attributes for tree node
	 * 
	 * @param repository
	 * @param confidentialStorage
	 * @return
	 */
	@Bean
	public AbstractFormValueService<IdmTreeNode, IdmTreeNodeFormValue> treeNodeFormValueService(
			AbstractFormValueRepository<IdmTreeNode, IdmTreeNodeFormValue> repository, 
			ConfidentialStorage confidentialStorage) {
		return new AbstractFormValueService<IdmTreeNode, IdmTreeNodeFormValue>(repository, confidentialStorage) {};
	}
	
	/**
	 * Eav attributes for identity contracts
	 * 
	 * @param repository
	 * @param confidentialStorage
	 * @return
	 */
	@Bean
	public AbstractFormValueService<IdmIdentityContract, IdmIdentityContractFormValue> identityContractFormValueService(
			AbstractFormValueRepository<IdmIdentityContract, IdmIdentityContractFormValue> repository, 
			ConfidentialStorage confidentialStorage) {
		return new AbstractFormValueService<IdmIdentityContract, IdmIdentityContractFormValue>(repository, confidentialStorage) {};
	}
	
	/**
	 * Eav attributes for contract time slices
	 * 
	 * @param repository
	 * @param confidentialStorage
	 * @return
	 */
	@Bean
	public AbstractFormValueService<IdmContractSlice, IdmContractSliceFormValue> contractSliceFormValueService(
			AbstractFormValueRepository<IdmContractSlice, IdmContractSliceFormValue> repository, 
			ConfidentialStorage confidentialStorage) {
		return new AbstractFormValueService<IdmContractSlice, IdmContractSliceFormValue>(repository, confidentialStorage) {};
	}
	
	
	/**
	 * Eav attributes for common eav form
	 * - persists filters, configurable properties, etc.
	 * 
	 * @param repository
	 * @param confidentialStorage
	 * @return
	 */
	@Bean
	public AbstractFormValueService<IdmForm, IdmFormValue> formValueService(
			AbstractFormValueRepository<IdmForm, IdmFormValue> repository, 
			ConfidentialStorage confidentialStorage) {
		return new AbstractFormValueService<IdmForm, IdmFormValue>(repository, confidentialStorage) {};
	}
}
