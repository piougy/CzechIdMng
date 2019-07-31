package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.BooleanUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleComparison;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.DuplicateRolesDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleFormAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormValue_;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Assigned roles integration tests
 * - referential integrity
 * - role deduplicate
 * 
 * @author Radek Tomiška
 * @author Ondrej Kopr
 *
 */
public class DefaultIdmIdentityRoleServiceIntegrationTest extends AbstractIntegrationTest {

	private static String ATTRIBUTE_ONE = "attrOne";
	private static String ATTRIBUTE_TWO = "attrTwo";
	private static String ATTRIBUTE_THREE = "attrThree";
	private static Long ATTRIBUTE_ONE_DEFAULT_VALUE = 1122337788l;
	private static Boolean B_ROLE_DUPLICATED = Boolean.FALSE;
	private static Boolean A_ROLE_DUPLICATED = Boolean.TRUE;
	private static Boolean NONE_ROLE_DUPLICATED = null;
	private static boolean MANUALLY_DUPLICATED = true;
	private static boolean MANUALLY_NOT_DUPLICATED = false;
	
	@Autowired private ApplicationContext context;
	@Autowired private FormService formService;
	@Autowired private IdmRoleService roleService;
	@Autowired private IdmRoleFormAttributeService roleFormAttributeService;
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	//
	private DefaultIdmIdentityRoleService service;

	@Before
	public void init() {
		service = context.getAutowireCapableBeanFactory().createBean(DefaultIdmIdentityRoleService.class);
	}
	
	@After
	public void logout() {
		automaticRoleAttributeService.find(null).forEach(autoRole -> {
			automaticRoleAttributeService.delete(autoRole);
		});
	}
	
