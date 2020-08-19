package eu.bcvsolutions.idm.core.model.event.processor.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractInitApplicationProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueService;

/**
 * Init product provided role catalogue.
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Component(InitRoleCatalogueProcessor.PROCESSOR_NAME)
@Description("Init product provided role catalogue item 'CzechIdM Roles'. "
		+ "This item will contain all product provided roles (by person). "
		+ "Catalogue item is created, when no other catalogue item exists."
		+ "If this processor is disabled, then catalogue item will not be created "
		+ "and product provided roles will be created without catalogue relation.")
public class InitRoleCatalogueProcessor extends AbstractInitApplicationProcessor {

	public static final String PROCESSOR_NAME = "core-init-role-catalogue-processor";
	private static final String CATALOGUE_CODE = "CzechIdM Roles";
	//
	@Autowired private IdmRoleCatalogueService roleCatalogueService;
	@Autowired private IdmRoleCatalogueRoleService roleCatalogueRoleService;
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<ModuleDescriptorDto> event) {
		return super.conditional(event) 
				&& isInitDataEnabled()
				&& roleCatalogueService.count(null) == 0;
	}
	
	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		// create product provided role catalogue
		IdmRoleCatalogueDto roleCatalogue = new IdmRoleCatalogueDto();
		roleCatalogue.setCode(CATALOGUE_CODE);
		roleCatalogue.setName(CATALOGUE_CODE);
		roleCatalogueService.save(roleCatalogue);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	/**
	 * Register role int product catalogue item.
	 * 
	 * @param role product provided role
	 */
	public void registerRole(IdmRoleDto role) {
		IdmRoleCatalogueDto roleCatalogue = getRoleCatalogue();
		if (roleCatalogue == null) {
			return;
		}
		//
		IdmRoleCatalogueRoleDto catalogueItem = new IdmRoleCatalogueRoleDto();
		catalogueItem.setRoleCatalogue(roleCatalogue.getId());
		catalogueItem.setRole(role.getId());
		roleCatalogueRoleService.save(catalogueItem);
	}
	
	/**
	 * Get catalogue for product roles.
	 * 
	 * @return
	 */
	private IdmRoleCatalogueDto getRoleCatalogue() {
		return roleCatalogueService.getByCode(CATALOGUE_CODE);
	}
	
	@Override
	public int getOrder() {
		// before admin role is created
		return CoreEvent.DEFAULT_ORDER - 50;
	}
}
