package eu.bcvsolutions.idm.test.api;

import java.io.InputStream;
import java.util.Collection;
import java.util.stream.Collectors;

import org.activiti.engine.IdentityService;
import org.activiti.engine.impl.bpmn.parser.factory.DefaultActivityBehaviorFactory;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.utils.IdmAuthorityUtils;
import eu.bcvsolutions.idm.core.workflow.api.dto.WorkflowDeploymentDto;
import eu.bcvsolutions.idm.core.workflow.api.service.WorkflowDeploymentService;
import eu.bcvsolutions.idm.test.api.utils.AuthenticationTestUtils;

/**
 * 
 * Super class for activiti workflow tests
 * 
 * @author svandav
 *
 */
@Ignore
public abstract class AbstractWorkflowIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	@Rule
	public ActivitiRule activitiRule;
	@Autowired
	private IdentityService workflowIdentityService;
	@Autowired
	private WorkflowDeploymentService processDeploymentService;
	@Autowired
	private AutowireCapableBeanFactory beanFactory;
	@Autowired
	private ModuleService moduleService;
	@Autowired
	private LookupService lookupService;

    
	/**
	 * Behavior injection from configuration doesn't work - we need to initialize it manually
	 */
	@Before
    public void initBehavior() {
    	ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl)activitiRule.getProcessEngine().getProcessEngineConfiguration();
    	DefaultActivityBehaviorFactory activityBehaviorFactory = getBehaviourFactory();
		beanFactory.autowireBean(activityBehaviorFactory);
		// Evaluate expression in workflow
		activityBehaviorFactory.setExpressionManager(((SpringProcessEngineConfiguration) processEngineConfiguration).getExpressionManager());
		// For catch email
		((SpringProcessEngineConfiguration) processEngineConfiguration).getBpmnParser().setActivityBehaviorFactory(activityBehaviorFactory);
    }
	
	/**
	 * Login as user without authorities given in parameter authorities
	 *
	 * @param user
	 * @param authorities
	 */
	public void loginWithout(String user, String ...authorities) {
		Collection<GrantedAuthority> authoritiesWithout = IdmAuthorityUtils.toAuthorities(moduleService.getAvailablePermissions()).stream().filter(authority -> {
			for (String auth: authorities) {
				if (auth.equals(authority.getAuthority())) {
					return false;
				}
			}
			return true;
		}).collect(Collectors.toList());
		IdmIdentityDto identity = (IdmIdentityDto) lookupService.getDtoLookup(IdmIdentityDto.class).lookup(user);
		SecurityContextHolder.getContext().setAuthentication(new IdmJwtAuthentication(identity, null, authoritiesWithout, "test"));
	}

	
    @Override
	public void loginAsAdmin(String username){
    	IdmIdentityDto identity = (IdmIdentityDto) lookupService.getDtoLookup(IdmIdentityDto.class).lookup(username);
		SecurityContextHolder.getContext().setAuthentication(AuthenticationTestUtils.getSystemAuthentication(identity.getUsername(), identity.getId()));
		workflowIdentityService.setAuthenticatedUserId(username);
	}

	@Override
	public void logout() {
		super.logout();
		workflowIdentityService.setAuthenticatedUserId(null);
	}

	/**
	 * Deploy process by definition file path
	 * @param xmlPath
	 * @return
	 */
	public WorkflowDeploymentDto deployProcess(String xmlPath) {
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(xmlPath);
		return processDeploymentService.create(xmlPath, xmlPath, is);
	}
	
	public abstract DefaultActivityBehaviorFactory getBehaviourFactory();
	
}
