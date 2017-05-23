package eu.bcvsolutions.idm;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmContractGuarantee;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmIdentityFormValue;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdentityBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.evaluator.BasePermissionEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.identity.ContractGuaranteeByIdentityContractEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.identity.IdentityContractByIdentityEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.identity.IdentityRoleByIdentityEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.identity.SelfIdentityEvaluator;

/**
 * Initialize demo data for application
 * 
 * @author Radek Tomiška 
 *
 */
@Component
@DependsOn("initApplicationData")
public class InitDemoData implements ApplicationListener<ContextRefreshedEvent> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InitDemoData.class);
	private static final String PARAMETER_DEMO_DATA_ENABLED = "idm.sec.core.demo.data.enabled";
	private static final String PARAMETER_DEMO_DATA_CREATED = "idm.sec.core.demo.data.created";
	public static final String FORM_ATTRIBUTE_PHONE = "phone";
	public static final String FORM_ATTRIBUTE_WWW = "webPages";
	public static final String FORM_ATTRIBUTE_PASSWORD = "password";
	public static final String FORM_ATTRIBUTE_DATETIME = "datetime";
	public static final String DEFAULT_ROLE_NAME = "userRole";
	
	@Autowired
	private InitApplicationData initApplicationData;	
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmTreeNodeService treeNodeService;	
	@Autowired
	private IdmTreeTypeService treeTypeService;
	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	private IdmContractGuaranteeService contractGuaranteeService;
	@Autowired
	private SecurityService securityService;	
	@Autowired
	private ConfigurationService configurationService;	
	@Autowired
	private FormService formService;	
	@Autowired
	private IdmPasswordPolicyService passwordPolicyService;
	@Autowired
	private IdmAuthorizationPolicyService authorizationPolicyService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// init only, when demo data is enabled
		if (configurationService.getBooleanValue(PARAMETER_DEMO_DATA_ENABLED, false)) {
			init();
		}
	}
	
	protected void init() {
		// we need to be ensured admin and and admin role exists.
		initApplicationData.init();
		//
		securityService.setSystemAuthentication();
		//
		try {
			IdmRole superAdminRole = this.roleService.getByName(InitApplicationData.ADMIN_ROLE);
			IdmIdentityDto identityAdmin = this.identityService.getByUsername(InitApplicationData.ADMIN_USERNAME);
			//
			Page<IdmTreeNode> rootsList = treeNodeService.findRoots((UUID) null, new PageRequest(0, 1));
			IdmTreeNode rootOrganization = null;
			if (!rootsList.getContent().isEmpty()) {
				rootOrganization = rootsList.getContent().get(0);
			} else {
				IdmTreeNode organizationRoot = new IdmTreeNode();
				organizationRoot.setCode("root");
				organizationRoot.setName("Organization ROOT");
				organizationRoot.setTreeType(treeTypeService.getByCode(InitApplicationData.DEFAULT_TREE_TYPE));
				this.treeNodeService.save(organizationRoot);
			}
			//
			if (!configurationService.getBooleanValue(PARAMETER_DEMO_DATA_CREATED, false)) {
				LOG.info("Creating demo data ...");		
				//
				// create default password policy for validate
				IdmPasswordPolicy passValidate = null;
				try {
					passValidate = this.passwordPolicyService.getDefaultPasswordPolicy(IdmPasswordPolicyType.VALIDATE);
				} catch (ResultCodeException e) {
					// nothing, password policy for validate not exist
				}
				// default password policy not exist, try to found by name
				if (passValidate == null) {
					passValidate = this.passwordPolicyService.findOneByName("DEFAULT_VALIDATE_POLICY");
				}
				// if password policy still not exist create default password policy
				if (passValidate == null) {
					passValidate = new IdmPasswordPolicy();
					passValidate.setName("DEFAULT_VALIDATE_POLICY");
					passValidate.setDefaultPolicy(true);
					passValidate.setType(IdmPasswordPolicyType.VALIDATE);
					passwordPolicyService.save(passValidate);
				}
				//
				// create default password policy for generate
				IdmPasswordPolicy passGenerate = null;
				try {
					passGenerate = this.passwordPolicyService.getDefaultPasswordPolicy(IdmPasswordPolicyType.GENERATE);
				} catch (ResultCodeException e) {
					// nothing, password policy for generate password not exist
				}
				// try to found password policy by name
				if (passGenerate == null) {
					passGenerate = this.passwordPolicyService.findOneByName("DEFAULT_GENERATE_POLICY");
				}
				// if still not exist create default generate password policy
				if (passGenerate == null) {
					passGenerate = new IdmPasswordPolicy();
					passGenerate.setName("DEFAULT_GENERATE_POLICY");
					passGenerate.setDefaultPolicy(true);
					passGenerate.setType(IdmPasswordPolicyType.GENERATE);
					passGenerate.setMinLowerChar(2);
					passGenerate.setMinNumber(2);
					passGenerate.setMinSpecialChar(2);
					passGenerate.setMinUpperChar(2);
					passGenerate.setMinPasswordLength(8);
					passGenerate.setMaxPasswordLength(12);
					passwordPolicyService.save(passGenerate);
				}
				//
				IdmRole role1 = new IdmRole();
				role1.setName(DEFAULT_ROLE_NAME);
				role1 = this.roleService.save(role1);
				IdmAuthorizationPolicyDto policy = new IdmAuthorizationPolicyDto();
				// add autocomplete data access
				policy.setPermissions(IdmBasePermission.AUTOCOMPLETE);
				policy.setRole(role1.getId());
				policy.setEvaluator(BasePermissionEvaluator.class);
				authorizationPolicyService.save(policy);
				// self policy
				IdmAuthorizationPolicyDto selfPolicy = new IdmAuthorizationPolicyDto();
				selfPolicy.setPermissions(IdmBasePermission.READ, IdentityBasePermission.PASSWORDCHANGE);
				selfPolicy.setRole(role1.getId());
				selfPolicy.setGroupPermission(CoreGroupPermission.IDENTITY.getName());
				selfPolicy.setAuthorizableType(IdmIdentity.class.getCanonicalName());
				selfPolicy.setEvaluator(SelfIdentityEvaluator.class);
				authorizationPolicyService.save(selfPolicy);
				// read identity roles by identity
				IdmAuthorizationPolicyDto identityRolePolicy = new IdmAuthorizationPolicyDto();
				identityRolePolicy.setRole(role1.getId());
				identityRolePolicy.setGroupPermission(CoreGroupPermission.IDENTITYROLE.getName());
				identityRolePolicy.setAuthorizableType(IdmIdentityRole.class.getCanonicalName());
				identityRolePolicy.setEvaluator(IdentityRoleByIdentityEvaluator.class);
				authorizationPolicyService.save(identityRolePolicy);				
				// read identity contracts by identity
				IdmAuthorizationPolicyDto identityContractPolicy = new IdmAuthorizationPolicyDto();
				identityContractPolicy.setRole(role1.getId());
				identityContractPolicy.setGroupPermission(CoreGroupPermission.IDENTITYCONTRACT.getName());
				identityContractPolicy.setAuthorizableType(IdmIdentityContract.class.getCanonicalName());
				identityContractPolicy.setEvaluator(IdentityContractByIdentityEvaluator.class);
				authorizationPolicyService.save(identityContractPolicy);
				// read contract guarantees by identity contract
				IdmAuthorizationPolicyDto contractGuaranteePolicy = new IdmAuthorizationPolicyDto();
				contractGuaranteePolicy.setRole(role1.getId());
				contractGuaranteePolicy.setGroupPermission(CoreGroupPermission.CONTRACTGUARANTEE.getName());
				contractGuaranteePolicy.setAuthorizableType(IdmContractGuarantee.class.getCanonicalName());
				contractGuaranteePolicy.setEvaluator(ContractGuaranteeByIdentityContractEvaluator.class);
				authorizationPolicyService.save(contractGuaranteePolicy);
				//
				LOG.info(MessageFormat.format("Role created [id: {0}]", role1.getId()));
				//
				IdmRole role2 = new IdmRole();
				role2.setName("customRole");
				// TODO: subroles are disabled for now
				//List<IdmRoleComposition> subRoles = new ArrayList<>();
				//subRoles.add(new IdmRoleComposition(role2, superAdminRole));
				//role2.setSubRoles(subRoles);
				role2 = this.roleService.save(role2);
				LOG.info(MessageFormat.format("Role created [id: {0}]", role2.getId()));
				//
				IdmRole roleManager = new IdmRole();
				roleManager.setName("manager");
				roleManager = this.roleService.save(roleManager);
				LOG.info(MessageFormat.format("Role created [id: {0}]", roleManager.getId()));
				//
				//
				IdmIdentityDto identity = new IdmIdentityDto();
				identity.setUsername("tomiska");
				identity.setPassword(new GuardedString("heslo"));
				identity.setFirstName("Radek");
				identity.setLastName("Tomiška");
				identity.setEmail("radek.tomiska@bcvsolutions.eu");
				identity = this.identityService.save(identity);
				LOG.info(MessageFormat.format("Identity created [id: {0}]", identity.getId()));
				IdmIdentityContractDto defaultContract = identityContractService.findAllByIdentity(identity.getId()).get(0);
				//
				IdmIdentityRoleDto identityRole1 = new IdmIdentityRoleDto();
				identityRole1.setIdentityContract(defaultContract.getId());
				identityRole1.setRole(role1.getId());
				identityRole1 = identityRoleService.save(identityRole1);
				//
				IdmIdentityRoleDto identityRole2 = new IdmIdentityRoleDto();
				identityRole2.setIdentityContract(defaultContract.getId());
				identityRole2.setRole(role2.getId());
				identityRole2 = identityRoleService.save(identityRole2);
				//
				IdmIdentityDto identity2 = new IdmIdentityDto();
				identity2.setUsername("svanda");
				identity2.setFirstName("Vít");
				identity2.setPassword(new GuardedString("heslo"));
				identity2.setLastName("Švanda");
				identity2.setEmail("vit.svanda@bcvsolutions.eu");
				identity2 = this.identityService.save(identity2);
				LOG.info(MessageFormat.format("Identity created [id: {0}]", identity2.getId()));
				//
				IdmIdentityDto identity3 = new IdmIdentityDto();
				identity3.setUsername("kopr");
				identity3.setFirstName("Ondrej");
				identity3.setPassword(new GuardedString("heslo"));
				identity3.setLastName("Kopr");
				identity3.setEmail("ondrej.kopr@bcvsolutions.eu");
				identity3 = this.identityService.save(identity3);
				LOG.info(MessageFormat.format("Identity created [id: {0}]", identity3.getId()));
				//
				// get tree type for organization
				IdmTreeType treeType = treeTypeService.getByCode(InitApplicationData.DEFAULT_TREE_TYPE);
				//
				IdmTreeNode organization1 = new IdmTreeNode();
				organization1.setCode("one");
				organization1.setName("Organization One");
				organization1.setParent(rootOrganization);
				organization1.setTreeType(treeType);
				this.treeNodeService.save(organization1);
				//
				IdmTreeNode organization2 = new IdmTreeNode();
				organization2.setCode("two");
				organization2.setName("Organization Two");
				organization2.setCreator("ja");
				organization2.setParent(rootOrganization);
				organization2.setTreeType(treeType);
				this.treeNodeService.save(organization2);
				//
				IdmIdentityContractDto identityWorkPosition = new IdmIdentityContractDto();
				identityWorkPosition.setIdentity(identityAdmin.getId());
				identityWorkPosition.setWorkPosition(organization2.getId());
				identityWorkPosition = identityContractService.save(identityWorkPosition);
				IdmContractGuaranteeDto contractGuarantee = new IdmContractGuaranteeDto();
				contractGuarantee.setIdentityContract(identityWorkPosition.getId());
				contractGuarantee.setGuarantee(identity2.getId());
				contractGuaranteeService.save(contractGuarantee);
				//
				LOG.info("Demo data was created.");
				//				
				configurationService.setBooleanValue(PARAMETER_DEMO_DATA_CREATED, true);
				//
				// demo eav identity form
				
				IdmFormAttribute letter = new IdmFormAttribute();
				letter.setCode("letter");
				letter.setName("Favorite letter");
				letter.setPlaceholder("Character");
				letter.setDescription("Some favorite character");
				letter.setPersistentType(PersistentType.CHAR);
				letter.setRequired(true);
				formService.saveAttribute(IdmIdentity.class, letter);
				
				IdmFormAttribute phone = new IdmFormAttribute();
				phone.setCode(FORM_ATTRIBUTE_PHONE);
				phone.setName("Phone");
				phone.setDescription("Additional identitiy's phone");
				phone.setPersistentType(PersistentType.TEXT);
				formService.saveAttribute(IdmIdentity.class, phone);
				
				IdmFormAttribute description = new IdmFormAttribute();
				description.setCode("description");
				description.setName("Description");
				description.setDescription("Some longer optional text (2000 characters)");
				description.setPersistentType(PersistentType.TEXTAREA);
				formService.saveAttribute(IdmIdentity.class, description);
				
				IdmFormAttribute rich = new IdmFormAttribute();
				rich.setCode("rich");
				rich.setName("RichText");
				rich.setDescription("Some rich text (2000 characters)");
				rich.setPersistentType(PersistentType.RICHTEXTAREA);
				formService.saveAttribute(IdmIdentity.class, rich);
				
				IdmFormAttribute sure = new IdmFormAttribute();
				sure.setCode("sure");
				sure.setName("Registration");
				sure.setPersistentType(PersistentType.BOOLEAN);
				sure.setDefaultValue(Boolean.TRUE.toString());
				formService.saveAttribute(IdmIdentity.class, sure);
				
				IdmFormAttribute intNumber = new IdmFormAttribute();
				intNumber.setCode("intNumber");
				intNumber.setName("Int number");
				intNumber.setPersistentType(PersistentType.INT);
				formService.saveAttribute(IdmIdentity.class, intNumber);
				
				IdmFormAttribute longNumber = new IdmFormAttribute();
				longNumber.setCode("longNumber");
				longNumber.setName("Long number");
				longNumber.setPersistentType(PersistentType.LONG);
				formService.saveAttribute(IdmIdentity.class, longNumber);
				
				IdmFormAttribute doubleNumber = new IdmFormAttribute();
				doubleNumber.setCode("doubleNumber");
				doubleNumber.setName("Double number");
				doubleNumber.setPersistentType(PersistentType.DOUBLE);
				formService.saveAttribute(IdmIdentity.class, doubleNumber);
				
				IdmFormAttribute currency = new IdmFormAttribute();
				currency.setCode("currency");
				currency.setName("Price");
				currency.setPersistentType(PersistentType.CURRENCY);
				formService.saveAttribute(IdmIdentity.class, currency);
				
				IdmFormAttribute date = new IdmFormAttribute();
				date.setCode("date");
				date.setName("Date");
				date.setPersistentType(PersistentType.DATE);
				date.setRequired(true);
				date.setDescription("Important date");
				formService.saveAttribute(IdmIdentity.class, date);
				
				IdmFormAttribute datetime = new IdmFormAttribute();
				datetime.setCode(FORM_ATTRIBUTE_DATETIME);
				datetime.setName("Date and time");
				datetime.setPersistentType(PersistentType.DATETIME);
				formService.saveAttribute(IdmIdentity.class, datetime);
				
				IdmFormAttribute webPages = new IdmFormAttribute();
				webPages.setCode(FORM_ATTRIBUTE_WWW);
				webPages.setName("WWW");
				webPages.setDescription("Favorite web pages (every line in new value)");
				webPages.setPersistentType(PersistentType.TEXT);
				webPages.setMultiple(true);
				formService.saveAttribute(IdmIdentity.class, webPages);
				
				IdmFormAttribute password = new IdmFormAttribute();
				password.setCode(FORM_ATTRIBUTE_PASSWORD);
				password.setName("Custom password");
				password.setPersistentType(PersistentType.TEXT);
				password.setConfidential(true);
				password.setDescription("Test password");
				formService.saveAttribute(IdmIdentity.class, password);
				
				IdmFormAttribute byteArray = new IdmFormAttribute();
				byteArray.setCode("byteArray");
				byteArray.setName("Byte array");
				byteArray.setPersistentType(PersistentType.BYTEARRAY);
				byteArray.setConfidential(false);
				byteArray.setDescription("Test byte array");
				byteArray.setPlaceholder("or image :-)");
				formService.saveAttribute(IdmIdentity.class, byteArray);				
				
				List<IdmIdentityFormValue> values = new ArrayList<>();				
				IdmIdentityFormValue phoneValue = new IdmIdentityFormValue();
				phoneValue.setFormAttribute(phone);
				phoneValue.setStringValue("12345679");
				values.add(phoneValue);
				
				formService.saveValues(identity.getId(), IdmIdentity.class, null, values);
				
				//
				// demo eav role form
				IdmFormAttribute roleExt = new IdmFormAttribute();
				roleExt.setCode("extAttr");
				roleExt.setName("Ext.attr");
				roleExt.setPersistentType(PersistentType.TEXT);
				roleExt.setConfidential(false);
				roleExt.setDescription("Role's custom extended attribute");
				
				formService.saveAttribute(IdmRole.class, roleExt);
				
				//
				// demo eav tree node form
				IdmFormAttribute treeNodeExt = new IdmFormAttribute();
				treeNodeExt.setCode("extAttr");
				treeNodeExt.setName("Ext.attr");
				treeNodeExt.setPersistentType(PersistentType.TEXT);
				treeNodeExt.setConfidential(false);
				treeNodeExt.setDescription("Tree node's custom extended attribute");
				
				formService.saveAttribute(IdmTreeNode.class, treeNodeExt);
				
				//
				// demo eav identity contract's form
				IdmFormAttribute identityContractExt = new IdmFormAttribute();
				identityContractExt.setCode("extAttr");
				identityContractExt.setName("Ext.attr");
				identityContractExt.setPersistentType(PersistentType.TEXT);
				identityContractExt.setConfidential(false);
				identityContractExt.setDescription("Identity contract's custom extended attribute");
				
				formService.saveAttribute(IdmIdentityContract.class, identityContractExt);
			}
		} catch(Exception ex) {
			LOG.warn("Demo data was not created", ex);
		} finally {
			SecurityContextHolder.clearContext();
		}
	}

}
