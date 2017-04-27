//package eu.bcvsolutions.idm.core.model.event.processor.policy;
//
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import org.joda.time.DateTime;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Description;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.stereotype.Component;
//import org.springframework.util.Assert;
//
//import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
//import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
//import eu.bcvsolutions.idm.core.api.event.EntityEvent;
//import eu.bcvsolutions.idm.core.api.event.EventResult;
//import eu.bcvsolutions.idm.core.model.entity.IdmAuthorizationPolicy;
//import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
//import eu.bcvsolutions.idm.core.model.entity.IdmRole;
//import eu.bcvsolutions.idm.core.model.event.AuthorizationPolicyEvent.AuthorizationPolicyEventType;
//import eu.bcvsolutions.idm.core.model.repository.IdmAuthorizationPolicyRepository;
//import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;
//import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
//
///**
// * Checks whether policy's group permissions changed. In case of change, all
// * users authentication flags are updated as well, which forces them to re-login
// * into CzechIdM.
// * 
// * @author Jan Helbich
// */
//@Component
//@Description("Checks whether policy's group permissions changed and optionally updates authentication rules.")
//public class AuthorizationPolicyPermissionsChangeProcessor extends CoreEventProcessor<IdmAuthorizationPolicy> {
//
//	private static final Logger LOG = LoggerFactory.getLogger(AuthorizationPolicyPermissionsChangeProcessor.class);
//	private static final String PROCESSOR_NAME = "authorization-policy-permissions-change-processor";
//
//	private final IdmAuthorizationPolicyRepository repository;
//	private final IdmAuthorizationPolicyService service;
//	private final IdmIdentityService identityService;
//
//	@Autowired
//	public AuthorizationPolicyPermissionsChangeProcessor(IdmAuthorizationPolicyRepository repository,
//			IdmAuthorizationPolicyService service, IdmIdentityService identityService) {
//		super(AuthorizationPolicyEventType.CREATE, AuthorizationPolicyEventType.UPDATE);
//		//
//		Assert.notNull(repository);
//		Assert.notNull(service);
//		Assert.notNull(identityService);
//		//
//		this.repository = repository;
//		this.service = service;
//		this.identityService = identityService;
//	}
//
//	@Override
//	public int getOrder() {
//		// runs last
//		return Integer.MAX_VALUE;
//	}
//
//	@Override
//	public String getName() {
//		return PROCESSOR_NAME;
//	}
//
//	@Override
//	public EventResult<IdmAuthorizationPolicy> process(EntityEvent<IdmAuthorizationPolicy> event) {
//		IdmAuthorizationPolicy entity = event.getContent();
//		IdmAuthorizationPolicy persisted = repository.getPersistedPolicy(entity.getId());
//		Assert.notNull(persisted); // updates only - persisted always has to exist
//		//
//		processAuthoritiesChange(entity, persisted);
//		return new DefaultEventResult<>(event, this);
//	}
//
//	private void processAuthoritiesChange(IdmAuthorizationPolicy entity, IdmAuthorizationPolicy persisted) {
//		Set<GrantedAuthority> current = service.getGrantedAuthorities(Collections.singletonList(entity));
//		Set<GrantedAuthority> original = service.getGrantedAuthorities(Collections.singletonList(persisted));
//		// no authorities were changed
//		processAuthoritiesChange(entity, current, original);
//	}
//
//	private void processAuthoritiesChange(IdmAuthorizationPolicy entity, Set<GrantedAuthority> current, Set<GrantedAuthority> original) {
//		if (current.equals(original)) {
//			return;
//		}
//		//
//		Set<GrantedAuthority> added = retainAll(current, original);
//		Set<GrantedAuthority> removed = retainAll(original, current);
//		LOG.debug("Authorities added for [{}]: [{}].", entity.getId(), added);
//		LOG.debug("Authorities removed from [{}]: [{}].", entity.getId(), removed);
//		//
//		updateIdentitiesAuthChangeInRole(entity.getRole());
//	}
//
//	private Set<GrantedAuthority> retainAll(Set<GrantedAuthority> target, Set<GrantedAuthority> diff) {
//		Set<GrantedAuthority> copy = new HashSet<>(target);
//		copy.retainAll(diff);
//		return copy;
//	}
//
//	/**
//	 * Update authority change timestamp on identities in current role.
//	 * 
//	 * @param role
//	 */
//	private void updateIdentitiesAuthChangeInRole(IdmRole role) {
//		List<IdmIdentity> usersInRole = identityService.findAllByRole(role);
//		identityService.updateAuthorityChange(usersInRole, DateTime.now());
//	}
//
//}
