package eu.bcvsolutions.idm.core.service;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.junit.Assert.*;

import org.hibernate.envers.RevisionType;
import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.dto.AuditFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmAudit;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuditService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

public class DefaultAuditServiceTest extends AbstractIntegrationTest {
	
	@Autowired
	private IdmAuditService auditService;
	
	@Autowired
	private IdmRoleRepository roleRepository;
	
	@Autowired
	private IdmIdentityRepository identityRepository;
	
	@Autowired
	private IdmIdentityRoleRepository identityRoleRepository;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Before
	public void before() {
		this.loginAsAdmin("admin");
	}
	
	@After
	public void after() {	
		this.logout();
	}
	
	@Test
	public void roleAuditTestCreateModify() {		
		IdmRole role = saveTransactional(constructRole("audit_test_role"), roleRepository);
		
		List<IdmAudit> result = auditService.findRevisions(IdmRole.class, role.getId());
		
		assertEquals(1, result.size());
		
		role = roleRepository.findOne(role.getId());
		role.setName("audit_test_role_2");
		role.setDescription("desc");
		roleRepository.save(role);
		result = auditService.findRevisions(IdmRole.class, role.getId());

		assertEquals(2, result.size());
		
		IdmAudit audit = result.get(result.size() - 1);
		assertEquals(true, audit.getChangedAttributes().contains("name"));
		assertEquals(true, audit.getChangedAttributes().contains("description"));
		assertEquals(RevisionType.MOD.toString(), audit.getModification());
		
		IdmAudit audit2 = result.get(result.size() - result.size());
		assertEquals(RevisionType.ADD.toString(), audit2.getModification());
		
		assertEquals(true, audit2.getTimestamp() < audit.getTimestamp());
	}
	
	@Test
	@Transactional
	public void testFindRevision() {
		
		IdmRole roleRevision = auditService.findRevision(IdmRole.class, UUID.randomUUID(), 123456l);
		
		assertEquals(null, roleRevision);
		
		List<IdmAudit> resutl = auditService.find(null).getContent();
		
		if (!resutl.isEmpty()) {
			
			Exception ex = null;
			
			for (IdmAudit idmAudit : resutl) {
				try {
					BaseEntity object = (BaseEntity) auditService.findRevision(Class.forName(idmAudit.getType()), idmAudit.getEntityId(), (Long)idmAudit.getId());
					
					assertEquals((UUID)object.getId(), idmAudit.getEntityId());
					
					Class.forName(idmAudit.getType()).cast(object);
					
				} catch (RevisionDoesNotExistException | ClassNotFoundException | ClassCastException e) {
					ex = e;
				}
			}
			
			assertEquals(null, ex);
		}
		
		/*IdmRole roleRevision2 = auditService.getPreviousVersion(roleRevision, (Long)audit.getId());
		assertNotEquals(null, roleRevision2);
		assertEquals("audit_test_role", roleRevision2.getName());*/
		
	}
	
	@Test
	public void identityAuditCreateModify() {
		IdmIdentity identity = this.constructIdentity("aud_test", "test", "test");
		identityRepository.save(identity);
		
		identity = identityRepository.findOne(identity.getId());
		
		IdmRole role = roleRepository.save(constructRole("aud_test_role"));
		
		IdmIdentityRole identityRole = new IdmIdentityRole();
		identityRole.setIdentity(identity);
		identityRole.setRole(role);
		identityRoleRepository.save(identityRole);
		
		List<IdmAudit> result = auditService.findRevisions(IdmIdentity.class, identity.getId());
		assertEquals(2, result.size());

		IdmAudit audit = result.get(result.size() - 1);
		assertEquals(RevisionType.MOD.toString(), audit.getModification());
		assertEquals(true, audit.getChangedAttributes().contains("roles"));
		assertEquals(2, audit.getModifiedEntityNames().size());
		
		assertEquals(true, audit.getModifiedEntityNames().toString().contains("IdmIdentityRole"));
	}
	
	@Test
	public void auditQuickSearch() {
		AuditFilter filter = new AuditFilter();
		filter.setModifier("admin");
		filter.setType(IdmRole.class.getSimpleName());
		
		Pageable pageable = new PageRequest(1, 10);
		
		List<IdmAudit> result = auditService.find(filter, pageable).getContent();
		
		for (IdmAudit idmAudit : result) {
			assertEquals("admin", idmAudit.getModifier());
			assertEquals(IdmRole.class.getSimpleName(), idmAudit.getType());
		}
	}
	
	@Transactional
	private <T extends BaseEntity> T saveTransactional(T entity, AbstractEntityRepository<T, ?> respository) {
		return respository.save(entity);
	}
	
	private IdmRole constructRole(String name) {
		IdmRole role = new IdmRole();
		role.setName(name);
		return role;
	}
	
	private IdmIdentity constructIdentity(String username, String firstName, String secondName) {
		IdmIdentity identity = new IdmIdentity();
		identity.setUsername(username);
		identity.setFirstName(firstName);
		identity.setLastName(secondName);
		return identity;
	}
}
