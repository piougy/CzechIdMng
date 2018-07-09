package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.envers.RevisionType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.google.common.collect.ImmutableList;

import eu.bcvsolutions.idm.core.api.audit.dto.IdmAuditDto;
import eu.bcvsolutions.idm.core.api.audit.dto.filter.IdmAuditFilter;
import eu.bcvsolutions.idm.core.api.audit.service.IdmAuditService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Audit
 * 
 * @author Ondrej Kopr
 */
public class DefaultAuditServiceTest extends AbstractIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private IdmAuditService auditService;
	@Autowired private IdmRoleService roleService;
	@Autowired private IdmIdentityService identityService;
	@PersistenceContext private EntityManager entityManager;

	@Before
	public void before() {
		this.loginAsAdmin();
	}

	@After
	public void after() {
		this.logout();
	}

	@Test
	public void roleAuditTestCreateModify() {
		IdmRoleDto role = saveInTransaction(constructRole("audit_test_role"), roleService);

		List<IdmAuditDto> result = auditService.findRevisions(IdmRole.class, role.getId());

		assertEquals(1, result.size());

		role = roleService.get(role.getId());
		role.setName("audit_test_role_2");
		role.setDescription("desc");
		role = saveInTransaction(role, roleService);
		result = auditService.findRevisions(IdmRole.class, role.getId());

		assertEquals(2, result.size());

		IdmAuditDto audit = result.get(result.size() - 1);
		// now is disabled changed attributes TODO: fix envers transaction
		assertEquals(true, audit.getChangedAttributes().contains("name"));
		assertEquals(true, audit.getChangedAttributes().contains("description"));
		assertEquals(RevisionType.MOD.toString(), audit.getModification());

		IdmAuditDto audit2 = result.get(result.size() - result.size());
		assertEquals(RevisionType.ADD.toString(), audit2.getModification());

		assertEquals(true, audit2.getTimestamp() < audit.getTimestamp());
	}

	@Test
	@Transactional
	public void testFindRevision() {

		IdmRole roleRevision = auditService.findRevision(IdmRole.class, UUID.randomUUID(), 123456l);

		assertEquals(null, roleRevision);

		List<IdmAuditDto> result = auditService.find(null).getContent();
		// test only first and second
		try {
			IdmAuditDto idmAudit = result.get(0);
			BaseEntity object = (BaseEntity) auditService.findRevision(Class.forName(idmAudit.getType()),
					idmAudit.getEntityId(), (Long) idmAudit.getId());
			if (object != null) {
				assertEquals((UUID) object.getId(), idmAudit.getEntityId());
	
				Class.forName(idmAudit.getType()).cast(object);
			}
			
			// second
			idmAudit = result.get(1);
			object = (BaseEntity) auditService.findRevision(Class.forName(idmAudit.getType()),
					idmAudit.getEntityId(), (Long) idmAudit.getId());
			if (object != null) {
				assertEquals((UUID) object.getId(), idmAudit.getEntityId());
				Class.forName(idmAudit.getType()).cast(object);
			}
		} catch (ClassNotFoundException e) {
			fail(e.getLocalizedMessage());
		}

		/*
		 * IdmRole roleRevision2 = auditService.getPreviousVersion(roleRevision,
		 * (Long)audit.getId()); assertNotEquals(null, roleRevision2);
		 * assertEquals("audit_test_role", roleRevision2.getName());
		 */

	}

	@Test
	public void diffAuditTest() {
		IdmIdentityDto identity = this.constructIdentity("test_diff", "John", "Doe");
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

		List<IdmAuditDto> result = auditService.findRevisions(IdmIdentity.class, identity.getId());
		assertEquals(3, result.size());

		getTransactionTemplate().execute(new TransactionCallback<Object>() {
			public Object doInTransaction(TransactionStatus transactionStatus) {
				IdmAuditDto idmAudit = result.get(0);
				IdmIdentity version1 = auditService.findVersion(IdmIdentity.class, idmAudit.getEntityId(),
						Long.parseLong(idmAudit.getId().toString()));

				idmAudit = result.get(1);
				IdmIdentity version2 = auditService.findVersion(IdmIdentity.class, idmAudit.getEntityId(),
						Long.parseLong(idmAudit.getId().toString()));

				idmAudit = result.get(2);
				IdmIdentity version3 = auditService.findVersion(IdmIdentity.class, idmAudit.getEntityId(),
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

				// 3 modification from IdmIdentity
				assertEquals(3, diff.size());
				assertTrue(diff.containsKey("firstName"));
				assertTrue(diff.containsKey("lastName"));
				assertTrue(diff.containsKey("email"));

				assertEquals("Leonard", diff.get("firstName"));
				assertEquals("Nimoy", diff.get("lastName"));
				assertEquals("example@example.ex", diff.get("email"));

				// test version #2 with #3
				diff = auditService.getDiffBetweenVersion(parseSerializableToString(result.get(1).getId()),
						parseSerializableToString(result.get(2).getId()));
				
				// 3 modification from IdmIdentity
				assertEquals(3, diff.size());
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
				assertEquals(0, diff.size()); // modified only

				return null;
			}
		});

	}

	@Test
	public void identityAuditCreateModify() {
		IdmIdentityDto identity = this.constructIdentity("aud_test", "test", "test");
		identity = identityService.save(identity);
		identity = identityService.get(identity.getId());
		IdmRoleDto role = roleService.save(constructRole("aud_test_role"));		
		helper.createIdentityRole(identity, role);
		//
		List<IdmAuditDto> result = auditService.findRevisions(IdmIdentity.class, identity.getId());
		assertEquals(1, result.size()); // only one remove audited list

		IdmAuditDto audit = result.get(result.size() - 1);
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
		IdmAuditFilter filter = new IdmAuditFilter();
		filter.setModifier("admin");
		filter.setType(IdmRole.class.getSimpleName());

		Pageable pageable = new PageRequest(0, 10);

		List<IdmAuditDto> result = auditService.find(filter, pageable).getContent();

		for (IdmAuditDto idmAudit : result) {
			assertEquals("admin", idmAudit.getModifier());
			assertEquals(IdmRole.class.getName(), idmAudit.getType());
		}
	}
	
	@Test
	public void createAndEditOneTransaction() {
		String username = "test_user_" + System.currentTimeMillis();
		IdmIdentityDto newIdentity = getTransactionTemplate().execute(new TransactionCallback<IdmIdentityDto>() {
			public IdmIdentityDto doInTransaction(TransactionStatus transactionStatus) {
				IdmIdentityDto identity = new IdmIdentityDto();
				identity.setUsername(username);
				identity.setFirstName(username);
				identity.setLastName(username);
				identity = identityService.save(identity);
				//
				identity.setEmail("example@example.tld");
				identity.setLastName(username + "edit");
				
				return identityService.save(identity);
			}
		});
		assertEquals(newIdentity.getUsername(), username);
		MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
		parameters.put("username", ImmutableList.of(username));
		List<IdmAuditDto> audits = auditService.findEntityWithRelation(IdmIdentity.class, parameters, null).getContent();
		assertEquals(2, audits.size());
		//
		String contractChangedAttribute = audits.get(0).getChangedAttributes();
		assertTrue(contractChangedAttribute.contains("externe"));
		assertTrue(contractChangedAttribute.contains("position"));
		assertTrue(contractChangedAttribute.contains("identity"));
		assertTrue(contractChangedAttribute.contains("disabled"));
		assertTrue(contractChangedAttribute.contains("main"));
		//
		String identityChangedAttribute = audits.get(1).getChangedAttributes();
		assertTrue(identityChangedAttribute.contains("state"));
		assertTrue(identityChangedAttribute.contains("email"));
		assertTrue(identityChangedAttribute.contains("lastName"));
	}
	
	@Test
	public void editAndEditOneTrasaction() {
		String username = "test_user_" + System.currentTimeMillis();
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername(username);
		identity.setFirstName(username);
		identity.setLastName(username);
		identityService.save(identity);
		
		IdmIdentityDto newIdentity = getTransactionTemplate().execute(new TransactionCallback<IdmIdentityDto>() {
			public IdmIdentityDto doInTransaction(TransactionStatus transactionStatus) {
				IdmIdentityDto identity = identityService.getByCode(username);
				identity.setFirstName(username + "--edit");
				identityService.save(identity);
				//
				identity.setEmail("example@example.tld");
				identity.setLastName(username + "edit");
				
				return identityService.save(identity);
			}
		});
		assertEquals(newIdentity.getUsername(), username);
		MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
		parameters.put("username", ImmutableList.of(username));
		List<IdmAuditDto> audits = auditService.findEntityWithRelation(IdmIdentity.class, parameters, null).getContent();
		assertEquals(3, audits.size());
	}
	
	@Test
	public void editAndDeleteOneTrasaction() {
		String username = "test_user_" + System.currentTimeMillis();
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername(username);
		identity.setFirstName(username);
		identity.setLastName(username);
		identityService.save(identity);
		
		IdmIdentityDto newIdentity = getTransactionTemplate().execute(new TransactionCallback<IdmIdentityDto>() {
			public IdmIdentityDto doInTransaction(TransactionStatus transactionStatus) {
				IdmIdentityDto identity = identityService.getByCode(username);
				identity.setFirstName(username + "--edit");
				identity = identityService.save(identity);
				// hibernate send this as one query 
				//
				identityService.delete(identity);
				return null;
			}
		});
		assertEquals(newIdentity, null);
		MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
		parameters.put("username", ImmutableList.of(username));
		List<IdmAuditDto> audits = auditService.findEntityWithRelation(IdmIdentity.class, parameters, null).getContent();
		// add idenity + contract -- delete identity + contract	
		assertEquals(4, audits.size());
	}

	private IdmRoleDto constructRole(String name) {
		IdmRoleDto role = new IdmRoleDto();
		role.setName(name);
		return role;
	}

	private IdmIdentityDto constructIdentity(String username, String firstName, String secondName) {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername(username);
		identity.setFirstName(firstName);
		identity.setLastName(secondName);
		return identity;
	}

	private Long parseSerializableToString(Serializable id) {
		return Long.parseLong(id.toString());
	}
}
