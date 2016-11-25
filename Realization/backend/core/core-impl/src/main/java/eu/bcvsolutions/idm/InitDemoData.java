package eu.bcvsolutions.idm;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.google.common.base.Charsets;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityFormValue;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleComposition;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeTypeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.eav.domain.PersistentType;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.eav.service.api.FormService;
import eu.bcvsolutions.idm.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.security.api.service.SecurityService;

/**
 * Initialize demo data for application
 * 
 * @author Radek Tomiška 
 *
 */
@Component
@DependsOn("initApplicationData")
public class InitDemoData implements ApplicationListener<ContextRefreshedEvent> {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InitDemoData.class);
	private static final String PARAMETER_DEMO_DATA_CREATED = "idm.sec.core.demo.data";
	
	@Autowired
	private InitApplicationData initApplicationData;
	
	@Autowired
	private IdmIdentityService identityService;

	@Autowired
	private IdmRoleService roleService;

	@Autowired
	private IdmIdentityRoleRepository identityRoleRepository;

	@Autowired
	private IdmTreeNodeRepository treeNodeRepository;
	
	@Autowired
	private IdmTreeTypeRepository treeTypeRepository;

	@Autowired
	private IdmIdentityContractRepository identityContractRepository;

	@Autowired
	private SecurityService securityService;
	
	@Autowired
	private IdmConfigurationService configurationService;
	
	@Autowired
	private FormService formService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		init();
	}
	
	protected void init() {
		// we need to be ensured admin and and admin role exists.
		initApplicationData.init();
		//
		// TODO: runAs
		SecurityContextHolder.getContext().setAuthentication(
				new IdmJwtAuthentication("[SYSTEM]", null, securityService.getAllAvailableAuthorities()));
		try {
			IdmRole superAdminRole = this.roleService.getByName(InitApplicationData.ADMIN_ROLE);
			IdmIdentity identityAdmin = this.identityService.getByName(InitApplicationData.ADMIN_USERNAME);
			//
			Page<IdmTreeNode> rootsList = treeNodeRepository.findChildren(null, null, new PageRequest(0, 1));
			IdmTreeNode rootOrganization = null;
			if (!rootsList.getContent().isEmpty()) {
				rootOrganization = rootsList.getContent().get(0);
			} else {
				IdmTreeNode organizationRoot = new IdmTreeNode();
				organizationRoot.setCode("root");
				organizationRoot.setName("Organization ROOT");
				organizationRoot.setTreeType(treeTypeRepository.findOneByCode(InitApplicationData.DEFAULT_TREE_TYPE));
				this.treeNodeRepository.save(organizationRoot);
			}
			//
			if (!configurationService.getBooleanValue(PARAMETER_DEMO_DATA_CREATED, false)) {
				log.info("Creating demo data ...");				
				//
				IdmRole role1 = new IdmRole();
				role1.setName("userRole");
				role1 = this.roleService.save(role1);
				log.info(MessageFormat.format("Role created [id: {0}]", role1.getId()));
				//
				IdmRole role2 = new IdmRole();
				role2.setName("customRole");
				List<IdmRoleComposition> subRoles = new ArrayList<>();
				subRoles.add(new IdmRoleComposition(role2, superAdminRole));
				role2.setSubRoles(subRoles);
				role2 = this.roleService.save(role2);
				role2.setApproveAddWorkflow("approveRoleByUserTomiska");
				log.info(MessageFormat.format("Role created [id: {0}]", role2.getId()));
				//
				IdmRole roleManager = new IdmRole();
				roleManager.setName("manager");
				roleManager = this.roleService.save(roleManager);
				log.info(MessageFormat.format("Role created [id: {0}]", roleManager.getId()));
				//
				//
				IdmIdentity identity = new IdmIdentity();
				identity.setUsername("tomiska");
				identity.setPassword("heslo".getBytes(Charsets.UTF_8));
				identity.setFirstName("Radek");
				identity.setLastName("Tomiška");
				identity.setEmail("radek.tomiska@bcvsolutions.eu");
				identity = this.identityService.save(identity);
				log.info(MessageFormat.format("Identity created [id: {0}]", identity.getId()));
				//
				IdmIdentityRole identityRole1 = new IdmIdentityRole();
				identityRole1.setIdentity(identity);
				identityRole1.setRole(role1);
				identityRoleRepository.save(identityRole1);
				//
				IdmIdentityRole identityRole2 = new IdmIdentityRole();
				identityRole2.setIdentity(identity);
				identityRole2.setRole(role2);
				identityRoleRepository.save(identityRole2);
				//
				IdmIdentity identity2 = new IdmIdentity();
				identity2.setUsername("svanda");
				identity2.setFirstName("Vít");
				identity2.setPassword("heslo".getBytes(Charsets.UTF_8));
				identity2.setLastName("Švanda");
				identity2.setEmail("vit.svanda@bcvsolutions.eu");
				identity2 = this.identityService.save(identity2);
				log.info(MessageFormat.format("Identity created [id: {0}]", identity2.getId()));
				//
				IdmIdentity identity3 = new IdmIdentity();
				identity3.setUsername("kopr");
				identity3.setFirstName("Ondrej");
				identity3.setPassword("heslo".getBytes(Charsets.UTF_8));
				identity3.setLastName("Kopr");
				identity3.setEmail("ondrej.kopr@bcvsolutions.eu");
				identity3 = this.identityService.save(identity3);
				log.info(MessageFormat.format("Identity created [id: {0}]", identity3.getId()));
				//
				// get tree type for organization
				IdmTreeType treeType = treeTypeRepository.findOneByCode(InitApplicationData.DEFAULT_TREE_TYPE);
				//
				IdmTreeNode organization1 = new IdmTreeNode();
				organization1.setCode("one");
				organization1.setName("Organization One");
				organization1.setParent(rootOrganization);
				organization1.setTreeType(treeType);
				this.treeNodeRepository.save(organization1);
				//
				IdmTreeNode organization2 = new IdmTreeNode();
				organization2.setCode("two");
				organization2.setName("Organization Two");
				organization2.setCreator("ja");
				organization2.setParent(rootOrganization);
				organization2.setTreeType(treeType);
				this.treeNodeRepository.save(organization2);
				//
				IdmIdentityContract identityWorkingPosition = new IdmIdentityContract();
				identityWorkingPosition.setIdentity(identityAdmin);
				identityWorkingPosition.setGuarantee(identity2);
				identityWorkingPosition.setWorkingPosition(organization2);
				identityContractRepository.save(identityWorkingPosition);
				//
				log.info("Demo data was created.");
				//				
				configurationService.setBooleanValue(PARAMETER_DEMO_DATA_CREATED, true);
				//
				// test idendentity form
				List<IdmFormAttribute> attributes = new ArrayList<>();
				
				IdmFormAttribute letter = new IdmFormAttribute();
				letter.setName("letter");
				letter.setDisplayName("Favorite letter");
				letter.setDescription("Favorite character");
				letter.setPersistentType(PersistentType.CHAR);
				letter.setRequired(true);
				attributes.add(letter);
				
				IdmFormAttribute phone = new IdmFormAttribute();
				phone.setName("phone");
				phone.setDisplayName("Phone");
				phone.setDescription("Additional identitiy's phone");
				phone.setPersistentType(PersistentType.TEXT);
				attributes.add(phone);
				
				IdmFormAttribute description = new IdmFormAttribute();
				description.setName("description");
				description.setDisplayName("Description");
				description.setDescription("Some longer optional text (2000 characters)");
				description.setPersistentType(PersistentType.TEXTAREA);
				attributes.add(description);
				
				IdmFormAttribute sure = new IdmFormAttribute();
				sure.setName("sure");
				sure.setDisplayName("Registration");
				sure.setPersistentType(PersistentType.BOOLEAN);
				sure.setDefaultValue(Boolean.TRUE.toString());
				attributes.add(sure);
				
				IdmFormAttribute intNumber = new IdmFormAttribute();
				intNumber.setName("intNumber");
				intNumber.setDisplayName("Int number");
				intNumber.setPersistentType(PersistentType.INT);
				attributes.add(intNumber);
				
				IdmFormAttribute longNumber = new IdmFormAttribute();
				longNumber.setName("longNumber");
				longNumber.setDisplayName("Long number");
				longNumber.setPersistentType(PersistentType.LONG);
				attributes.add(longNumber);
				
				IdmFormAttribute doubleNumber = new IdmFormAttribute();
				doubleNumber.setName("doubleNumber");
				doubleNumber.setDisplayName("Double number");
				doubleNumber.setPersistentType(PersistentType.DOUBLE);
				attributes.add(doubleNumber);
				
				IdmFormAttribute currency = new IdmFormAttribute();
				currency.setName("currency");
				currency.setDisplayName("Price");
				currency.setPersistentType(PersistentType.CURRENCY);
				attributes.add(currency);
				
				IdmFormAttribute date = new IdmFormAttribute();
				date.setName("date");
				date.setDisplayName("Date");
				date.setPersistentType(PersistentType.DATE);
				date.setRequired(true);
				date.setDescription("Important date");
				attributes.add(date);
				
				IdmFormAttribute datetime = new IdmFormAttribute();
				datetime.setName("datetime");
				datetime.setDisplayName("Date and time");
				datetime.setPersistentType(PersistentType.DATETIME);
				attributes.add(datetime);
				
				IdmFormAttribute webPages = new IdmFormAttribute();
				webPages.setName("webPages");
				webPages.setDisplayName("WWW");
				webPages.setDescription("Favorite web pages (every line in new value)");
				webPages.setPersistentType(PersistentType.TEXT);
				webPages.setMultiple(true);
				attributes.add(webPages);
				
				IdmFormAttribute password = new IdmFormAttribute();
				password.setName("password");
				password.setDisplayName("Custom password");
				password.setPersistentType(PersistentType.TEXT);
				password.setConfidential(true);
				password.setDescription("Test password");
				attributes.add(password);
				
				IdmFormDefinition formDefinition = formService.createDefinition(IdmIdentity.class.getCanonicalName(), null, attributes);
				
				List<IdmIdentityFormValue> values = new ArrayList<>();				
				IdmIdentityFormValue phoneValue = new IdmIdentityFormValue();
				phoneValue.setFormAttribute(phone);
				phoneValue.setStringValue("12345679");
				values.add(phoneValue);
				
				formService.saveValues(identity, formDefinition, values);
			}
		} finally {
			SecurityContextHolder.clearContext();
		}
	}

}
