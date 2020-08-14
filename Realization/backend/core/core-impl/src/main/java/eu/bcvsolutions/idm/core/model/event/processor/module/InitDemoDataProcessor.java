package eu.bcvsolutions.idm.core.model.event.processor.module;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitApplicationData;
import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractInitApplicationProcessor;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormProjectionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormProjectionService;
import eu.bcvsolutions.idm.core.eav.service.impl.IdentityFormProjectionRoute;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Initialize demo data for application:
 * - demo eav (all available persistent types)
 * - demo users are created with product roles assigned
 * - demo tree structure is created
 * 
 * @author Radek Tomiška 
 * @since 10.5.0
 */
@Component(InitDemoDataProcessor.PROCESSOR_NAME)
@Description("Initialize demo data for application.")
public class InitDemoDataProcessor extends AbstractInitApplicationProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InitDemoDataProcessor.class);
	public static final String PROCESSOR_NAME = "core-init-demo-data-processor";
	//
	public static final String PARAMETER_DEMO_DATA_ENABLED = "idm.sec.core.demo.data.enabled";
	public static final String PARAMETER_DEMO_DATA_CREATED = "idm.sec.core.demo.data.created";
	public static final String FORM_ATTRIBUTE_PHONE = "phone";
	public static final String FORM_ATTRIBUTE_WWW = "webPages";
	public static final String FORM_ATTRIBUTE_UUID = "uuid";
	public static final String FORM_ATTRIBUTE_PASSWORD = "password";
	public static final String FORM_ATTRIBUTE_DATETIME = "datetime";
	public static final String FORM_ATTRIBUTE_DATE = "date";
	public static final String FORM_ATTRIBUTE_LETTER = "letter";
	//
	@Autowired private ObjectMapper mapper;
	@Autowired private LookupService lookupService;
	@Autowired private FormService formService;
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmRoleService roleService;
	@Autowired private RoleConfiguration roleConfiguration;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmTreeNodeService treeNodeService;	
	@Autowired private IdmTreeTypeService treeTypeService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmContractGuaranteeService contractGuaranteeService;
	@Autowired private ConfigurationService configurationService;	
	@Autowired private IdmFormProjectionService formProjectionService;	
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<ModuleDescriptorDto> event) {
		// Create demo data after application starts for the first time
		return super.conditional(event) 
				&& configurationService.getBooleanValue(PARAMETER_DEMO_DATA_ENABLED, true)
				&& !configurationService.getBooleanValue(PARAMETER_DEMO_DATA_CREATED, false);
	}
	
	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		LOG.info("Creating demo data.");
		//
		// form attributes at first => identity will be created with default values
		createFormAttributes();
		//
		// get default tree type and root (by init data defensively)
		IdmTreeTypeDto treeType = treeTypeService.getByCode(InitApplicationData.DEFAULT_TREE_TYPE);
		Page<IdmTreeNodeDto> rootsList = treeNodeService.findRoots(treeType.getId(), PageRequest.of(0, 1));
		IdmTreeNodeDto rootOrganization = null;
		if (!rootsList.getContent().isEmpty()) {
			rootOrganization = rootsList.getContent().get(0);
		} else {
			rootOrganization = new IdmTreeNodeDto();
			rootOrganization.setCode("root");
			rootOrganization.setName("Organization");
			rootOrganization.setTreeType(treeTypeService.getByCode(InitOrganizationProcessor.DEFAULT_TREE_TYPE).getId());
			rootOrganization = treeNodeService.save(rootOrganization);
		}
		//
		IdmRoleDto role2 = createRequestableCustomRole();
		//
		IdmRoleDto userManagerRole = roleConfiguration.getUserManagerRole();
		if (userManagerRole == null) {
			userManagerRole = new IdmRoleDto();
			userManagerRole.setCode("userManagerRole");
			userManagerRole.setCanBeRequested(true);
			userManagerRole = roleService.save(userManagerRole);
			//
			LOG.info("Role created [id: {}]", userManagerRole.getId());	
		}
		//
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername("john");
		identity.setPassword(new GuardedString("john"));
		identity.setFirstName("John");
		identity.setLastName("Doe");
		identity.setEmail("john.doe@bcvsolutions.eu");
		identity = identityService.save(identity);
		List<IdmFormValueDto> values = new ArrayList<>();				
		IdmFormValueDto phoneValue = new IdmFormValueDto();
		phoneValue.setFormAttribute(formService.getAttribute(identity.getClass(), FORM_ATTRIBUTE_PHONE).getId());
		phoneValue.setStringValue("12345679");
		values.add(phoneValue);
		formService.saveValues(identity.getId(), IdmIdentity.class, null, values);
		LOG.info("Identity created [id: {}]", identity.getId());
		//
		// create prime contract
		IdmIdentityContractDto identityContract = identityContractService.getPrimeContract(identity.getId());
		if (identityContract == null) {
			identityContract = identityContractService.prepareMainContract(identity.getId());
			identityContract = identityContractService.save(identityContract);
		}
		//
		IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
		identityRole.setIdentityContract(identityContract.getId());
		identityRole.setRole(role2.getId());
		identityRole = identityRoleService.save(identityRole);
		//
		IdmIdentityDto identity2 = new IdmIdentityDto();
		identity2.setUsername("jane");
		identity2.setFirstName("Jane");
		identity2.setPassword(new GuardedString("jane"));
		identity2.setLastName("Doe");
		identity2.setEmail("jane.doe@bcvsolutions.eu");
		identity2 = identityService.save(identity2);
		LOG.info("Identity created [id: {}]", identity2.getId());
		//
		IdmIdentityDto identity3 = new IdmIdentityDto();
		identity3.setUsername("novak");
		identity3.setFirstName("Jan");
		identity3.setPassword(new GuardedString("novak"));
		identity3.setLastName("Novák");
		identity3.setEmail("jan.novak@bcvsolutions.eu");
		identity3 = identityService.save(identity3);
		LOG.info("Identity created [id: {}]", identity3.getId());
		//
		IdmTreeNodeDto organization1 = new IdmTreeNodeDto();
		organization1.setCode("one");
		organization1.setName("Organization One");
		organization1.setParent(rootOrganization.getId());
		organization1.setTreeType(treeType.getId());
		organization1 = treeNodeService.save(organization1);
		//
		IdmTreeNodeDto organization2 = new IdmTreeNodeDto();
		organization2.setCode("two");
		organization2.setName("Organization Two");
		organization2.setParent(rootOrganization.getId());
		organization2.setTreeType(treeType.getId());
		organization2 = treeNodeService.save(organization2);
		//
		// form projection for externe user
		IdmFormProjectionDto externeProjection = new IdmFormProjectionDto();
		externeProjection.setOwnerType(lookupService.getOwnerType(IdmIdentity.class));
		externeProjection.setCode("identity-externe");
		externeProjection.setRoute(IdentityFormProjectionRoute.PROJECTION_NAME);
		try {
			externeProjection.setBasicFields( // TODO: better setter
					mapper.writeValueAsString(
							Lists.newArrayList(
								IdmIdentity_.username.getName(), 
								IdmIdentity_.firstName.getName(),
								IdmIdentity_.lastName.getName()
							)
					)
			);
		} catch (Exception ex) {
			LOG.warn("Demo form proction will show all basic attributes.", ex);
		}
		externeProjection.getProperties().put(IdentityFormProjectionRoute.PARAMETER_LOAD_ASSIGNED_ROLES, false); // not available now in product projection
		externeProjection = formProjectionService.save(externeProjection);
		IdmIdentityDto externeIdentity = new IdmIdentityDto();
		externeIdentity.setUsername("externeUser");
		externeIdentity.setFirstName("František");
		externeIdentity.setPassword(new GuardedString("externeUser"));
		externeIdentity.setLastName("Nový");
		externeIdentity.setEmail("frantisek.novy@bcvsolutions.eu");
		externeIdentity.setFormProjection(externeProjection.getId());
		externeIdentity = identityService.save(externeIdentity);
		LOG.info("Externe identity created [id: {}]", externeIdentity.getId());
		//
		// helpdesk
		IdmRoleDto helpdeskRole = roleConfiguration.getHelpdeskRole();
		if (helpdeskRole != null) {
			identity = new IdmIdentityDto();
			identity.setUsername("helpdesk");
			identity.setPassword(new GuardedString("helpdesk"));
			identity.setFirstName("Helpdesk");
			identity.setLastName("User");
			identity.setEmail("hepldesk@bcvsolutions.eu");
			identity.setDescription("Helpdesk - can read other users and change passwords.");
			identity = identityService.save(identity);
			// create prime contract
			identityContract = identityContractService.getPrimeContract(identity.getId());
			if (identityContract == null) {
				identityContract = identityContractService.prepareMainContract(identity.getId());
				identityContract = identityContractService.save(identityContract);
			}
			//
			identityRole = new IdmIdentityRoleDto();
			identityRole.setIdentityContract(identityContract.getId());
			identityRole.setRole(helpdeskRole.getId());
			identityRole = identityRoleService.save(identityRole);
		}
		//
		// user manager - role created defensively above
		identity = new IdmIdentityDto();
		identity.setUsername("manager");
		identity.setPassword(new GuardedString("manager"));
		identity.setFirstName("Manager");
		identity.setLastName("User");
		identity.setEmail("manager@bcvsolutions.eu");
		identity.setDescription("Manager with subordinates (externeUser)");
		identity = identityService.save(identity);
		// create prime contract
		identityContract = identityContractService.getPrimeContract(identity.getId());
		if (identityContract == null) {
			identityContract = identityContractService.prepareMainContract(identity.getId());
			identityContract = identityContractService.save(identityContract);
		}
		//
		identityRole = new IdmIdentityRoleDto();
		identityRole.setIdentityContract(identityContract.getId());
		identityRole.setRole(userManagerRole.getId());
		identityRole = identityRoleService.save(identityRole);
		//
		identityContract = identityContractService.getPrimeContract(externeIdentity.getId());
		if (identityContract == null) {
			identityContract = identityContractService.prepareMainContract(identity.getId());
			identityContract.setExterne(true);
			identityContract = identityContractService.save(identityContract);
		} else {
			identityContract.setExterne(true);
			identityContract = identityContractService.save(identityContract);
		}
		// externe - set manager
		IdmContractGuaranteeDto guarantee = new IdmContractGuaranteeDto();
		guarantee.setIdentityContract(identityContract.getId());
		guarantee.setGuarantee(identity.getId());
		contractGuaranteeService.save(guarantee);
		//
		// role manager
		IdmRoleDto roleManagerRole = roleConfiguration.getRoleManagerRole();
		if (roleManagerRole != null) {
			identity = new IdmIdentityDto();
			identity.setUsername("roleManager");
			identity.setPassword(new GuardedString("roleManager"));
			identity.setFirstName("Role");
			identity.setLastName("Manager");
			identity.setEmail("role.manager@bcvsolutions.eu");
			identity.setDescription("Role manager - can edit managed roles.");
			identity = identityService.save(identity);
			// create prime contract
			identityContract = identityContractService.getPrimeContract(identity.getId());
			if (identityContract == null) {
				identityContract = identityContractService.prepareMainContract(identity.getId());
				identityContract = identityContractService.save(identityContract);
			}
			//
			identityRole = new IdmIdentityRoleDto();
			identityRole.setIdentityContract(identityContract.getId());
			identityRole.setRole(roleManagerRole.getId());
			identityRole = identityRoleService.save(identityRole);
		}
		//
		LOG.info("Demo data was created.");
		//				
		configurationService.setBooleanValue(PARAMETER_DEMO_DATA_CREATED, true);
		//
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		// after init data is created
		return CoreEvent.DEFAULT_ORDER + 3000;
	}
	
	private IdmRoleDto createRequestableCustomRole() {
		IdmRoleDto role = new IdmRoleDto();
		role.setCode("customRole");
		role.setCanBeRequested(true);
		role = roleService.save(role);
		//
		LOG.info("Role created [id: {}]", role.getId());
		return role;
	}
	
	private void createFormAttributes() {
		//
		// demo eav identity form			
		IdmFormAttributeDto letter = new IdmFormAttributeDto();
		letter.setCode(FORM_ATTRIBUTE_LETTER);
		letter.setName("Favorite letter");
		letter.setPlaceholder("Character");
		letter.setDescription("Some favorite character");
		letter.setPersistentType(PersistentType.CHAR);
		letter.setRequired(true);
		letter = formService.saveAttribute(IdmIdentity.class, letter);
		
		IdmFormAttributeDto phone = new IdmFormAttributeDto();
		phone.setCode(FORM_ATTRIBUTE_PHONE);
		phone.setName("Phone");
		phone.setDescription("Additional identitiy's phone");
		phone.setPersistentType(PersistentType.TEXT);
		phone = formService.saveAttribute(IdmIdentity.class, phone);
		
		IdmFormAttributeDto description = new IdmFormAttributeDto();
		description.setCode("description");
		description.setName("Description");
		description.setDescription("Some longer optional text (2000 characters)");
		description.setPersistentType(PersistentType.TEXT);
		description.setFaceType(BaseFaceType.TEXTAREA);
		description = formService.saveAttribute(IdmIdentity.class, description);
		
		IdmFormAttributeDto rich = new IdmFormAttributeDto();
		rich.setCode("rich");
		rich.setName("RichText");
		rich.setDescription("Some rich text (2000 characters)");
		rich.setPersistentType(PersistentType.TEXT);
		description.setFaceType(BaseFaceType.RICHTEXTAREA);
		rich = formService.saveAttribute(IdmIdentity.class, rich);
		
		IdmFormAttributeDto sure = new IdmFormAttributeDto();
		sure.setCode("sure");
		sure.setName("Registration");
		sure.setPersistentType(PersistentType.BOOLEAN);
		sure = formService.saveAttribute(IdmIdentity.class, sure);
		
		IdmFormAttributeDto intNumber = new IdmFormAttributeDto();
		intNumber.setCode("intNumber");
		intNumber.setName("Int number");
		intNumber.setPersistentType(PersistentType.INT);
		intNumber = formService.saveAttribute(IdmIdentity.class, intNumber);
		
		IdmFormAttributeDto longNumber = new IdmFormAttributeDto();
		longNumber.setCode("longNumber");
		longNumber.setName("Long number");
		longNumber.setPersistentType(PersistentType.LONG);
		longNumber = formService.saveAttribute(IdmIdentity.class, longNumber);
		
		IdmFormAttributeDto doubleNumber = new IdmFormAttributeDto();
		doubleNumber.setCode("doubleNumber");
		doubleNumber.setName("Double number");
		doubleNumber.setPersistentType(PersistentType.DOUBLE);
		doubleNumber = formService.saveAttribute(IdmIdentity.class, doubleNumber);
		
		IdmFormAttributeDto currency = new IdmFormAttributeDto();
		currency.setCode("currency");
		currency.setName("Price");
		currency.setPersistentType(PersistentType.DOUBLE);
		currency.setFaceType(BaseFaceType.CURRENCY);			
		currency = formService.saveAttribute(IdmIdentity.class, currency);
		
		IdmFormAttributeDto date = new IdmFormAttributeDto();
		date.setCode(FORM_ATTRIBUTE_DATE);
		date.setName("Date");
		date.setPersistentType(PersistentType.DATE);
		date.setRequired(true);
		date.setDescription("Important date");
		date = formService.saveAttribute(IdmIdentity.class, date);
		
		IdmFormAttributeDto datetime = new IdmFormAttributeDto();
		datetime.setCode(FORM_ATTRIBUTE_DATETIME);
		datetime.setName("Date and time");
		datetime.setPersistentType(PersistentType.DATETIME);
		datetime = formService.saveAttribute(IdmIdentity.class, datetime);
		
		IdmFormAttributeDto uuid = new IdmFormAttributeDto();
		uuid.setCode(FORM_ATTRIBUTE_UUID);
		uuid.setName("UUID");
		uuid.setDescription("Some uuid value");
		uuid.setPersistentType(PersistentType.UUID);
		uuid = formService.saveAttribute(IdmIdentity.class, uuid);
		
		IdmFormAttributeDto webPages = new IdmFormAttributeDto();
		webPages.setCode(FORM_ATTRIBUTE_WWW);
		webPages.setName("WWW");
		webPages.setDescription("Favorite web pages (every line in new value)");
		webPages.setPersistentType(PersistentType.TEXT);
		webPages.setMultiple(true);
		webPages = formService.saveAttribute(IdmIdentity.class, webPages);
		
		IdmFormAttributeDto password = new IdmFormAttributeDto();
		password.setCode(FORM_ATTRIBUTE_PASSWORD);
		password.setName("Custom password");
		password.setPersistentType(PersistentType.TEXT);
		password.setConfidential(true);
		password.setDescription("Test password");
		password = formService.saveAttribute(IdmIdentity.class, password);
		
		IdmFormAttributeDto byteArray = new IdmFormAttributeDto();
		byteArray.setCode("byteArray");
		byteArray.setName("Byte array");
		byteArray.setPersistentType(PersistentType.BYTEARRAY);
		byteArray.setConfidential(false);
		byteArray.setDescription("Test byte array");
		byteArray.setPlaceholder("or image :-)");
		byteArray = formService.saveAttribute(IdmIdentity.class, byteArray);				
		//
		// demo eav role form
		IdmFormAttributeDto roleExt = new IdmFormAttributeDto();
		roleExt.setCode("extAttr");
		roleExt.setName("Ext.attr");
		roleExt.setPersistentType(PersistentType.TEXT);
		roleExt.setConfidential(false);
		roleExt.setDescription("Role's custom extended attribute");
		roleExt = formService.saveAttribute(IdmRole.class, roleExt);
		//
		// demo eav tree node form
		IdmFormAttributeDto treeNodeExt = new IdmFormAttributeDto();
		treeNodeExt.setCode("extAttr");
		treeNodeExt.setName("Ext.attr");
		treeNodeExt.setPersistentType(PersistentType.TEXT);
		treeNodeExt.setConfidential(false);
		treeNodeExt.setDescription("Tree node's custom extended attribute");
		treeNodeExt = formService.saveAttribute(IdmTreeNode.class, treeNodeExt);
		//
		// demo eav identity contract's form
		IdmFormAttributeDto identityContractExt = new IdmFormAttributeDto();
		identityContractExt.setCode("extAttr");
		identityContractExt.setName("Ext.attr");
		identityContractExt.setPersistentType(PersistentType.TEXT);
		identityContractExt.setConfidential(false);
		identityContractExt.setDescription("Identity contract's custom extended attribute");
		
		identityContractExt = formService.saveAttribute(IdmIdentityContract.class, identityContractExt);
	}
}
