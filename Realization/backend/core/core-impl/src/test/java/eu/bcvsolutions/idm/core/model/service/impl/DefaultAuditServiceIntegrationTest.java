package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.envers.RevisionType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.audit.dto.IdmAuditDto;
import eu.bcvsolutions.idm.core.api.audit.dto.IdmAuditEntityDto;
import eu.bcvsolutions.idm.core.api.audit.dto.filter.IdmAuditFilter;
import eu.bcvsolutions.idm.core.api.audit.service.IdmAuditService;
import eu.bcvsolutions.idm.core.api.domain.TransactionContextHolder;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.security.api.authentication.AuthenticationManager;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.exception.IdmAuthenticationException;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Audit tests
 * - test transactional id
 * 
 * @author Ondrej Kopr
 * @author Radek Tomiška
 */
public class DefaultAuditServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private IdmAuditService auditService;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private AuthenticationManager authenticationManager;
	@PersistenceContext
	private EntityManager entityManager;

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
		role.setCode("audit_test_role_2");
		role.setDescription("desc");
		role = saveInTransaction(role, roleService);
		result = auditService.findRevisions(IdmRole.class, role.getId());

		assertEquals(2, result.size());

		IdmAuditDto audit = result.get(result.size() - 1);
		// now is disabled changed attributes TODO: fix envers transaction
		assertEquals(true, audit.getChangedAttributes().contains("code"));
		assertEquals(true, audit.getChangedAttributes().contains("description"));
		assertEquals(RevisionType.MOD.toString(), audit.getModification());

		IdmAuditDto audit2 = result.get(result.size() - result.size());
		assertEquals(RevisionType.ADD.toString(), audit2.getModification());

		Assert.assertFalse(audit2.getTimestamp() > audit.getTimestamp());
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
		getHelper().createIdentityRole(identity, role);
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

		Pageable pageable = PageRequest.of(0, 10);

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
		IdmAuditFilter auditFilter = new IdmAuditFilter();
		auditFilter.setOwnerCode(username);
		auditFilter.setOwnerType(IdmIdentity.class.getCanonicalName());
		
		List<IdmAuditDto> audits = auditService.findEntityWithRelation(
				auditFilter, 
				PageRequest.of(0, Integer.MAX_VALUE, Sort.by("id"))).getContent();
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
		IdmAuditFilter auditFilter = new IdmAuditFilter();
		auditFilter.setOwnerCode(username);
		auditFilter.setOwnerType(IdmIdentity.class.getCanonicalName());
		List<IdmAuditDto> audits = auditService.findEntityWithRelation(auditFilter, null).getContent();
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
		IdmAuditFilter auditFilter = new IdmAuditFilter();
		auditFilter.setOwnerCode(username);
		auditFilter.setOwnerType(IdmIdentity.class.getCanonicalName());
		List<IdmAuditDto> audits = auditService.findEntityWithRelation(auditFilter, null).getContent();
		// add idenity + contract -- delete identity + contract	
		assertEquals(4, audits.size());
	}

	@Test
	public void testLoginAuditWithPagination() {
		String password = "password-" + System.currentTimeMillis();
		GuardedString passwordAsGuardedString = new GuardedString(password);
		IdmIdentityDto identity = getHelper().createIdentity(passwordAsGuardedString);
		
		LoginDto loginDto = new LoginDto(identity.getUsername(), passwordAsGuardedString);
		authenticationManager.authenticate(loginDto);
		authenticationManager.authenticate(loginDto);
		authenticationManager.authenticate(loginDto);
		
		IdmAuditFilter filter = new IdmAuditFilter();
		filter.setOwnerId(identity.getId().toString());
		PageRequest pageable = PageRequest.of(0, 1);
		Page<IdmAuditDto> findLogin = getTransactionTemplate().execute(new TransactionCallback<Page<IdmAuditDto>>() {
			@Override
			public Page<IdmAuditDto> doInTransaction(TransactionStatus status) {
				return auditService.findLogin(filter, pageable);
			}
		});
		
		assertEquals(3, findLogin.getTotalElements());
		assertEquals(1, findLogin.getContent().size());
	}

	@Test
	public void testLoginAuditWithPaginationAndFailed() {
		this.logout();

		String password = "password-" + System.currentTimeMillis();
		GuardedString passwordAsGuardedString = new GuardedString(password);
		IdmIdentityDto identity = getHelper().createIdentity(passwordAsGuardedString);
		
		LoginDto loginDto = new LoginDto(identity.getUsername(), passwordAsGuardedString);
		authenticationManager.authenticate(loginDto);
		this.logout();
		authenticationManager.authenticate(loginDto);
		this.logout();
		authenticationManager.authenticate(loginDto);
		this.logout();
		loginDto = new LoginDto(identity.getUsername(), new GuardedString("test-" + System.currentTimeMillis()));
		
		try {
			authenticationManager.authenticate(loginDto);
			fail();
		} catch (IdmAuthenticationException e) {
			// Success
		} catch (Exception e) {
			fail();
		}

		this.logout();

		try {
			authenticationManager.authenticate(loginDto);
			fail();
		} catch (IdmAuthenticationException e) {
			// Success
		} catch (Exception e) {
			fail();
		}

		this.logout();

		IdmAuditFilter filter = new IdmAuditFilter();
		filter.setOwnerId(identity.getId().toString());
		PageRequest pageable = PageRequest.of(0, 1);

		Page<IdmAuditDto> findLogin = getTransactionTemplate().execute(new TransactionCallback<Page<IdmAuditDto>>() {
			@Override
			public Page<IdmAuditDto> doInTransaction(TransactionStatus status) {
				return auditService.findLogin(filter, pageable);
			}
		});
		

		assertEquals(5, findLogin.getTotalElements());
		assertEquals(1, findLogin.getContent().size());
	}

	@Test
	public void testLoginAuditWithoutPagination() {
		String password = "password-" + System.currentTimeMillis();
		GuardedString passwordAsGuardedString = new GuardedString(password);
		IdmIdentityDto identity = getHelper().createIdentity(passwordAsGuardedString);
		LoginDto loginDto = new LoginDto(identity.getUsername(), passwordAsGuardedString);
		authenticationManager.authenticate(loginDto);
		authenticationManager.authenticate(loginDto);
		authenticationManager.authenticate(loginDto);
		authenticationManager.authenticate(loginDto);
		authenticationManager.authenticate(loginDto);

		getTransactionTemplate().execute(new TransactionCallback<Object>() {
			@Override
			public Object doInTransaction(TransactionStatus status) {
				IdmAuditFilter filter = new IdmAuditFilter();
				filter.setOwnerId(identity.getId().toString());
				Page<IdmAuditDto> findLogin = auditService.findLogin(filter, null);
				assertEquals(5, findLogin.getTotalElements());
				assertEquals(5, findLogin.getContent().size());
				
				return null;
			}
		});
		
	}

	@Test
	public void testLoginAuditWithPasswordChange() {
		String password = "password-" + System.currentTimeMillis();
		GuardedString passwordAsGuardedString = new GuardedString(password);
		IdmIdentityDto identity = getHelper().createIdentity(passwordAsGuardedString);

		LoginDto loginDto = new LoginDto(identity.getUsername(), passwordAsGuardedString);
		authenticationManager.authenticate(loginDto);

		String newPassword = "new-password-" + System.currentTimeMillis();
		this.loginAsAdmin();
		getTransactionTemplate().execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus status) {
				PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
				passwordChangeDto.setAll(true);
				passwordChangeDto.setNewPassword(new GuardedString(newPassword));
				identityService.passwordChange(identity, passwordChangeDto);
				return null;
			}
		});
		this.logout();

		loginDto = new LoginDto(identity.getUsername(), passwordAsGuardedString);
		authenticationManager.authenticate(loginDto);

		IdmAuditFilter filter = new IdmAuditFilter();
		filter.setOwnerId(identity.getId().toString());
		List<IdmAuditDto> logins = getTransactionTemplate().execute(new TransactionCallback<List<IdmAuditDto>>() {
			@Override
			public List<IdmAuditDto> doInTransaction(TransactionStatus status) {
				return auditService.findLogin(filter, null).getContent();
			}
		});

		assertEquals(2, logins.size());
	}

	@Test
	public void testFilteringByChangedAttributes() {
		IdmIdentityDto identity = getHelper().createIdentity();
		
		identity.setFirstName(getHelper().createName());
		identity.setLastName(getHelper().createName());
		
		identity = identityService.save(identity);

		IdmAuditFilter filter = new IdmAuditFilter();
		filter.setModification("MOD");
		filter.setEntityId(identity.getId());
		filter.setChangedAttributesList(Lists.newArrayList(IdmIdentity_.description.getName()));
		
		List<IdmAuditDto> content = auditService.find(filter, null).getContent();
		assertEquals(0, content.size());

		filter.setChangedAttributesList(Lists.newArrayList(IdmIdentity_.firstName.getName()));
		content = auditService.find(filter, null).getContent();
		IdmAuditDto auditDto = content.get(0);
		assertEquals(1, content.size());
		assertTrue(auditDto.getChangedAttributes().contains(IdmIdentity_.firstName.getName()));

		filter.setChangedAttributesList(Lists.newArrayList(IdmIdentity_.firstName.getName(), IdmIdentity_.lastName.getName()));
		content = auditService.find(filter, null).getContent();
		IdmAuditDto nextAuditDto = content.get(0);
		assertEquals(1, content.size());
		assertEquals(auditDto.getId(), content.get(0).getId());	
		assertTrue(nextAuditDto.getChangedAttributes().contains(IdmIdentity_.firstName.getName()));
		assertTrue(nextAuditDto.getChangedAttributes().contains(IdmIdentity_.lastName.getName()));

		filter.setChangedAttributesList(Lists.newArrayList(IdmIdentity_.firstName.getName(), IdmIdentity_.lastName.getName(), IdmIdentity_.description.getName()));
		content = auditService.find(filter, null).getContent();
		nextAuditDto = content.get(0);
		assertEquals(1, content.size());
		assertEquals(auditDto.getId(), content.get(0).getId());	
		assertTrue(nextAuditDto.getChangedAttributes().contains(IdmIdentity_.firstName.getName()));
		assertTrue(nextAuditDto.getChangedAttributes().contains(IdmIdentity_.lastName.getName()));
		assertFalse(nextAuditDto.getChangedAttributes().contains(IdmIdentity_.description.getName()));
	}

	@Test
	public void testFilteringByChangedAttributesMoreRecords() {
		IdmIdentityDto identity = getHelper().createIdentity();
		
		identity.setFirstName(getHelper().createName());
		identity.setLastName(getHelper().createName());
		
		identity = identityService.save(identity);
	
		identity.setEmail("email@example.tld");
		
		identity = identityService.save(identity);
		
		identity.setEmail("emailChanged@example.tld");
		identity.setLastName(getHelper().createName());
		
		identity = identityService.save(identity);
		
		identity.setPhone("123456789");
		
		identity = identityService.save(identity);

		IdmAuditFilter filter = new IdmAuditFilter();
		filter.setModification("MOD");
		filter.setEntityId(identity.getId());
		filter.setChangedAttributesList(Lists.newArrayList(IdmIdentity_.description.getName()));
		
		List<IdmAuditDto> content = auditService.find(filter, null).getContent();
		assertEquals(0, content.size());

		filter.setChangedAttributesList(Lists.newArrayList(IdmIdentity_.firstName.getName()));
		content = auditService.find(filter, null).getContent();
		IdmAuditDto auditDto = content.get(0);
		assertEquals(1, content.size());
		assertTrue(auditDto.getChangedAttributes().contains(IdmIdentity_.firstName.getName()));

		filter.setChangedAttributesList(Lists.newArrayList(IdmIdentity_.lastName.getName()));
		content = auditService.find(filter, null).getContent();
		assertEquals(2, content.size());
		
		filter.setChangedAttributesList(Lists.newArrayList(IdmIdentity_.firstName.getName(), IdmIdentity_.lastName.getName()));
		content = auditService.find(filter, null).getContent();
		assertEquals(2, content.size());
		
		filter.setChangedAttributesList(Lists.newArrayList(IdmIdentity_.email.getName(), IdmIdentity_.description.getName()));
		content = auditService.find(filter, null).getContent();
		assertEquals(2, content.size());

		filter.setChangedAttributesList(Lists.newArrayList(IdmIdentity_.email.getName()));
		content = auditService.find(filter, null).getContent();
		assertEquals(2, content.size());

		filter.setChangedAttributesList(Lists.newArrayList(IdmIdentity_.lastName.getName()));
		content = auditService.find(filter, null).getContent();
		assertEquals(2, content.size());
	}

	@Test
	public void testToDtoWithoutVersion() {
		IdmIdentityDto identity = getHelper().createIdentity();
		identity.setDescription("description-" + System.currentTimeMillis());
		identity = identityService.save(identity);

		IdmAuditFilter filter = new IdmAuditFilter();
		filter.setEntityId(identity.getId());
		List<IdmAuditDto> audits = auditService.find(filter, null).getContent();

		assertEquals(2, audits.size());

		for (IdmAuditDto audit : audits) {
			assertFalse(audit instanceof IdmAuditEntityDto);
		}

		filter = new IdmAuditFilter();
		filter.setEntityId(identity.getId());
		filter.setWithVersion(Boolean.FALSE);
		audits = auditService.find(filter, null).getContent();

		assertEquals(2, audits.size());

		for (IdmAuditDto audit : audits) {
			assertFalse(audit instanceof IdmAuditEntityDto);
		}
	}

	@Test
	public void testToDtoWithVersion() {
		IdmIdentityDto identity = getHelper().createIdentity();
		String newDescription = "description-" + System.currentTimeMillis();
		identity.setDescription(newDescription);
		identity = identityService.save(identity);

		IdmAuditFilter filter = new IdmAuditFilter();
		filter.setEntityId(identity.getId());
		filter.setWithVersion(Boolean.TRUE);
		List<IdmAuditDto> audits = auditService.find(filter, null).getContent();

		assertEquals(2, audits.size());

		for (IdmAuditDto audit : audits) {
			assertTrue(audit instanceof IdmAuditEntityDto);
			IdmAuditEntityDto auditEntity = (IdmAuditEntityDto) audit;
			assertNotNull(auditEntity.getEntity());

			// Check attribute for MOD
			if (auditEntity.getModification().equals("MOD")) {
				assertTrue(auditEntity.getEntity().containsKey(IdmIdentity_.description.getName()));
				Object description = auditEntity.getEntity().get(IdmIdentity_.description.getName());
				assertNotNull(description);
				assertEquals(newDescription, description);
			}
		}
	}

	@Test
	public void testFilterById() {
		IdmIdentityDto identity = getHelper().createIdentity();

		IdmAuditFilter filter = new IdmAuditFilter();
		filter.setEntityId(identity.getId());
		List<IdmAuditDto> audits = auditService.find(filter, null).getContent();
		assertEquals(1, audits.size());
		IdmAuditDto auditDto = audits.get(0);

		filter = new IdmAuditFilter();
		filter.setId(auditDto.getId());
		audits = auditService.find(filter, null).getContent();

		assertEquals(1, audits.size());
		assertEquals(auditDto.getId(), audits.get(0).getId());
	}

	@Test
	public void testFilterByText() {
		IdmIdentityDto identity = getHelper().createIdentity();

		IdmAuditFilter filter = new IdmAuditFilter();
		filter.setEntityId(identity.getId());
		List<IdmAuditDto> audits = auditService.find(filter, null).getContent();
		assertEquals(1, audits.size());
		IdmAuditDto auditDto = audits.get(0);

		// This is little bit dangerous because is possible found by text another audit logs.
		filter = new IdmAuditFilter();
		filter.setText(auditDto.getId().toString());
		audits = auditService.find(filter, null).getContent();

		assertEquals(1, audits.size());
		assertEquals(auditDto.getId(), audits.get(0).getId());
	}

	@Test
	public void testFilterByFrom() {
		ZonedDateTime now = ZonedDateTime.now();
		IdmIdentityDto identity = getHelper().createIdentity();

		IdmAuditFilter filter = new IdmAuditFilter();
		filter.setEntityId(identity.getId());
		filter.setFrom(now);
		List<IdmAuditDto> audits = auditService.find(filter, null).getContent();
		assertEquals(1, audits.size());
		IdmAuditDto auditDto = audits.get(0);
		assertEquals(identity.getId(), auditDto.getEntityId());
	}

	@Test
	public void testFilterByFromAndTill() {
		ZonedDateTime from = ZonedDateTime.now();
		IdmIdentityDto identity = getHelper().createIdentity();
		ZonedDateTime tillOne = ZonedDateTime.now();
		identity.setDescription("description-" + System.currentTimeMillis());
		identity = identityService.save(identity);
		ZonedDateTime tillTwo = ZonedDateTime.now();

		IdmAuditFilter filter = new IdmAuditFilter();
		filter.setEntityId(identity.getId());
		filter.setFrom(from);
		filter.setTill(tillOne);
		List<IdmAuditDto> audits = auditService.find(filter, null).getContent();
		assertEquals(1, audits.size());
		IdmAuditDto auditDto = audits.get(0);
		assertEquals(identity.getId(), auditDto.getEntityId());

		filter = new IdmAuditFilter();
		filter.setEntityId(identity.getId());
		filter.setFrom(from);
		filter.setTill(tillTwo);
		audits = auditService.find(filter, null).getContent();
		assertEquals(2, audits.size());

		for (IdmAuditDto aud : audits) {
			assertEquals(identity.getId(), aud.getEntityId());
		}
	}

	@Test
	public void testFilterByOwnerId() {
		IdmIdentityDto identity = this.getHelper().createIdentity();

		IdmAuditFilter filter = new IdmAuditFilter();
		filter.setOwnerId(identity.getId().toString());
		List<IdmAuditDto> audits = auditService.find(filter, null).getContent();

		for (IdmAuditDto audit : audits) {
			assertEquals(identity.getId().toString(), audit.getOwnerId());
			assertEquals(identity.getUsername(), audit.getOwnerCode());
			assertEquals(IdmIdentity.class.getName(), audit.getOwnerType());
		}
	}

	@Test
	public void testFilterByOwnerCode() {
		IdmIdentityDto identity = this.getHelper().createIdentity();

		IdmAuditFilter filter = new IdmAuditFilter();
		filter.setOwnerCode(identity.getUsername());
		List<IdmAuditDto> audits = auditService.find(filter, null).getContent();

		for (IdmAuditDto audit : audits) {
			assertEquals(identity.getId().toString(), audit.getOwnerId());
			assertEquals(identity.getUsername(), audit.getOwnerCode());
			assertEquals(IdmIdentity.class.getName(), audit.getOwnerType());
		}
	}

	@Test
	public void testFilterBySubownerId() {
		IdmIdentityDto identity = this.getHelper().createIdentity();
		IdmRoleDto role = this.getHelper().createRole();
		IdmIdentityRoleDto identityRole = this.getHelper().createIdentityRole(identity, role);

		IdmAuditFilter filter = new IdmAuditFilter();
		filter.setSubOwnerId(role.getId().toString());
		List<IdmAuditDto> audits = auditService.find(filter, null).getContent();
		assertEquals(1, audits.size());

		IdmAuditDto auditDto = audits.get(0);

		assertEquals(identity.getId().toString(), auditDto.getOwnerId());

		assertEquals(role.getId().toString(), auditDto.getSubOwnerId());

		assertEquals(identityRole.getId(), auditDto.getEntityId());
	}

	@Test
	public void testFilterBySubownerCode() {
		IdmIdentityDto identity = this.getHelper().createIdentity();
		IdmRoleDto role = this.getHelper().createRole();
		IdmIdentityRoleDto identityRole = this.getHelper().createIdentityRole(identity, role);

		IdmAuditFilter filter = new IdmAuditFilter();
		filter.setSubOwnerCode(role.getCode());
		List<IdmAuditDto> audits = auditService.find(filter, null).getContent();
		assertEquals(1, audits.size());

		IdmAuditDto auditDto = audits.get(0);

		assertEquals(identity.getUsername(), auditDto.getOwnerCode());

		assertEquals(role.getCode(), auditDto.getSubOwnerCode());

		assertEquals(identityRole.getId(), auditDto.getEntityId());
	}

	@Test
	public void testFilterBySubownerType() {
		IdmIdentityDto identity = this.getHelper().createIdentity();
		IdmRoleDto role = this.getHelper().createRole();
		IdmIdentityRoleDto identityRole = this.getHelper().createIdentityRole(identity, role);

		IdmAuditFilter filter = new IdmAuditFilter();
		filter.setSubOwnerId(role.getId().toString());
		filter.setSubOwnerType(IdmRole.class.getName());
		List<IdmAuditDto> audits = auditService.find(filter, null).getContent();
		assertEquals(1, audits.size());

		IdmAuditDto auditDto = audits.get(0);

		assertEquals(IdmIdentity.class.getName(), auditDto.getOwnerType());

		assertEquals(IdmRole.class.getName(), auditDto.getSubOwnerType());

		assertEquals(identityRole.getId(), auditDto.getEntityId());
	}
	
	@Test
	public void testFillTransactionalIdCreateIdentity() {
		TransactionContextHolder.setContext(TransactionContextHolder.createEmptyContext()); //start transaction
		UUID transactionId = TransactionContextHolder.getContext().getTransactionId();
		Assert.assertNotNull(transactionId);
		//	
		IdmIdentityDto identity = getHelper().createIdentity(); // with password
		Assert.assertEquals(transactionId, identity.getTransactionId());
		// default contract and password has to have the same transaction id
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		Assert.assertEquals(transactionId, contract.getTransactionId());
		// default password
		IdmPasswordDto password = getHelper().getPassword(identity);
		Assert.assertEquals(transactionId, password.getTransactionId());
		
		// audit for identity, contract and password with the same transaction id
		IdmAuditFilter filter = new IdmAuditFilter();
		filter.setTransactionId(transactionId);
		List<IdmAuditDto> audits = auditService.find(filter, null).getContent();
		Assert.assertEquals(3, audits.size());
		//
		Assert.assertTrue(audits.stream().anyMatch(a -> a.getEntityId().equals(identity.getId())));
		Assert.assertTrue(audits.stream().anyMatch(a -> a.getEntityId().equals(contract.getId())));
		Assert.assertTrue(audits.stream().anyMatch(a -> a.getEntityId().equals(password.getId())));
	}

	private IdmRoleDto constructRole(String name) {
		IdmRoleDto role = new IdmRoleDto();
		role.setCode(name);
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
