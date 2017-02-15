package eu.bcvsolutions.idm.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.envers.RevisionType;
import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.model.dto.filter.AuditFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmAudit;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuditService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

public class DefaultAuditServiceTest extends AbstractIntegrationTest {

	@Autowired
	private IdmAuditService auditService;

	@Autowired
	private IdmRoleService roleService;

	@Autowired
	private IdmIdentityService identityService;

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
		IdmRole role = saveInTransaction(constructRole("audit_test_role"), roleService);

		List<IdmAudit> result = auditService.findRevisions(IdmRole.class, role.getId());

		assertEquals(1, result.size());

		role = roleService.get(role.getId());
		role.setName("audit_test_role_2");
		role.setDescription("desc");
		roleService.save(role);
		result = auditService.findRevisions(IdmRole.class, role.getId());

		assertEquals(2, result.size());

		IdmAudit audit = result.get(result.size() - 1);
		// now is disabled changed attributes TODO: fix envers transaction
		// assertEquals(true, audit.getChangedAttributes().contains("name"));
		// assertEquals(true, audit.getChangedAttributes().contains("description"));
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

		List<IdmAudit> result = auditService.find(null).getContent();

		if (!result.isEmpty()) {

			Exception ex = null;

			for (IdmAudit idmAudit : result) {
				try {
					BaseEntity object = (BaseEntity) auditService.findRevision(Class.forName(idmAudit.getType()),
							idmAudit.getEntityId(), (Long) idmAudit.getId());

					if (object != null) {
						assertEquals((UUID) object.getId(), idmAudit.getEntityId());

						Class.forName(idmAudit.getType()).cast(object);
					}
				} catch (RevisionDoesNotExistException | ClassNotFoundException | ClassCastException e) {
					ex = e;
				}
			}

			assertEquals(null, ex);
		}

		/*
		 * IdmRole roleRevision2 = auditService.getPreviousVersion(roleRevision,
		 * (Long)audit.getId()); assertNotEquals(null, roleRevision2);
		 * assertEquals("audit_test_role", roleRevision2.getName());
		 */

	}

	@Test
	public void diffAuditTest() {
		IdmIdentity identity = this.constructIdentity("test_diff", "John", "Doe");
		identity = saveInTransaction(identity, identityService);
		// identityRepository.save(identity);

		identity = identityService.get(identity.getId());

		identity.setEmail("example@example.ex");
		identity.setFirstName("Leonard");
		identity.setLastName("Nimoy");
		identity = saveInTransaction(identity, identityService);
		// identityRepository.save(identity);

		identity = identityService.get(identity.getId());
		identity.setEmail(null);
		identity.setFirstName("John");
		identity.setLastName("Doe");
		identity = saveInTransaction(identity, identityService);

		List<IdmAudit> result = auditService.findRevisions(IdmIdentity.class, identity.getId());
		assertEquals(3, result.size());

		getTransactionTemplate().execute(new TransactionCallback<Object>() {
			public Object doInTransaction(TransactionStatus transactionStatus) {
				IdmAudit idmAudit = result.get(0);
				IdmIdentity version1 = auditService.getVersion(IdmIdentity.class, idmAudit.getEntityId(),
						Long.parseLong(idmAudit.getId().toString()));

				idmAudit = result.get(1);
				IdmIdentity version2 = auditService.getVersion(IdmIdentity.class, idmAudit.getEntityId(),
						Long.parseLong(idmAudit.getId().toString()));

				idmAudit = result.get(2);
				IdmIdentity version3 = auditService.getVersion(IdmIdentity.class, idmAudit.getEntityId(),
						Long.parseLong(idmAudit.getId().toString()));

				// sample test to default value
				assertEquals(version1.getFirstName(), "John");
				assertEquals(version1.getLastName(), "Doe");

				assertEquals(version2.getFirstName(), "Leonard");
				assertEquals(version2.getLastName(), "Nimoy");
				assertEquals(version2.getEmail(), "example@example.ex");

				assertEquals(version3.getFirstName(), "John");
				assertEquals(version3.getLastName(), "Doe");
				assertNull(version3.getEmail());

				// get diff between version
				// test version #1 with #2
				Map<String, Object> diff = auditService.getDiffBetweenVersion(
						parseSerializableToString(result.get(0).getId()),
						parseSerializableToString(result.get(1).getId()));

				// 3 modification from IdmIdentity and 3 modification from AbstractEntity
				assertEquals(6, diff.size());
				assertTrue(diff.containsKey("firstName"));
				assertTrue(diff.containsKey("lastName"));
				assertTrue(diff.containsKey("email"));

				assertEquals("Leonard", diff.get("firstName"));
				assertEquals("Nimoy", diff.get("lastName"));
				assertEquals("example@example.ex", diff.get("email"));

				// test version #2 with #3
				diff = auditService.getDiffBetweenVersion(parseSerializableToString(result.get(1).getId()),
						parseSerializableToString(result.get(2).getId()));
				
				// 3 modification from IdmIdentity and 1 from abstract entity (date of modified)
				assertEquals(4, diff.size());
				assertTrue(diff.containsKey("firstName"));
				assertTrue(diff.containsKey("lastName"));
				assertTrue(diff.containsKey("email"));

				assertEquals("John", diff.get("firstName"));
				assertEquals("Doe", diff.get("lastName"));
				assertEquals(null, diff.get("email"));

				// final test version #1 with #3
				diff = auditService.getDiffBetweenVersion(parseSerializableToString(result.get(0).getId()),
						parseSerializableToString(result.get(2).getId()));
				// all diff values are from AbstractEntity modifier, modified date, modifier id
				assertEquals(3, diff.size());

				return null;
			}
		});

	}

	@Test
	public void identityAuditCreateModify() {
		IdmIdentity identity = this.constructIdentity("aud_test", "test", "test");
		identityService.save(identity);

		identity = identityService.get(identity.getId());

		IdmRole role = roleService.save(constructRole("aud_test_role"));

		IdmIdentityRole identityRole = new IdmIdentityRole();
		identityRole.setIdentity(identity);
		identityRole.setRole(role);
		identityRoleRepository.save(identityRole);

		List<IdmAudit> result = auditService.findRevisions(IdmIdentity.class, identity.getId());
		assertEquals(1, result.size()); // only one remove audited list

		IdmAudit audit = result.get(result.size() - 1);
		assertEquals(RevisionType.ADD.toString(), audit.getModification());
		// TODO: list aren't audited
		// assertEquals(true, audit.getChangedAttributes().contains("roles"));
		// assertEquals(2, audit.getModifiedEntityNames().size());
		//
		// assertEquals(true,
		// audit.getModifiedEntityNames().toString().contains("IdmIdentityRole"));
	}

	@Test
	public void auditQuickSearch() {
		AuditFilter filter = new AuditFilter();
		filter.setModifier("admin");
		filter.setType(IdmRole.class.getSimpleName());

		Pageable pageable = new PageRequest(0, 10);

		List<IdmAudit> result = auditService.find(filter, pageable).getContent();

		for (IdmAudit idmAudit : result) {
			assertEquals("admin", idmAudit.getModifier());
			assertEquals(IdmRole.class.getSimpleName(), idmAudit.getType());
		}
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

	private Long parseSerializableToString(Serializable id) {
		return Long.parseLong(id.toString());
	}
}