	@Test
	@Transactional
	public void testReferentialIntegrityDirectRole() {
		IdmIdentityContractDto contract = getHelper().getPrimeContract(getHelper().createIdentity().getId());
		
		IdmRoleDto directRoleOne = getHelper().createRole(); 
		IdmRoleDto subRoleOne = getHelper().createRole();
		IdmRoleDto directRoleTwo = getHelper().createRole(); 
		//
		IdmIdentityRoleDto directIdentityRoleOne = new IdmIdentityRoleDto();
		directIdentityRoleOne.setIdentityContract(contract.getId());
		directIdentityRoleOne.setRole(directRoleOne.getId());
		directIdentityRoleOne = service.save(directIdentityRoleOne);
		//
		IdmIdentityRoleDto subIdentityRoleOne = new IdmIdentityRoleDto();
		subIdentityRoleOne.setIdentityContract(contract.getId());
		subIdentityRoleOne.setRole(subRoleOne.getId());
		subIdentityRoleOne.setDirectRole(directIdentityRoleOne.getId());
		subIdentityRoleOne = service.save(subIdentityRoleOne);
		//
		IdmIdentityRoleDto otherIdentityRoleOne = new IdmIdentityRoleDto();
		otherIdentityRoleOne.setIdentityContract(contract.getId());
		otherIdentityRoleOne.setRole(directRoleTwo.getId());
		otherIdentityRoleOne = service.save(otherIdentityRoleOne);
		//
		// check after create
		List<IdmIdentityRoleDto> assignedRoles = service.findAllByContract(contract.getId());
		Assert.assertEquals(3, assignedRoles.size());
		//
		// delete direct role
		service.delete(directIdentityRoleOne);
		assignedRoles = service.findAllByContract(contract.getId());
		Assert.assertEquals(1, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(directRoleTwo.getId())));
	}

	@Test
	public void testDuplicityValidInOpenInterval() {
		/*			B
		 *   A   |---------|
		 *  <----|---------------->
		 * ______|_________|____________
		 *             |
		 *            now
		 */
		createAndCheckDuplicitIdentityRole(
				null, null, // A
				new LocalDate().minusDays(5), new LocalDate().plusDays(5), //B 
				B_ROLE_DUPLICATED);
	}

	@Test
	public void testDuplicityManuallyInfinity() {
		/*			  B
		 *   A   <--------->
		 *  <-------------------->
		 * ___________________________
		 *             |
		 *            now
		 */
		createAndCheckDuplicitIdentityRole(
				null, null, // A
				null, null, //B 
				B_ROLE_DUPLICATED);
	}

	@Test
	public void testDuplicityValidInOpenIntervalLeftSideClosed() {
		/*					B
		 *         A   |-------------|
		 * 		  |----|------------------------->
		 * _______|____|_____________|____________
		 *                 |
		 *                now
		 */
		createAndCheckDuplicitIdentityRole(
				new LocalDate().minusDays(10), null, // A
				new LocalDate().minusDays(5), new LocalDate().plusDays(5), // B
				B_ROLE_DUPLICATED);
	}

	@Test
	public void testDuplicityValidInOpenIntervalRightSideClosed() {
		/*					B
		 *         A  |-------------|
		 * 		  <------------------------|
		 * ___________|_____________|______|_____
		 *                   |
		 *                  now
		 */
		createAndCheckDuplicitIdentityRole(
				null, new LocalDate().plusDays(10), // A
				new LocalDate().minusDays(5), new LocalDate().plusDays(5), // B
				B_ROLE_DUPLICATED);
	}

	@Test
	public void testDuplicityValidInClosedInterval() {
		/*					B
		 *         A  |-------------|
		 * 		 |-------------------------|
		 * ______|____|_____________|______|_____
		 *                   |
		 *                  now
		 */
		createAndCheckDuplicitIdentityRole(
				new LocalDate().minusDays(10), new LocalDate().plusDays(10), // A
				new LocalDate().minusDays(5), new LocalDate().plusDays(5), // B
				B_ROLE_DUPLICATED);
	}

	@Test
	public void testDuplicityValidInClosedIntervalFull() {
		/*					B
		 *          |-------------|
		 * 	      A |-------------|
		 * _________|_____________|_______
		 *                 |
		 *                now
		 */
		createAndCheckDuplicitIdentityRole(
				new LocalDate().minusDays(5), new LocalDate().plusDays(5), // A
				new LocalDate().minusDays(5), new LocalDate().plusDays(5), // B
				B_ROLE_DUPLICATED);
	}

	@Test
	public void testDuplicityValidTillSameAndOpenInterval() {
		/*					B
		 *         A  |-------------|
		 * 		 <------------------|
		 * ___________|_____________|___________
		 *                   |
		 *                  now
		 */
		createAndCheckDuplicitIdentityRole(
				null, new LocalDate().plusDays(5), // A
				new LocalDate().minusDays(5), new LocalDate().plusDays(5), // B
				B_ROLE_DUPLICATED);
	}

	@Test
	public void testDuplicityValidTillSameAndClosedInterval() {
		/*					B
		 *         A  |-------------|
		 * 		 |------------------|
		 * ______|____|_____________|_________
		 *                   |
		 *                  now
		 */
		createAndCheckDuplicitIdentityRole(
				new LocalDate().minusDays(50), new LocalDate().plusDays(5),
				new LocalDate().minusDays(5), new LocalDate().plusDays(5),
				B_ROLE_DUPLICATED);
	}

	@Test
	public void testDuplicityValidFromSameAndOpenInterval() {
		/*					B
		 *         A  |-------------|
		 * 		      |-------------------->
		 * ___________|_____________|___________
		 *                   |
		 *                  now
		 */
		createAndCheckDuplicitIdentityRole(
				new LocalDate().minusDays(5), null, // A
				new LocalDate().minusDays(5), new LocalDate().plusDays(5), // B
				B_ROLE_DUPLICATED);
	}

	@Test
	public void testDuplicityValidFromSameAndClosedInterval() {
		/*					B
		 *            |-------------|
		 * 		    A |--------------------|
		 * ___________|_____________|______|_____
		 *                   |
		 *                  now
		 */
		createAndCheckDuplicitIdentityRole(
				new LocalDate().minusDays(5), new LocalDate().plusDays(50), // A
				new LocalDate().minusDays(5), new LocalDate().plusDays(5), // B
				B_ROLE_DUPLICATED);
	}

	@Test
	public void testDuplicityValidOnlyOneRole() {
		/* Contract has infinite validity
		 * 							   B
		 *            A             |------|
		 * 		 |----------|       |      |
		 * ______|__________|_______|______|_____
		 *            |
		 *           now
		 */
		createAndCheckDuplicitIdentityRole(
				new LocalDate().minusDays(5), new LocalDate().plusDays(10), // A
				new LocalDate().plusDays(20), new LocalDate().plusDays(50), // B
				NONE_ROLE_DUPLICATED);
	}

	@Test
	public void testDuplicityValidNoRole() {
		/* Contract has infinite validity
		 * 				     	   B
		 *            A         |------|
		 * 		 |----------|   |      |
		 * ______|__________|___|______|_____
		 *    |
		 *   now
		 */
		createAndCheckDuplicitIdentityRole(
				new LocalDate().plusDays(5), new LocalDate().plusDays(10), // A
				new LocalDate().plusDays(20), new LocalDate().plusDays(50), // B
				NONE_ROLE_DUPLICATED);
	}

	@Test
	public void testDuplicityValidNoRoleWithContract() {
		/* Contract has infinite validity
		 * 				     	   B
		 *            A         |------|
		 * 		 |----------|   |      |
		 * ______|__________|___|______|_____
		 *    |
		 *   now
		 */
		IdmIdentityDto identity = createAndCheckDuplicitIdentityRole(
				new LocalDate().plusDays(5), new LocalDate().plusDays(10), // A
				new LocalDate().plusDays(20), new LocalDate().plusDays(50), // B
				NONE_ROLE_DUPLICATED);

		List<IdmIdentityRoleDto> allByIdentity = service.findAllByIdentity(identity.getId());
		assertEquals(2, allByIdentity.size());
		
		IdmIdentityRoleDto one = allByIdentity.get(0);
		IdmIdentityRoleDto two = allByIdentity.get(1);

		/* Contract has infinite validity
		 * 				     	   B
		 *            A         |------|
		 * 		 |----------|   |      |
		 * ______|__________|___|______|_____
		 *    |				|
		 *   now		contract
		 *   			valid till
		 */
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		contract.setValidTill(new LocalDate().plusDays(10));
		contract = identityContractService.save(contract);
		
		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		assertNull(duplicated);
	}

	@Test
	public void testDuplicityValidOneoRoleWithContract() {
		/* Contract has infinite validity
		 * 				     	   B
		 *            A         |------|
		 * 		 |----------|   |      |
		 * ______|__________|___|______|_____
		 *    		  |
		 *   		 now
		 */
		IdmIdentityDto identity = createAndCheckDuplicitIdentityRole(
				new LocalDate().minusDays(5), new LocalDate().plusDays(10), // A
				new LocalDate().plusDays(20), new LocalDate().plusDays(50), // B
				NONE_ROLE_DUPLICATED);

		List<IdmIdentityRoleDto> allByIdentity = service.findAllByIdentity(identity.getId());
		assertEquals(2, allByIdentity.size());
		
		IdmIdentityRoleDto one = allByIdentity.get(0);
		IdmIdentityRoleDto two = allByIdentity.get(1);

		IdmIdentityRoleDto roleB = null;
		if (one.getValidTill().isAfter(two.getValidTill())) {
			roleB = one;
		} else {
			roleB = two;
		}

		/* Contract has infinite validity
		 * 				     	   B
		 *            A         |------|
		 * 		 |----------|   |      |
		 * ______|__________|___|______|_____
		 *    		|		|
		 *   	   now	contract
		 *   			valid till
		 */
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		contract.setValidTill(new LocalDate().plusDays(10));
		contract = identityContractService.save(contract);

		// Only for embedded and identityContract
		one = service.get(one.getId());
		two = service.get(two.getId());

		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		assertNotNull(duplicated);
		assertEquals(roleB.getId(), duplicated.getId());
	}

	@Test
	public void testDuplicityValidRoleWithDifferentValidity() {
		/* Contract has infinite validity
		 * 							   B
		 *            A    |--------------|
		 * 		 |----------------|       |
		 * ______|_________|______|_______|____
		 *                    |
		 *                   now
		 */
		createAndCheckDuplicitIdentityRole(
				new LocalDate().minusDays(10), new LocalDate().plusDays(10), // A
				new LocalDate().minusDays(5), new LocalDate().plusDays(50), // B
				A_ROLE_DUPLICATED);
	}

	@Test
	public void testDuplicitySpaceBetweenValidity() {
		/* Contract has infinite validity
		 * 							   B
		 *            A             |------|
		 * 		 |----------|       |      |
		 * ______|__________|_______|______|_____
		 *   |
		 *  now
		 */
		createAndCheckDuplicitIdentityRole(
				new LocalDate().plusDays(5), new LocalDate().plusDays(10),
				new LocalDate().plusDays(15), new LocalDate().plusDays(50),
				NONE_ROLE_DUPLICATED);
	}

	@Test
	public void testDuplicityValidInOpenIntervalAutomatic() {
		/* 						MAN
		 *          AUTO  |--------------|
		 * 		 <----------------------------->
		 * _______________|______________|____
		 *                       |
		 *                      now
		 */
		createAndCheckDuplicityAutomaticIdentityRole(
				null, null, // AUTO
				new LocalDate().minusDays(5), new LocalDate().plusDays(5), // MAN
				MANUALLY_DUPLICATED);
	}

	@Test
	public void testDuplicityValidInOpenIntervalLeftSideClosedAutomatic() {
		/* 					    MAN
		 *          AUTO  |--------------|
		 * 		 |----------------------------->
		 * ______|________|______________|____
		 *                       |
		 *                      now
		 */
		createAndCheckDuplicityAutomaticIdentityRole(
				new LocalDate().minusDays(10), null, // AUTO
				new LocalDate().minusDays(5), new LocalDate().plusDays(5), // MAN
				MANUALLY_DUPLICATED);
	}

	@Test
	public void testDuplicityValidInOpenIntervalRightSideClosedAutomatic() {
		/* 					    MAN
		 *          AUTO  |--------------|
		 * 		 <-----------------------------|
		 * _______________|______________|_____|__
		 *                       |
		 *                      now
		 */
		createAndCheckDuplicityAutomaticIdentityRole(
				null, new LocalDate().plusDays(10), // AUTO
				new LocalDate().minusDays(5), new LocalDate().plusDays(5), // MAN
				MANUALLY_DUPLICATED);
	}

	@Test
	public void testDuplicityValidInClosedIntervalAutomatic() {
		/* 						MAN
		 *          AUTO  |--------------|
		 * 		 |-----------------------------|
		 * ______|________|______________|_____|__
		 *                       |
		 *                      now
		 */
		createAndCheckDuplicityAutomaticIdentityRole(
				new LocalDate().minusDays(10), new LocalDate().plusDays(10), // AUTO
				new LocalDate().minusDays(5), new LocalDate().plusDays(5), // MAN
				MANUALLY_DUPLICATED);
	}

	@Test
	public void testDuplicityValidInClosedIntervalFullAutomatic() {
		/* 				   MAN
		 *           |--------------|
		 *     AUTO  |--------------|
		 * __________|______________|_______
		 *                  |
		 *                 now
		 */
		createAndCheckDuplicityAutomaticIdentityRole(
				new LocalDate().minusDays(5), new LocalDate().plusDays(5), // AUTO
				new LocalDate().minusDays(5), new LocalDate().plusDays(5), // MAN
				MANUALLY_DUPLICATED);
	}

	@Test
	public void testDuplicityValidTillSameAndOpenIntervalAutomatic() {
		/* 					    MAN
		 *          AUTO  |--------------|
		 * 		 <-----------------------|
		 * ______ ________|______________|_______
		 *                       |
		 *                      now
		 */
		createAndCheckDuplicityAutomaticIdentityRole(
				null, new LocalDate().plusDays(5), // AUTO
				new LocalDate().minusDays(5), new LocalDate().plusDays(5), // MAN
				MANUALLY_DUPLICATED);
	}

	@Test
	public void testDuplicityValidTillSameAndClosedIntervalAutomatic() {
		/* 					    MAN
		 *          AUTO  |--------------|
		 * 		 |-----------------------|
		 * ______|________|______________|_______
		 *                       |
		 *                      now
		 */
		createAndCheckDuplicityAutomaticIdentityRole(
				new LocalDate().minusDays(50), new LocalDate().plusDays(5), // AUTO
				new LocalDate().minusDays(5), new LocalDate().plusDays(5), // MAN
				MANUALLY_DUPLICATED);
	}

	@Test
	public void testDuplicityValidFromSameAndOpenIntervalAutomatic() {
		/* 				  MAN
		 *          |--------------|
		 *     AUTO |-------------------->
		 * _________|______________|_______
		 *                  |
		 *                 now
		 */
		createAndCheckDuplicityAutomaticIdentityRole(
				new LocalDate().minusDays(5), null, // AUTO
				new LocalDate().minusDays(5), new LocalDate().plusDays(5), // MAN
				MANUALLY_DUPLICATED);
	}

	@Test
	public void testDuplicityValidFromSameAndClosedIntervalAutomatic() {
		/* 				 MAN
		 *          |--------------|
		 *     AUTO |--------------------|
		 * _________|______________|_____|__
		 *                  |
		 *                 now
		 */
		createAndCheckDuplicityAutomaticIdentityRole(
				new LocalDate().minusDays(5), new LocalDate().plusDays(50), // AUTO
				new LocalDate().minusDays(5), new LocalDate().plusDays(5), // MAN
				MANUALLY_DUPLICATED);
	}

	@Test
	public void testDuplicityValidOnlyOneRoleFirstAutomaticManualWithValid() {
		/* 					     MAN
		 *          AUTO       |-----|
		 * 		 |--------|    |     |
		 * ______|________|____|_____|__
		 *            |
		 *           now
		 */
		createAndCheckDuplicityAutomaticIdentityRole(
				new LocalDate().minusDays(5), new LocalDate().plusDays(5), // AUTO
				new LocalDate().plusDays(10), new LocalDate().plusDays(20), // MAN
				MANUALLY_DUPLICATED);
	}

	@Test
	public void testDuplicityValidOnlyOneRoleFirstAutomaticManualWithoutValid() {
		/* 					            MAN
		 *          AUTO          |-------------->
		 * 		 |--------|       |              
		 * ______|________|_______|___________________
		 *            |
		 *           now
		 */
		createAndCheckDuplicityAutomaticIdentityRole(
				new LocalDate().minusDays(5), new LocalDate().plusDays(5), // AUTO
				new LocalDate().plusDays(10), null, // MAN
				MANUALLY_DUPLICATED);
	}

	@Test
	public void testDuplicityValidOnlyOneRoleLastAutomatic() {
		/* 					    MAN
		 *          |--------------|
		 *     AUTO |-------------------|
		 * _________|______________|____|___
		 *                  |
		 *                 now
		 */
		createAndCheckDuplicityAutomaticIdentityRole(
				new LocalDate().minusDays(10), new LocalDate().plusDays(20), // AUTO
				new LocalDate().minusDays(10), new LocalDate().plusDays(5), // MAN
				MANUALLY_DUPLICATED);
	}

	@Test
	public void testDuplicitySpaceBetweenValidityAutomatic() {
		/* 					      MAN
		 *          AUTO        |-----|
		 * 		 |--------|     |     |
		 * ______|________|_____|_____|__
		 *           |
		 *          now
		 */
		createAndCheckDuplicityAutomaticIdentityRole(
				new LocalDate().minusDays(10), new LocalDate().plusDays(20), // AUTO
				new LocalDate().plusDays(30), new LocalDate().plusDays(40), // MAN
				MANUALLY_DUPLICATED);
	}

	@Test
	public void testDuplicityValidManuallyTillSameAsAutomatic() {
		/* 			 MAN
		 *       |-----------------------|
		 * 		 |        |--------------| AUTO
		 * ______|________|______________|_______
		 *                       |
		 *                      now
		 */
		createAndCheckDuplicityAutomaticIdentityRole(
				new LocalDate().minusDays(10), new LocalDate().plusDays(20), // AUTO
				new LocalDate().minusDays(30), new LocalDate().plusDays(20), // MAN
				MANUALLY_DUPLICATED);
	}

	@Test
	public void testDuplicityValidBothAutomaticFirst() {
		/* 					    MAN
		 *          AUTO  |--------------|
		 * 		 |--------------------|  |
		 * ______|________|___________|__|_______
		 *                       |
		 *                      now
		 */
		createAndCheckDuplicityAutomaticIdentityRole(
				new LocalDate().minusDays(10), new LocalDate().plusDays(5), // AUTO
				new LocalDate().minusDays(5), new LocalDate().plusDays(10), // MAN
				MANUALLY_DUPLICATED);
	}

	@Test
	public void testDuplicityValidBothManuallyFirst() {
		/* 		         MAN
		 *       |---------------|
		 * 		 |         |-----------------| AUTO
		 * ______|_________|_____|___________|__
		 *                    |
		 *                   now
		 */
		createAndCheckDuplicityAutomaticIdentityRole(
				new LocalDate().minusDays(5), new LocalDate().plusDays(10), // AUTO
				new LocalDate().minusDays(10), new LocalDate().plusDays(5), // MAN
				MANUALLY_DUPLICATED);
	}

	@Test
	public void testDuplicityValidAutomaticManuallyInfinity() {
		/* 		         MAN
		 *     <--------------------------->
		 * 		      |-----| AUTO
		 * ___________|_____|_____________
		 *               |
		 *                   now
		 */
		createAndCheckDuplicityAutomaticIdentityRole(
				new LocalDate().minusDays(5), new LocalDate().plusDays(10), // AUTO
				null, null, // MAN
				MANUALLY_DUPLICATED);
	}

	@Test
	public void testDuplicityInvalidAutomaticManuallyInfinity() {
		/* 		         MAN
		 *     <--------------------------->
		 * 		      |-----| AUTO
		 * ___________|_____|_____________
		 *       |
		 *      now
		 */
		createAndCheckDuplicityAutomaticIdentityRole(
				new LocalDate().plusDays(5), new LocalDate().plusDays(10), // AUTO
				null, null, // MAN
				MANUALLY_NOT_DUPLICATED);
	}

	@Test
	public void testDuplicityInvalidAutomaticManuallyValidTill() {
		/* 		         MAN
		 *     <-----------------------|
		 * 		      |-----| AUTO     |
		 * ___________|_____|__________|__
		 *       |
		 *      now
		 */
		createAndCheckDuplicityAutomaticIdentityRole(
				new LocalDate().plusDays(5), new LocalDate().plusDays(10), // AUTO
				null, null, // MAN
				MANUALLY_NOT_DUPLICATED);
	}

	@Test
	public void testDuplicityAutomaticManuallyValid() {
		/* 		         MAN
		 *    |--------------------------->
		 * 	  |	      |-----| AUTO
		 * ___|_______|_____|_____________
		 *       |
		 *      now
		 */
		createAndCheckDuplicityAutomaticIdentityRole(
				new LocalDate().plusDays(5), new LocalDate().plusDays(10), // AUTO
				new LocalDate().minusDays(5), null, // MAN
				MANUALLY_NOT_DUPLICATED);
	}

	@Test
	public void testDuplicityAutomaticManuallyInfinity() {
		/* 		         MAN
		 *    <------------------>
		 * 	  <-------------------> AUTO
		 * _________________________________
		 *             |
		 *            now
		 */
		createAndCheckDuplicityAutomaticIdentityRole(
				null, null, // AUTO
				null, null, // MAN
				MANUALLY_DUPLICATED);
	}

	@Test
	public void testDuplicityValidTillFilledOnlyRoleA() {
		/* Contract has infinite validity
		 * 	         A
		 *    <------------------>
		 * 		 <----------| B
		 * ______ __________|____
		 *            |
		 *           now
		 */
		createAndCheckDuplicitIdentityRole(
				null, null, // A
				null, new LocalDate().plusDays(10), // B
				B_ROLE_DUPLICATED);
	}

	@Test
	public void testDuplicityValidTillFilledOnlyRoleAWithContract() {
		/* Contract has infinite validity
		 * 	         B
		 *    <------------------>
		 * 		 <----------| A
		 * ______ __________|____
		 *            |     |
		 *           now   contract
		 *   			   valid till
		 */
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString)null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		contract.setValidTill(new LocalDate().plusDays(10));
		contract = identityContractService.save(contract);
		
		IdmRoleDto role = getHelper().createRole();
		
		IdmIdentityRoleDto one = getHelper().createIdentityRole(contract, role);
		one.setValidTill(LocalDate.now().plusDays(10));
		IdmIdentityRoleDto two = getHelper().createIdentityRole(contract, role);
		
		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		assertNotNull(duplicated);
		assertEquals(two.getId(), duplicated.getId());
	}

	@Test
	public void testSubdefinitionManuallyLong() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());

		IdmRoleDto role = createRoleWithAttributes(prepareAttributeOne());

		IdmIdentityRoleDto one = getHelper().createIdentityRole(identity, role, new LocalDate().minusDays(10), new LocalDate().plusDays(10));
		IdmIdentityRoleDto two = getHelper().createIdentityRole(identity, role, new LocalDate().minusDays(5), new LocalDate().plusDays(5));

		List<IdmIdentityRoleDto> allByIdentity = service.findAllByIdentity(identity.getId());
		assertEquals(2, allByIdentity.size());

		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		assertNotNull(duplicated); // Default values
		assertEquals(two.getId(), duplicated.getId());
		
		IdmFormAttributeDto attribute = getAttribute(ATTRIBUTE_ONE, role);
		setValue(one, attribute, System.currentTimeMillis());

		duplicated = service.getDuplicated(one, two, null);
		assertNull(duplicated);

		setValue(two, attribute, System.currentTimeMillis());

		duplicated = service.getDuplicated(one, two, null);
		assertNull(duplicated);

		long currentTimeMillis = System.currentTimeMillis();
		setValue(two, attribute, currentTimeMillis);
		setValue(one, attribute, currentTimeMillis);

		duplicated = service.getDuplicated(one, two, null);
		assertNotNull(duplicated);
		assertEquals(two.getId(), duplicated.getId());

		duplicated = service.getDuplicated(one, two, Boolean.TRUE);
		assertNotNull(duplicated);
		assertEquals(two.getId(), duplicated.getId());
		
		duplicated = service.getDuplicated(one, two, Boolean.FALSE);
		assertNotNull(duplicated);
		assertEquals(two.getId(), duplicated.getId());
		
		currentTimeMillis = System.currentTimeMillis();
		setValue(two, attribute, currentTimeMillis);
		
		duplicated = service.getDuplicated(one, two, Boolean.FALSE);
		assertNull(duplicated);
		
		duplicated = service.getDuplicated(one, two, Boolean.TRUE);
		assertNotNull(duplicated);
		assertEquals(two.getId(), duplicated.getId());
	}

	@Test
	public void testSubdefinitionManuallyShortText() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());

		IdmRoleDto role = createRoleWithAttributes(prepareAttributeThree());

		IdmIdentityRoleDto one = getHelper().createIdentityRole(identity, role, new LocalDate().minusDays(10), new LocalDate().plusDays(10));
		IdmIdentityRoleDto two = getHelper().createIdentityRole(identity, role, new LocalDate().plusDays(15), new LocalDate().plusDays(20));

		List<IdmIdentityRoleDto> allByIdentity = service.findAllByIdentity(identity.getId());
		assertEquals(2, allByIdentity.size());

		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		assertNull(duplicated);
		
		IdmFormAttributeDto attribute = getAttribute(ATTRIBUTE_THREE, role);
		setValue(one, attribute, "test-" + System.currentTimeMillis());

		duplicated = service.getDuplicated(one, two, null);
		assertNull(duplicated);

		setValue(two, attribute, "test-" + System.currentTimeMillis());

		duplicated = service.getDuplicated(one, two, null);
		assertNull(duplicated);

		String currentTimeMillis = "test-" + System.currentTimeMillis();
		setValue(two, attribute, currentTimeMillis);
		setValue(one, attribute, currentTimeMillis);
		
		one.setValidFrom(null);
		one.setValidTill(null);
		two.setValidFrom(null);
		two.setValidTill(null);

		one = service.save(one);
		two = service.save(two);

		duplicated = service.getDuplicated(one, two, null);
		assertNotNull(duplicated);
		assertEquals(two.getId(), duplicated.getId());

		duplicated = service.getDuplicated(one, two, Boolean.TRUE);
		assertNotNull(duplicated);
		assertEquals(two.getId(), duplicated.getId());
		
		currentTimeMillis = "test-" + System.currentTimeMillis();
		setValue(two, attribute, currentTimeMillis);
		duplicated = service.getDuplicated(one, two, Boolean.TRUE);
		assertNotNull(duplicated);
		assertEquals(two.getId(), duplicated.getId());

		duplicated = service.getDuplicated(one, two, Boolean.FALSE);
		assertNull(duplicated);
	}

	@Test
	public void testSubdefinitionManuallyDate() {
		LocalDate value = LocalDate.now();
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());

		IdmRoleDto role = createRoleWithAttributes(prepareAttributeTwo());

		IdmIdentityRoleDto one = getHelper().createIdentityRole(identity, role, new LocalDate().minusDays(10), new LocalDate().plusDays(10));
		IdmIdentityRoleDto two = getHelper().createIdentityRole(identity, role, new LocalDate().plusDays(15), new LocalDate().plusDays(20));

		List<IdmIdentityRoleDto> allByIdentity = service.findAllByIdentity(identity.getId());
		assertEquals(2, allByIdentity.size());

		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		assertNull(duplicated);
		
		IdmFormAttributeDto attribute = getAttribute(ATTRIBUTE_TWO, role);
		setValue(one, attribute, value.plusDays(1));

		duplicated = service.getDuplicated(one, two, null);
		assertNull(duplicated);

		setValue(two, attribute, value.minusDays(1));

		duplicated = service.getDuplicated(one, two, null);
		assertNull(duplicated);

		setValue(two, attribute, value.minusDays(5));
		setValue(one, attribute, value.minusDays(5));

		// Still not duplicated (validity)
		duplicated = service.getDuplicated(one, two, null);
		assertNull(duplicated);
		
		LocalDate validFrom = new LocalDate().minusDays(5);
		LocalDate validTill = new LocalDate().plusDays(5);
		one.setValidFrom(validFrom);
		one.setValidTill(validTill);
		two.setValidFrom(validFrom);
		two.setValidTill(validTill);

		one = service.save(one);
		two = service.save(two);
		
		duplicated = service.getDuplicated(one, two, null);
		assertNotNull(duplicated);
		assertEquals(two.getId(), duplicated.getId());

		// Duplicated by validity
		duplicated = service.getDuplicated(one, two, Boolean.TRUE);
		assertNotNull(duplicated);
		assertEquals(two.getId(), duplicated.getId());

		one.setValidFrom(validFrom.plusDays(10));
		one.setValidTill(validTill.plusDays(15));
		two.setValidFrom(validFrom);
		two.setValidTill(validTill);

		one = service.save(one);
		two = service.save(two);

		duplicated = service.getDuplicated(one, two, Boolean.TRUE);
		assertNull(duplicated);
	}

	@Test
	public void testSubdefinitionManuallyMultivaluedSame() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());

		IdmRoleDto role = createRoleWithAttributes(prepareAttributeThree());

		IdmIdentityRoleDto one = getHelper().createIdentityRole(identity, role);
		IdmIdentityRoleDto two = getHelper().createIdentityRole(identity, role);

		List<IdmIdentityRoleDto> allByIdentity = service.findAllByIdentity(identity.getId());
		assertEquals(2, allByIdentity.size());

		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		assertNotNull(duplicated);
		assertEquals(two.getId(), duplicated.getId());

		IdmFormAttributeDto attribute = getAttribute(ATTRIBUTE_THREE, role);
		setValue(one, attribute, Lists.newArrayList("1", "2", "3"));

		duplicated = service.getDuplicated(one, two, null);
		assertNull(duplicated);

		setValue(two, attribute, Lists.newArrayList("2", "1", "1"));

		duplicated = service.getDuplicated(one, two, null);
		assertNull(duplicated);

		setValue(two, attribute, Lists.newArrayList("3", "1", "2"));
		duplicated = service.getDuplicated(one, two, null);
		assertNotNull(duplicated);
		assertEquals(two.getId(), duplicated.getId());

		setValue(two, attribute, Lists.newArrayList("1", "2", "3"));
		duplicated = service.getDuplicated(one, two, null);
		assertNotNull(duplicated);
		assertEquals(two.getId(), duplicated.getId());

		duplicated = service.getDuplicated(one, two, Boolean.TRUE);
		assertNotNull(duplicated);
		assertEquals(two.getId(), duplicated.getId());
		
		setValue(two, attribute, Lists.newArrayList("1", "2"));
		duplicated = service.getDuplicated(one, two, Boolean.FALSE);
		assertNull(duplicated);
		
		setValue(two, attribute, Lists.newArrayList("1", "2", "3"));
		
		duplicated = service.getDuplicated(one, two, Boolean.FALSE);
		assertNotNull(duplicated);
		assertEquals(two.getId(), duplicated.getId());

		duplicated = service.getDuplicated(one, two, Boolean.TRUE);
		assertNotNull(duplicated);
		assertEquals(two.getId(), duplicated.getId());
	}

	@Test
	public void testDuplicityValidOnlyOneRoleLastAutomaticWithSubdefinition() {
		/* 					    MAN
		 *          |--------------|
		 *     AUTO |-------------------|
		 * _________|______________|____|___
		 *                  |
		 *                 now
		 */
		IdmRoleDto role = createRoleWithAttributes(prepareAttributeOne());
		IdmFormAttributeDto attribute = getAttribute(ATTRIBUTE_ONE, role);
		
		IdmIdentityDto identity = createAndCheckDuplicityAutomaticIdentityRole(role,
				new LocalDate().minusDays(10), new LocalDate().plusDays(20), // AUTO
				new LocalDate().minusDays(10), new LocalDate().plusDays(5), // MAN
				MANUALLY_DUPLICATED);

		List<IdmIdentityRoleDto> roles = service.findAllByIdentity(identity.getId());

		IdmIdentityRoleDto manually = roles.stream().filter(r -> { return r.getAutomaticRole() == null && r.getDirectRole() == null;}).findFirst().orElse(null);
		IdmIdentityRoleDto automatic = roles.stream().filter(r -> { return r.getAutomaticRole() != null || r.getDirectRole() != null;}).findFirst().orElse(null);
		assertNotNull(automatic);
		assertNotNull(manually);
		assertNotEquals(manually.getId(), automatic.getId());
		automatic = fillEavs(automatic);
		manually = fillEavs(manually);

		IdmIdentityRoleDto duplicated = service.getDuplicated(manually, automatic, null);
		assertNotNull(duplicated);
		assertEquals(manually.getId(), duplicated.getId());
		
		setValue(manually, attribute, Long.valueOf(1233333));

		duplicated = service.getDuplicated(manually, automatic, null);
		assertNull(duplicated);

		duplicated = service.getDuplicated(manually, automatic, Boolean.TRUE);
		assertNotNull(duplicated);
		assertEquals(manually.getId(), duplicated.getId());
		
		duplicated = service.getDuplicated(manually, automatic, null);
		assertNull(duplicated);

		duplicated = service.getDuplicated(manually, automatic, Boolean.TRUE);
		assertNotNull(duplicated);
		assertEquals(manually.getId(), duplicated.getId());

		setValue(manually, attribute, ATTRIBUTE_ONE_DEFAULT_VALUE);

		duplicated = service.getDuplicated(manually, automatic, null);
		assertNotNull(duplicated);
		assertEquals(manually.getId(), duplicated.getId());

		duplicated = service.getDuplicated(automatic, manually, null);
		assertNotNull(duplicated);
		assertEquals(manually.getId(), duplicated.getId());
	}

	@Test
	public void testDuplicityValidAutomaticManuallyInfinityWithSubdefinition() {
		/* 		         MAN
		 *     <--------------------------->
		 * 		      |-----| AUTO
		 * ___________|_____|_____________
		 *               |
		 *                   now
		 */
		IdmIdentityDto identity = createAndCheckDuplicityAutomaticIdentityRole(
				new LocalDate().minusDays(5), new LocalDate().plusDays(10), // AUTO
				null, null, // MAN
				MANUALLY_DUPLICATED);

		List<IdmIdentityRoleDto> roles = service.findAllByIdentity(identity.getId());

		IdmIdentityRoleDto manually = roles.stream().filter(r -> { return r.getAutomaticRole() == null && r.getDirectRole() == null;}).findFirst().orElse(null);
		IdmIdentityRoleDto automatic = roles.stream().filter(r -> { return r.getAutomaticRole() != null || r.getDirectRole() != null;}).findFirst().orElse(null);
		assertNotNull(automatic);
		assertNotNull(manually);
		assertNotEquals(manually.getId(), automatic.getId());

		IdmRoleDto role = DtoUtils.getEmbedded(manually, IdmIdentityRole_.role, IdmRoleDto.class, null);
		assertNotNull(role);
		role = createRoleAttributes(role, prepareAttributeThree());
		
		IdmFormAttributeDto attribute = getAttribute(ATTRIBUTE_THREE, role);
		setValue(manually, attribute, Lists.newArrayList("1"));

		IdmIdentityRoleDto duplicated = service.getDuplicated(manually, automatic, null);
		assertNull(duplicated);

		duplicated = service.getDuplicated(manually, automatic, Boolean.TRUE);
		assertNotNull(duplicated);
		assertEquals(manually.getId(), duplicated.getId());
	}

	@Test
	public void testTwoDiffContract() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity);
		IdmIdentityContractDto identityContact = getHelper().createIdentityContact(identity);
		
		IdmRoleDto role = getHelper().createRole();

		IdmIdentityRoleDto one = getHelper().createIdentityRole(primeContract, role);
		IdmIdentityRoleDto two = getHelper().createIdentityRole(identityContact, role);

		List<IdmIdentityRoleDto> allByIdentity = service.findAllByIdentity(identity.getId());
		assertEquals(2, allByIdentity.size());

		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		assertNull(duplicated);
	}

	@Test
	public void testTwoDiffRoles() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		
		IdmRoleDto roleOne = getHelper().createRole();
		IdmRoleDto roleTwo = getHelper().createRole();

		IdmIdentityRoleDto one = getHelper().createIdentityRole(identity, roleOne);
		IdmIdentityRoleDto two = getHelper().createIdentityRole(identity, roleTwo);

		List<IdmIdentityRoleDto> allByIdentity = service.findAllByIdentity(identity.getId());
		assertEquals(2, allByIdentity.size());

		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		assertNull(duplicated);
	}

	@Test
	public void testDuplicatedWithFakeIdentityRolesWithId() {
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString)null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		IdmIdentityRoleDto one = new IdmIdentityRoleDto(UUID.randomUUID());
		one.setIdentityContract(contract.getId());
		one.setIdentityContractDto(contract);
		one.setRole(role.getId());
		one.setCreated(DateTime.now().minusDays(5));

		IdmIdentityRoleDto two = new IdmIdentityRoleDto(UUID.randomUUID());
		two.setIdentityContract(contract.getId());
		two.setIdentityContractDto(contract);
		two.setRole(role.getId());
		two.setCreated(DateTime.now().minusDays(10));

		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		assertNotNull(duplicated);
		assertEquals(one, duplicated);
	}

	@Test
	public void testDuplicatedWithFakeIdentityRolesWithoutId() {
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString)null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		IdmIdentityRoleDto one = new IdmIdentityRoleDto();
		one.setIdentityContract(contract.getId());
		one.setIdentityContractDto(contract);
		one.setRole(role.getId());
		one.setCreated(DateTime.now().minusDays(5));

		IdmIdentityRoleDto two = new IdmIdentityRoleDto();
		two.setIdentityContract(contract.getId());
		two.setIdentityContractDto(contract);
		two.setRole(role.getId());
		two.setCreated(DateTime.now().minusDays(10));

		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		assertNotNull(duplicated);
	}

	@Test
	public void testDuplicatedWithFakeIdentityRolesWithValid() {
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString)null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		IdmIdentityRoleDto one = new IdmIdentityRoleDto();
		one.setIdentityContract(contract.getId());
		one.setIdentityContractDto(contract);
		one.setRole(role.getId());
		one.setValidFrom(LocalDate.now().plusDays(10));
		one.setCreated(DateTime.now().minusDays(5));

		IdmIdentityRoleDto two = new IdmIdentityRoleDto();
		two.setIdentityContract(contract.getId());
		two.setIdentityContractDto(contract);
		two.setValidTill(LocalDate.now().plusDays(5));
		two.setRole(role.getId());
		two.setCreated(DateTime.now().minusDays(10));

		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		assertNull(duplicated);
	}

	@Test
	public void testDeduplicationWithConceptAndEav() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		IdmRoleDto role = createRoleWithAttributes(prepareAttributeOne());

		IdmIdentityRoleDto one = getHelper().createIdentityRole(identity, role, new LocalDate().minusDays(10), new LocalDate().plusDays(10));
		IdmIdentityRoleDto two = getHelper().createIdentityRole(identity, role, new LocalDate().plusDays(15), new LocalDate().plusDays(40));

		one = fillEavs(one);
		two = fillEavs(two);

		List<IdmFormInstanceDto> formInstances = service.getFormInstances(one);
		IdmFormValueDto valueFromOne = formInstances.get(0).getValues().get(0);
		IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(valueFromOne, IdmFormValue_.formAttribute, IdmFormAttributeDto.class);

		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		assertNull(duplicated);

		List<IdmConceptRoleRequestDto> concepts = new ArrayList<IdmConceptRoleRequestDto>();
		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setRole(role.getId());
		concept.setIdentityContract(contract.getId());
		concept.setValidFrom(new LocalDate().minusDays(9));
		concept.setValidTill(new LocalDate().plusDays(9));
		concept.setOperation(ConceptRoleRequestOperation.ADD);
		Map<String, BaseDto> embedded = concept.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		concept.setEmbedded(embedded);
		concepts.add(concept);
		
		List<IdmConceptRoleRequestDto> removeDuplicities = roleRequestService.removeDuplicities(concepts, identity.getId());
		assertFalse(removeDuplicities.isEmpty()); // EAVs for concept missing

		concepts = new ArrayList<IdmConceptRoleRequestDto>();

		IdmFormInstanceDto instance = new IdmFormInstanceDto();
		IdmFormValueDto value = new IdmFormValueDto(formAttributeDto);
		value.setValue(Long.valueOf(System.currentTimeMillis()));
		instance.setValues(Lists.newArrayList(value));
		concept.setEavs(Lists.newArrayList(instance));
		concepts.add(concept);

		removeDuplicities = roleRequestService.removeDuplicities(concepts, identity.getId());
		assertFalse(removeDuplicities.isEmpty()); // EAVs for concept is different

		concepts = new ArrayList<IdmConceptRoleRequestDto>();
		value = new IdmFormValueDto(formAttributeDto);
		value.setValue(ATTRIBUTE_ONE_DEFAULT_VALUE);
		instance.setValues(Lists.newArrayList(value));
		concept.setEavs(Lists.newArrayList(instance));
		concepts.add(concept);

		removeDuplicities = roleRequestService.removeDuplicities(concepts, identity.getId());
		assertTrue(removeDuplicities.isEmpty()); // EAVs are same
	}

	@Test
	public void testDeduplicationWithConcept() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		IdmRoleDto role = getHelper().createRole();

		IdmIdentityRoleDto one = getHelper().createIdentityRole(identity, role, new LocalDate().minusDays(10), new LocalDate().plusDays(10));
		IdmIdentityRoleDto two = getHelper().createIdentityRole(identity, role, new LocalDate().plusDays(15), new LocalDate().plusDays(40));

		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		assertNull(duplicated);

		List<IdmConceptRoleRequestDto> concepts = new ArrayList<IdmConceptRoleRequestDto>();
		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setRole(role.getId());
		concept.setIdentityContract(contract.getId());
		concept.setValidFrom(new LocalDate().minusDays(9));
		concept.setValidTill(new LocalDate().plusDays(9));
		concept.setOperation(ConceptRoleRequestOperation.ADD);
		Map<String, BaseDto> embedded = concept.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		concept.setEmbedded(embedded);
		concepts.add(concept);

		List<IdmConceptRoleRequestDto> removeDuplicities = roleRequestService.removeDuplicities(concepts, identity.getId());
		assertTrue(removeDuplicities.isEmpty()); // concept is duplicit with one

		concepts = new ArrayList<IdmConceptRoleRequestDto>();
		concept.setRole(UUID.randomUUID());
		concept.setDuplicate(null);
		concept.setDuplicates(null);
		concepts.add(concept);

		removeDuplicities = roleRequestService.removeDuplicities(concepts, identity.getId());
		assertFalse(removeDuplicities.isEmpty()); // Role is different

		concepts = new ArrayList<IdmConceptRoleRequestDto>();
		concept.setRole(role.getId());
		concept.setValidFrom(new LocalDate().plusDays(50));
		concept.setValidTill(new LocalDate().plusDays(60));
		concept.setDuplicate(null);
		concept.setDuplicates(null);
		concepts.add(concept);

		removeDuplicities = roleRequestService.removeDuplicities(concepts, identity.getId());
		assertFalse(removeDuplicities.isEmpty());
	}

	@Test
	public void testDeduplicationWithConceptWithRemovedRole() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		IdmRoleDto role = getHelper().createRole();

		IdmIdentityRoleDto one = getHelper().createIdentityRole(identity, role, null, null);


		List<IdmConceptRoleRequestDto> concepts = new ArrayList<IdmConceptRoleRequestDto>();
		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setRole(role.getId());
		concept.setIdentityContract(contract.getId());
		concept.setOperation(ConceptRoleRequestOperation.ADD);
		Map<String, BaseDto> embedded = concept.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		concept.setEmbedded(embedded);
		concepts.add(concept);

		List<IdmConceptRoleRequestDto> removeDuplicities = roleRequestService.removeDuplicities(concepts, identity.getId());
		assertTrue(removeDuplicities.isEmpty()); // concept is duplicit with one

		concepts = new ArrayList<IdmConceptRoleRequestDto>();
		concept.setDuplicate(null);
		concept.setDuplicates(null);
		concepts.add(concept);
		
		IdmConceptRoleRequestDto conceptWithremove = new IdmConceptRoleRequestDto();
		conceptWithremove.setOperation(ConceptRoleRequestOperation.REMOVE);
		conceptWithremove.setIdentityContract(contract.getId());
		conceptWithremove.setIdentityRole(one.getId());
		embedded = conceptWithremove.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		conceptWithremove.setEmbedded(embedded);
		concepts.add(conceptWithremove);

		removeDuplicities = roleRequestService.removeDuplicities(concepts, identity.getId());
		assertFalse(removeDuplicities.isEmpty()); // role will be removed
		assertEquals(2, removeDuplicities.size());
	}

	@Test
	public void deduplicationConceptSameIdentityRole() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());

		IdmRoleDto role = createRoleWithAttributes();

		getHelper().createIdentityRole(identity, role, null, null);
		 getHelper().createIdentityRole(identity, role, null, null);

		List<IdmConceptRoleRequestDto> duplicities = roleRequestService.removeDuplicities(new ArrayList<IdmConceptRoleRequestDto>(), identity.getId());
		assertTrue(duplicities.isEmpty()); // Deduplication with concept doesn't check current identity roles
	}

	@Test
	public void testDeduplicationConceptWithConcept() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		IdmRoleDto role = getHelper().createRole();

		List<IdmConceptRoleRequestDto> concepts = new ArrayList<IdmConceptRoleRequestDto>();
		IdmConceptRoleRequestDto conceptOne = new IdmConceptRoleRequestDto();
		conceptOne.setRole(role.getId());
		conceptOne.setIdentityContract(contract.getId());
		conceptOne.setOperation(ConceptRoleRequestOperation.ADD);
		Map<String, BaseDto> embedded = conceptOne.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		conceptOne.setEmbedded(embedded);
		concepts.add(conceptOne);
		
		IdmConceptRoleRequestDto conceptTwo = new IdmConceptRoleRequestDto();
		conceptTwo.setRole(role.getId());
		conceptTwo.setIdentityContract(contract.getId());
		conceptTwo.setOperation(ConceptRoleRequestOperation.ADD);
		embedded = conceptTwo.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		conceptTwo.setEmbedded(embedded);
		concepts.add(conceptTwo);

		List<IdmConceptRoleRequestDto> removeDuplicities = roleRequestService.removeDuplicities(concepts, identity.getId());
		assertFalse(removeDuplicities.isEmpty()); // concept is duplicit with another concept
		assertEquals(1, removeDuplicities.size());
		assertEquals(2, concepts.size());
	}

	@Test
	public void testDeduplicationConceptWithConcepts() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		IdmRoleDto role = getHelper().createRole();

		List<IdmConceptRoleRequestDto> concepts = new ArrayList<IdmConceptRoleRequestDto>();
		for (int index = 0; index < 9; index++) {
			IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
			concept.setRole(role.getId());
			concept.setIdentityContract(contract.getId());
			concept.setOperation(ConceptRoleRequestOperation.ADD);
			Map<String, BaseDto> embedded = concept.getEmbedded();
			embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
			concept.setEmbedded(embedded);
			concepts.add(concept);
		}
		
		assertEquals(9, concepts.size());

		List<IdmConceptRoleRequestDto> removeDuplicities = roleRequestService.removeDuplicities(concepts, identity.getId());
		assertFalse(removeDuplicities.isEmpty());
		assertEquals(1, removeDuplicities.size());
		assertEquals(9, concepts.size());
	}

	@Test
	public void testDeduplicationConceptWithConceptsAndIncrementDate() {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);

		IdmRoleDto role = getHelper().createRole();

		List<IdmConceptRoleRequestDto> concepts = new ArrayList<IdmConceptRoleRequestDto>();
		for (int index = 10; index < 19; index++) {
			IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
			concept.setRole(role.getId());
			concept.setIdentityContract(contract.getId());
			concept.setOperation(ConceptRoleRequestOperation.ADD);
			concept.setValidTill(LocalDate.now().plusDays(index));
			Map<String, BaseDto> embedded = concept.getEmbedded();
			embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
			concept.setEmbedded(embedded);
			concepts.add(concept);
		}
		
		assertEquals(9, concepts.size());

		List<IdmConceptRoleRequestDto> removeDuplicities = roleRequestService.removeDuplicities(concepts, identity.getId());
		assertFalse(removeDuplicities.isEmpty());
		assertEquals(1, removeDuplicities.size());
		assertEquals(9, concepts.size());
		
		IdmConceptRoleRequestDto conceptRoleRequestDto = removeDuplicities.get(0);
		assertEquals(LocalDate.now().plusDays(18), conceptRoleRequestDto.getValidTill());
	}

	@Test
	public void testMarkDuplicatesOneConcept() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString)null);
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityContractDto contract = getHelper().createIdentityContact(identity);

		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setId(UUID.randomUUID());
		concept.setRole(role.getId());
		concept.setIdentityContract(contract.getId());
		Map<String, BaseDto> embedded = concept.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		concept.setEmbedded(embedded);
		concept.setOperation(ConceptRoleRequestOperation.ADD);
		concept.setValidFrom(LocalDate.now().minusDays(5));
		concept.setValidTill(LocalDate.now().plusDays(5));

		List<IdmConceptRoleRequestDto> duplicates = roleRequestService.markDuplicates(Lists.newArrayList(concept), Lists.newArrayList());
		assertEquals(1, duplicates.size());
		assertEquals(concept.getId(), duplicates.get(0).getId());
		assertTrue(duplicates.get(0) == concept);
	}

	@Test
	public void testMarkDuplicatesBetweenConcepts() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString)null);
		IdmRoleDto role = getHelper().createRole();
		IdmRoleDto secondRole = getHelper().createRole();
		IdmIdentityContractDto contract = getHelper().createIdentityContact(identity);
		UUID roleId = role.getId();
		UUID contractId = contract.getId();

		IdmConceptRoleRequestDto conceptOne = new IdmConceptRoleRequestDto();
		conceptOne.setId(UUID.randomUUID());
		conceptOne.setRole(roleId);
		conceptOne.setIdentityContract(contractId);
		Map<String, BaseDto> embedded = conceptOne.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		conceptOne.setEmbedded(embedded);
		conceptOne.setOperation(ConceptRoleRequestOperation.ADD);
		conceptOne.setValidFrom(LocalDate.now().minusDays(20));
		conceptOne.setValidTill(LocalDate.now().plusDays(20));

		IdmConceptRoleRequestDto conceptTwo = new IdmConceptRoleRequestDto();
		conceptTwo.setId(UUID.randomUUID());
		conceptTwo.setRole(roleId);
		embedded = conceptTwo.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		conceptTwo.setEmbedded(embedded);
		conceptTwo.setIdentityContract(contractId);
		conceptTwo.setOperation(ConceptRoleRequestOperation.ADD);
		conceptTwo.setValidFrom(LocalDate.now().minusDays(5));
		conceptTwo.setValidTill(LocalDate.now().plusDays(15));
		
		IdmConceptRoleRequestDto conceptThrid = new IdmConceptRoleRequestDto();
		conceptThrid.setId(UUID.randomUUID());
		conceptThrid.setRole(roleId);
		embedded = conceptThrid.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		conceptThrid.setEmbedded(embedded);
		conceptThrid.setIdentityContract(contractId);
		conceptThrid.setOperation(ConceptRoleRequestOperation.ADD);
		conceptThrid.setValidFrom(LocalDate.now().plusDays(50));
		conceptThrid.setValidTill(LocalDate.now().plusDays(100));
		
		IdmConceptRoleRequestDto conceptFour = new IdmConceptRoleRequestDto();
		conceptFour.setId(UUID.randomUUID());
		conceptFour.setRole(secondRole.getId());
		embedded = conceptFour.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		conceptFour.setEmbedded(embedded);
		conceptFour.setIdentityContract(contractId);
		conceptFour.setOperation(ConceptRoleRequestOperation.ADD);
		conceptFour.setValidFrom(LocalDate.now().minusDays(10));
		conceptFour.setValidTill(LocalDate.now().plusDays(2));

		List<IdmConceptRoleRequestDto> duplicates = roleRequestService.markDuplicates(
				Lists.newArrayList(conceptOne, conceptTwo, conceptThrid, conceptFour),
				Lists.newArrayList());
		assertEquals(4, duplicates.size());

		for (IdmConceptRoleRequestDto concept : duplicates) {
			if (concept.getId().equals(conceptOne.getId())) {
				assertFalse(concept.getDuplicate());
			} else if (concept.getId().equals(conceptTwo.getId())) {
				DuplicateRolesDto duplicateWithRoles = concept.getDuplicates();
				assertTrue(concept.getDuplicate());
				assertTrue(duplicateWithRoles.getIdentityRoles().isEmpty());
				assertFalse(duplicateWithRoles.getConcepts().isEmpty());
				assertEquals(1, duplicateWithRoles.getConcepts().size());
				UUID duplicatedId = duplicateWithRoles.getConcepts().get(0);
				assertEquals(conceptOne.getId(), duplicatedId);
			} else if (concept.getId().equals(conceptThrid.getId())) {
				assertFalse(concept.getDuplicate());
			} else if (concept.getId().equals(conceptFour.getId())) {
				assertFalse(concept.getDuplicate());
			} else  {
				fail();
			}
		}
	}

	@Test
	public void testMarkDuplicatesBetweenConceptsWithDelete() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString)null);
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityContractDto contract = getHelper().createIdentityContact(identity);
		UUID roleId = role.getId();
		UUID contractId = contract.getId();

		IdmConceptRoleRequestDto conceptOne = new IdmConceptRoleRequestDto();
		conceptOne.setId(UUID.randomUUID());
		conceptOne.setRole(roleId);
		conceptOne.setIdentityContract(contractId);
		Map<String, BaseDto> embedded = conceptOne.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		conceptOne.setEmbedded(embedded);
		conceptOne.setOperation(ConceptRoleRequestOperation.REMOVE);

		IdmConceptRoleRequestDto conceptTwo = new IdmConceptRoleRequestDto();
		conceptTwo.setId(UUID.randomUUID());
		conceptTwo.setRole(roleId);
		embedded = conceptTwo.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		conceptTwo.setEmbedded(embedded);
		conceptTwo.setIdentityContract(contractId);
		conceptTwo.setOperation(ConceptRoleRequestOperation.ADD);

		List<IdmConceptRoleRequestDto> duplicates = roleRequestService.markDuplicates(
				Lists.newArrayList(conceptOne, conceptTwo),
				Lists.newArrayList());
		assertEquals(2, duplicates.size());

		for (IdmConceptRoleRequestDto concept : duplicates) {
			if (concept.getId().equals(conceptOne.getId())) {
				assertFalse(concept.getDuplicate());
			} else if (concept.getId().equals(conceptTwo.getId())) {
				assertFalse(concept.getDuplicate());
			} else  {
				fail();
			}
		}
	}

	@Test
	public void testMarkDuplicatesBothSame() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString)null);
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityContractDto contract = getHelper().createIdentityContact(identity);
		UUID roleId = role.getId();
		UUID contractId = contract.getId();

		IdmConceptRoleRequestDto conceptOne = new IdmConceptRoleRequestDto();
		conceptOne.setId(UUID.randomUUID());
		conceptOne.setRole(roleId);
		conceptOne.setIdentityContract(contractId);
		Map<String, BaseDto> embedded = conceptOne.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		conceptOne.setEmbedded(embedded);
		conceptOne.setOperation(ConceptRoleRequestOperation.ADD);

		IdmConceptRoleRequestDto conceptTwo = new IdmConceptRoleRequestDto();
		conceptTwo.setId(UUID.randomUUID());
		conceptTwo.setRole(roleId);
		embedded = conceptTwo.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		conceptTwo.setEmbedded(embedded);
		conceptTwo.setIdentityContract(contractId);
		conceptTwo.setOperation(ConceptRoleRequestOperation.ADD);

		List<IdmConceptRoleRequestDto> duplicates = roleRequestService.markDuplicates(
				Lists.newArrayList(conceptOne, conceptTwo),
				Lists.newArrayList());
		assertEquals(2, duplicates.size());

		// Both concepts are same. Duplicates is controlled by order in list
		for (IdmConceptRoleRequestDto concept : duplicates) {
			if (concept.getId().equals(conceptOne.getId())) {
				assertFalse(concept.getDuplicate());
			} else if (concept.getId().equals(conceptTwo.getId())) {
				DuplicateRolesDto duplicateWithRoles = concept.getDuplicates();
				assertTrue(concept.getDuplicate());
				assertTrue(duplicateWithRoles.getIdentityRoles().isEmpty());
				assertFalse(duplicateWithRoles.getConcepts().isEmpty());
				assertEquals(1, duplicateWithRoles.getConcepts().size());
				UUID duplicatedId = duplicateWithRoles.getConcepts().get(0);
				assertEquals(conceptOne.getId(), duplicatedId);
			} else  {
				fail();
			}
		}
	}

	@Test
	public void testMarkDuplicatesBetweenConceptsEmpty() {
		List<IdmConceptRoleRequestDto> duplicates = roleRequestService.markDuplicates(
				Lists.newArrayList(),
				Lists.newArrayList());
		assertEquals(0, duplicates.size());
	}

	@Test
	public void testMarkDuplicatesOneConceptOneIdentityRole() {
		IdmIdentityDto identity = this.getHelper().createIdentity((GuardedString)null);
		IdmRoleDto role = this.getHelper().createRole();
		IdmIdentityContractDto contract = this.getHelper().getPrimeContract(identity);
		IdmIdentityRoleDto identityRole = this.getHelper().createIdentityRole(contract, role, null, LocalDate.now().plusDays(5));

		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setRole(role.getId());
		concept.setIdentityContract(contract.getId());
		Map<String, BaseDto> embedded = concept.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		concept.setEmbedded(embedded);
		concept.setOperation(ConceptRoleRequestOperation.ADD);

		List<IdmConceptRoleRequestDto> duplicates = roleRequestService.markDuplicates(Lists.newArrayList(concept), Lists.newArrayList(identityRole));
		assertEquals(1, duplicates.size());

		DuplicateRolesDto duplicateWithRoles = duplicates.get(0).getDuplicates();
		assertFalse(concept.getDuplicate());
		assertTrue(duplicateWithRoles.getIdentityRoles().isEmpty());
		assertTrue(duplicateWithRoles.getConcepts().isEmpty());
	}

	@Test
	public void testMarkDuplicatesOneConceptOneIdentityRoleWithContractValidity() {
		IdmIdentityDto identity = this.getHelper().createIdentity((GuardedString)null);
		IdmRoleDto role = this.getHelper().createRole();
		IdmIdentityContractDto contract = this.getHelper().getPrimeContract(identity);
		contract.setValidTill(LocalDate.now().plusDays(5));
		contract = identityContractService.save(contract);

		IdmIdentityRoleDto identityRole = this.getHelper().createIdentityRole(contract, role, null, LocalDate.now().plusDays(5));

		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setRole(role.getId());
		concept.setIdentityContract(contract.getId());
		Map<String, BaseDto> embedded = concept.getEmbedded();
		embedded.put(IdmConceptRoleRequest_.identityContract.getName(), contract);
		concept.setEmbedded(embedded);
		concept.setOperation(ConceptRoleRequestOperation.ADD);

		List<IdmConceptRoleRequestDto> duplicates = roleRequestService.markDuplicates(Lists.newArrayList(concept), Lists.newArrayList(identityRole));
		assertEquals(1, duplicates.size());
		
		DuplicateRolesDto duplicateWithRoles = duplicates.get(0).getDuplicates();
		assertTrue(concept.getDuplicate());
		assertFalse(duplicateWithRoles.getIdentityRoles().isEmpty());
		assertTrue(duplicateWithRoles.getConcepts().isEmpty());
		assertEquals(1, duplicateWithRoles.getIdentityRoles().size());
		assertNull(duplicates.get(0).getId());

		assertEquals(identityRole.getId(), duplicateWithRoles.getIdentityRoles().get(0)); 
	}

	private IdmFormInstanceDto setValue(IdmIdentityRoleDto identityRole, IdmFormAttributeDto attribute, Serializable value) {
		IdmRoleDto role = DtoUtils.getEmbedded(identityRole, IdmIdentityRole_.role, IdmRoleDto.class);
		IdmFormDefinitionDto subdefinition = roleService.getFormAttributeSubdefinition(role);

		IdmFormInstanceDto formInstance = formService.getFormInstance(identityRole, subdefinition);
		// Remove current attribute
		List<IdmFormValueDto> values = new ArrayList<>(formInstance.getValues());
		values.removeIf(val -> val.getFormAttribute().equals(attribute.getId()));
		formService.deleteValues(identityRole, attribute);

		List<IdmFormValueDto> finalResult = new ArrayList<>();
		if (attribute.isMultiple()) {
			if (value instanceof Collection) {
				@SuppressWarnings({ "rawtypes", "unchecked" })
				Collection<Serializable> parameterValues = (Collection)value;
				for (Serializable val : parameterValues) {
					IdmFormValueDto newValue = new IdmFormValueDto(attribute);
					newValue.setValue(val);
					finalResult.add(newValue);
				}
			} else {
				IdmFormValueDto newValue = new IdmFormValueDto(attribute);
				newValue.setValue(value);
				finalResult.add(newValue);
			}
		} else {
			IdmFormValueDto newValue = new IdmFormValueDto(attribute);
			newValue.setValue(value);
			finalResult.add(newValue);
		}

		values.addAll(finalResult);
		
		IdmFormInstanceDto formInstanceDto = formService.saveFormInstance(identityRole, subdefinition, values);

		// Set back to identity role eavs attributes
		identityRole.setEavs(Lists.newArrayList(formInstanceDto));

		return formInstanceDto;
	}

	private IdmRoleDto createRoleWithAttributes(IdmFormAttributeDto... attrs) {
		IdmRoleDto role = getHelper().createRole();
		assertNull(role.getIdentityRoleAttributeDefinition());
	
		return createRoleAttributes(role, attrs);
	}

	private IdmRoleDto createRoleAttributes(IdmRoleDto role, IdmFormAttributeDto... attrs) {
		assertNull(role.getIdentityRoleAttributeDefinition());
	
		IdmFormDefinitionDto definition = formService.createDefinition(IdmIdentityRole.class, getHelper().createName(),
				ImmutableList.copyOf(attrs));
		role.setIdentityRoleAttributeDefinition(definition.getId());
		role = roleService.save(role);
		assertNotNull(role.getIdentityRoleAttributeDefinition());
		IdmRoleDto roleFinal = role;
		definition.getFormAttributes().forEach(attribute -> {
			roleFormAttributeService.addAttributeToSubdefintion(roleFinal, attribute);
		});

		return role;
	}

	private IdmFormAttributeDto getAttribute(String attributeCode, IdmRoleDto role) {
		IdmFormDefinitionDto subdefinition = roleService.getFormAttributeSubdefinition(role);
		IdmFormAttributeDto attributeDto = subdefinition.getFormAttributes().stream().filter(att -> { return att.getCode().equals(attributeCode);}).findFirst().orElse(null);
		assertNotNull(attributeDto);
		return attributeDto;
	}

	private IdmFormAttributeDto prepareAttributeOne() {
		IdmFormAttributeDto one = new IdmFormAttributeDto(ATTRIBUTE_ONE);
		one.setPersistentType(PersistentType.LONG);
		one.setDefaultValue(String.valueOf(ATTRIBUTE_ONE_DEFAULT_VALUE));
		one.setRequired(false);
		return one;
	}

	private IdmFormAttributeDto prepareAttributeTwo() {
		IdmFormAttributeDto two = new IdmFormAttributeDto(ATTRIBUTE_TWO);
		two.setPersistentType(PersistentType.DATE);
		two.setRequired(false);
		return two;
	}

	private IdmFormAttributeDto prepareAttributeThree() {
		IdmFormAttributeDto three = new IdmFormAttributeDto(ATTRIBUTE_THREE);
		three.setPersistentType(PersistentType.SHORTTEXT);
		three.setMultiple(true);
		three.setRequired(false);
		return three;
	}
	
	private IdmIdentityDto createAndCheckDuplicitIdentityRole(LocalDate validFromOne, LocalDate validTillOne,
			LocalDate validFromTwo, LocalDate validTillTwo, Boolean firstDuplicit) {
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());

		IdmRoleDto role = getHelper().createRole();

		IdmIdentityRoleDto one = getHelper().createIdentityRole(identity, role, validFromOne, validTillOne);
		IdmIdentityRoleDto two = getHelper().createIdentityRole(identity, role, validFromTwo, validTillTwo);

		List<IdmIdentityRoleDto> allByIdentity = service.findAllByIdentity(identity.getId());
		assertEquals(2, allByIdentity.size());

		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		
		if (firstDuplicit == null) {
			assertNull(duplicated);
		} else if (BooleanUtils.isTrue(firstDuplicit)) {
			assertNotNull(duplicated);
			assertEquals(one.getId(), duplicated.getId());
		} else {
			assertNotNull(duplicated);
			assertEquals(two.getId(), duplicated.getId());
		}

		// Different order
		duplicated = service.getDuplicated(two, one, null);
		
		if (firstDuplicit == null) {
			assertNull(duplicated);
		} else if (BooleanUtils.isTrue(firstDuplicit)) {
			assertNotNull(duplicated);
			assertEquals(one.getId(), duplicated.getId());
		} else {
			assertNotNull(duplicated);
			assertEquals(two.getId(), duplicated.getId());
		}

		return identity;
	}
	
	private IdmIdentityDto createAndCheckDuplicityAutomaticIdentityRole(LocalDate validFromOne, LocalDate validTillOne,
			LocalDate validFromTwo, LocalDate validTillTwo, boolean manuallyDuplicit) {
		IdmRoleDto role = getHelper().createRole();
		return createAndCheckDuplicityAutomaticIdentityRole(role, validFromOne, validTillOne, validFromTwo, validTillTwo, manuallyDuplicit);
	}

	private IdmIdentityDto createAndCheckDuplicityAutomaticIdentityRole(IdmRoleDto role, LocalDate validFromOne, LocalDate validTillOne,
			LocalDate validFromTwo, LocalDate validTillTwo, boolean manuallyDuplicit) {
		String description = "test-" + System.currentTimeMillis();
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		identity.setDescription(description);
		identity = identityService.save(identity);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		contract.setValidFrom(validFromOne);
		contract.setValidTill(validTillOne);
		contract = identityContractService.save(contract);

		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(
				automaticRole.getId(),
				AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY,
				IdmIdentity_.description.getName(), null, description);
		getHelper().recalculateAutomaticRoleByAttribute(automaticRole.getId());
		
		IdmIdentityRoleDto manually = getHelper().createIdentityRole(identity, role, validFromTwo, validTillTwo);
		final UUID manuallyId = manually.getId();

		List<IdmIdentityRoleDto> allByIdentity = service.findAllByIdentity(identity.getId());
		assertEquals(2, allByIdentity.size());
		IdmIdentityRoleDto automatic = allByIdentity.stream().filter(identityRole -> { return !identityRole.getId().equals(manuallyId);}).findFirst().orElse(null);
		assertNotNull(automatic);
		assertNotEquals(automatic.getId(), manually.getId());

		// Fill eavs (if exists)
		automatic = fillEavs(automatic);
		manually = fillEavs(manually);

		IdmIdentityRoleDto duplicated = service.getDuplicated(manually, automatic, null);
		if (manuallyDuplicit) {
			assertNotNull(duplicated);
			assertEquals(manually.getId(), duplicated.getId());
			assertNotEquals(automatic.getId(), duplicated.getId());
		} else {
			assertNull(duplicated);
		}

		duplicated = service.getDuplicated(automatic, manually, null);
		if (manuallyDuplicit) {
			assertNotNull(duplicated);
			assertEquals(manually.getId(), duplicated.getId());
			assertNotEquals(automatic.getId(), duplicated.getId());
		} else {
			assertNull(duplicated);
		}

		return identity;
	}

	private IdmIdentityRoleDto fillEavs(IdmIdentityRoleDto identityRole) {
		identityRole.setEavs(service.getFormInstances(identityRole));
		return identityRole;
	}
}
