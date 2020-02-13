package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.BooleanUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Assigned roles integration tests
 * - role deduplicate
 * 
 * @author Radek Tomiška
 * @author Ondrej Kopr
 *
 */
public class DefaultIdmIdentityRoleServiceUnitTest extends AbstractUnitTest {
	
	private static UUID ATTRIBUTE_ONE = UUID.randomUUID();
	private static UUID ATTRIBUTE_TWO = UUID.randomUUID();
	private static UUID ATTRIBUTE_THREE = UUID.randomUUID();
	private static Long ATTRIBUTE_ONE_DEFAULT_VALUE = 1122337788l;
	private static Boolean B_ROLE_DUPLICATED = Boolean.FALSE;
	private static Boolean A_ROLE_DUPLICATED = Boolean.TRUE;
	private static Boolean NONE_ROLE_DUPLICATED = null;
	private static boolean MANUALLY_DUPLICATED = true;
	private static boolean MANUALLY_NOT_DUPLICATED = false;
	//
	@Mock private IdmIdentityRoleRepository repository;
	@Mock private LookupService lookupService;
	@Mock private FormService formService;
	@Mock private EntityEventManager entityEventManager;
	@InjectMocks
	private DefaultIdmIdentityRoleService service;
	
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
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), //B 
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
				LocalDate.now().minusDays(10), null, // A
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), // B
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
				null, LocalDate.now().plusDays(10), // A
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), // B
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
				LocalDate.now().minusDays(10), LocalDate.now().plusDays(10), // A
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), // B
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
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), // A
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), // B
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
				null, LocalDate.now().plusDays(5), // A
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), // B
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
				LocalDate.now().minusDays(50), LocalDate.now().plusDays(5),
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(5),
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
				LocalDate.now().minusDays(5), null, // A
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), // B
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
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(50), // A
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), // B
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
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(10), // A
				LocalDate.now().plusDays(20), LocalDate.now().plusDays(50), // B
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
				LocalDate.now().plusDays(5), LocalDate.now().plusDays(10), // A
				LocalDate.now().plusDays(20), LocalDate.now().plusDays(50), // B
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
		List<IdmIdentityRoleDto> allByIdentity = createAndCheckDuplicitIdentityRole(
				LocalDate.now().plusDays(5), LocalDate.now().plusDays(10), // A
				LocalDate.now().plusDays(20), LocalDate.now().plusDays(50), // B
				NONE_ROLE_DUPLICATED);
		Assert.assertEquals(2, allByIdentity.size());
		
		/* Contract has infinite validity
		 * 				     	   B
		 *            A         |------|
		 * 		 |----------|   |      |
		 * ______|__________|___|______|_____
		 *    |				|
		 *   now		contract
		 *   			valid till
		 */
		allByIdentity.forEach(ir -> 
			((IdmIdentityContractDto) ir.getEmbedded().get(IdmIdentityRoleDto.PROPERTY_IDENTITY_CONTRACT)).setValidTill(LocalDate.now().plusDays(10))
		);
		IdmIdentityRoleDto one = allByIdentity.get(0);
		IdmIdentityRoleDto two = allByIdentity.get(1);
		
		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		Assert.assertNotNull(duplicated);
		// contract valid till < role two validity => role validity will be removed
		Assert.assertEquals(allByIdentity.get(1).getId(), duplicated.getId());
	}

	@Test
	public void testDuplicityValidOneRoleWithContract() {
		/* Contract has infinite validity
		 * 				     	   B
		 *            A         |------|
		 * 		 |----------|   |      |
		 * ______|__________|___|______|_____
		 *    		  |
		 *   		 now
		 */
		List<IdmIdentityRoleDto> allByIdentity = createAndCheckDuplicitIdentityRole(
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(10), // A
				LocalDate.now().plusDays(20), LocalDate.now().plusDays(50), // B
				NONE_ROLE_DUPLICATED);

		Assert.assertEquals(2, allByIdentity.size());
		
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
		allByIdentity.forEach(ir -> 
			((IdmIdentityContractDto) ir.getEmbedded().get(IdmIdentityRoleDto.PROPERTY_IDENTITY_CONTRACT)).setValidTill(LocalDate.now().plusDays(10))
		);

		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		Assert.assertNotNull(duplicated);
		Assert.assertEquals(roleB.getId(), duplicated.getId());
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
				LocalDate.now().minusDays(10), LocalDate.now().plusDays(10), // A
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(50), // B
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
				LocalDate.now().plusDays(5), LocalDate.now().plusDays(10),
				LocalDate.now().plusDays(15), LocalDate.now().plusDays(50),
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
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), // MAN
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
				LocalDate.now().minusDays(10), null, // AUTO
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), // MAN
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
				null, LocalDate.now().plusDays(10), // AUTO
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), // MAN
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
				LocalDate.now().minusDays(10), LocalDate.now().plusDays(10), // AUTO
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), // MAN
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
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), // AUTO
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), // MAN
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
				null, LocalDate.now().plusDays(5), // AUTO
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), // MAN
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
				LocalDate.now().minusDays(50), LocalDate.now().plusDays(5), // AUTO
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), // MAN
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
				LocalDate.now().minusDays(5), null, // AUTO
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), // MAN
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
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(50), // AUTO
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), // MAN
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
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), // AUTO
				LocalDate.now().plusDays(10), LocalDate.now().plusDays(20), // MAN
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
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), // AUTO
				LocalDate.now().plusDays(10), null, // MAN
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
				LocalDate.now().minusDays(10), LocalDate.now().plusDays(20), // AUTO
				LocalDate.now().minusDays(10), LocalDate.now().plusDays(5), // MAN
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
				LocalDate.now().minusDays(10), LocalDate.now().plusDays(20), // AUTO
				LocalDate.now().plusDays(30), LocalDate.now().plusDays(40), // MAN
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
				LocalDate.now().minusDays(10), LocalDate.now().plusDays(20), // AUTO
				LocalDate.now().minusDays(30), LocalDate.now().plusDays(20), // MAN
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
				LocalDate.now().minusDays(10), LocalDate.now().plusDays(5), // AUTO
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(10), // MAN
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
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(10), // AUTO
				LocalDate.now().minusDays(10), LocalDate.now().plusDays(5), // MAN
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
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(10), // AUTO
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
				LocalDate.now().plusDays(5), LocalDate.now().plusDays(10), // AUTO
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
				LocalDate.now().plusDays(5), LocalDate.now().plusDays(10), // AUTO
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
				LocalDate.now().plusDays(5), LocalDate.now().plusDays(10), // AUTO
				LocalDate.now().minusDays(5), null, // MAN
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
				null, LocalDate.now().plusDays(10), // B
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
		IdmIdentityContractDto contract = new IdmIdentityContractDto(UUID.randomUUID());
		contract.setValidTill(LocalDate.now().plusDays(10));
		
		IdmRoleDto role = new IdmRoleDto(UUID.randomUUID());
		
		IdmIdentityRoleDto one = prepareIdentityRole(contract, role, null, null);
		one.setValidTill(LocalDate.now().plusDays(10));
		IdmIdentityRoleDto two = prepareIdentityRole(contract, role, null, null);
		
		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		Assert.assertNotNull(duplicated);
		Assert.assertEquals(two.getId(), duplicated.getId());
	}
	
	/**
	 * #2034 Remove duplicates by simultaneously edited role composition
	 */
	@Test
	public void testDuplicateSubRoles() {
		IdmIdentityContractDto contract = new IdmIdentityContractDto(UUID.randomUUID());
		IdmRoleDto role = new IdmRoleDto(UUID.randomUUID());
		UUID compositionId = UUID.randomUUID();
		//
		IdmIdentityRoleDto one = prepareIdentityRole(contract, role, null, null);
		one.setRoleComposition(compositionId);
		one.setDirectRole(role.getId());
		IdmIdentityRoleDto two = prepareIdentityRole(contract, role, null, null);
		two.setRoleComposition(compositionId);
		two.setDirectRole(role.getId());
		//
		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		Assert.assertNotNull(duplicated);
		Assert.assertEquals(two.getId(), duplicated.getId());
		//
		one.setRoleComposition(compositionId);
		two.setRoleComposition(UUID.randomUUID());
		duplicated = service.getDuplicated(one, two, null);
		Assert.assertNull(duplicated);
		//
		one.setRoleComposition(compositionId);
		two.setRoleComposition(compositionId);
		two.setDirectRole(UUID.randomUUID());
		duplicated = service.getDuplicated(one, two, null);
		Assert.assertNull(duplicated);
		//
		one.setRoleComposition(compositionId);
		two.setRoleComposition(compositionId);
		two.setDirectRole(role.getId());
		//
		duplicated = service.getDuplicated(one, two, null);
		Assert.assertNotNull(duplicated);
		Assert.assertEquals(two.getId(), duplicated.getId());
		// manual, but duplicate
		two.setRoleComposition(null);
		two.setDirectRole(null);
		duplicated = service.getDuplicated(one, two, null);
		Assert.assertEquals(two.getId(), duplicated.getId());
	}
	
	@Test
	public void testSubdefinitionManuallyLong() {
		IdmIdentityContractDto contract = new IdmIdentityContractDto(UUID.randomUUID());
		IdmRoleDto role = new IdmRoleDto(UUID.randomUUID());
		//
		IdmIdentityRoleDto one = prepareIdentityRole(contract, role, LocalDate.now().minusDays(10), LocalDate.now().plusDays(10));
		IdmIdentityRoleDto two = prepareIdentityRole(contract, role, LocalDate.now().minusDays(5), LocalDate.now().plusDays(5));

		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		Assert.assertNotNull(duplicated); // Default values
		Assert.assertEquals(two.getId(), duplicated.getId());
		
		IdmFormAttributeDto attribute = prepareAttributeOne();
		setValue(one, attribute, getLong());

		duplicated = service.getDuplicated(one, two, null);
		Assert.assertNull(duplicated);

		setValue(two, attribute, getLong());

		duplicated = service.getDuplicated(one, two, null);
		Assert.assertNull(duplicated);

		long currentTimeMillis = getLong();
		setValue(two, attribute, currentTimeMillis);
		setValue(one, attribute, currentTimeMillis);

		duplicated = service.getDuplicated(one, two, null);
		Assert.assertNotNull(duplicated);
		Assert.assertEquals(two.getId(), duplicated.getId());

		duplicated = service.getDuplicated(one, two, Boolean.TRUE);
		Assert.assertNotNull(duplicated);
		Assert.assertEquals(two.getId(), duplicated.getId());
		
		duplicated = service.getDuplicated(one, two, Boolean.FALSE);
		Assert.assertNotNull(duplicated);
		Assert.assertEquals(two.getId(), duplicated.getId());
		
		currentTimeMillis = getLong();
		setValue(two, attribute, currentTimeMillis);
		
		duplicated = service.getDuplicated(one, two, Boolean.FALSE);
		Assert.assertNull(duplicated);
		
		duplicated = service.getDuplicated(one, two, Boolean.TRUE);
		Assert.assertNotNull(duplicated);
		Assert.assertEquals(two.getId(), duplicated.getId());
	}

	@Test
	public void testSubdefinitionManuallyShortText() {
		IdmIdentityContractDto contract = new IdmIdentityContractDto(UUID.randomUUID());
		IdmRoleDto role = new IdmRoleDto(UUID.randomUUID());

		IdmIdentityRoleDto one = prepareIdentityRole(contract, role, LocalDate.now().minusDays(10), LocalDate.now().plusDays(10));
		IdmIdentityRoleDto two = prepareIdentityRole(contract, role, LocalDate.now().plusDays(15), LocalDate.now().plusDays(20));

		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		Assert.assertNull(duplicated);
		
		IdmFormAttributeDto attribute = prepareAttributeThree();
		setValue(one, attribute, "test-" + getLong());

		duplicated = service.getDuplicated(one, two, null);
		Assert.assertNull(duplicated);

		setValue(two, attribute, "test-" + getLong());

		duplicated = service.getDuplicated(one, two, null);
		Assert.assertNull(duplicated);

		String currentTimeMillis = "test-" + getLong();
		setValue(two, attribute, currentTimeMillis);
		setValue(one, attribute, currentTimeMillis);
		
		one.setValidFrom(null);
		one.setValidTill(null);
		two.setValidFrom(null);
		two.setValidTill(null);

		duplicated = service.getDuplicated(one, two, null);
		Assert.assertNotNull(duplicated);
		Assert.assertEquals(two.getId(), duplicated.getId());

		duplicated = service.getDuplicated(one, two, Boolean.TRUE);
		Assert.assertNotNull(duplicated);
		Assert.assertEquals(two.getId(), duplicated.getId());
		
		currentTimeMillis = "test-" + getLong();
		setValue(two, attribute, currentTimeMillis);
		duplicated = service.getDuplicated(one, two, Boolean.TRUE);
		Assert.assertNotNull(duplicated);
		Assert.assertEquals(two.getId(), duplicated.getId());

		duplicated = service.getDuplicated(one, two, Boolean.FALSE);
		Assert.assertNull(duplicated);
	}

	@Test
	public void testSubdefinitionManuallyDate() {
		LocalDate value = LocalDate.now();
		IdmIdentityContractDto contract = new IdmIdentityContractDto(UUID.randomUUID());
		IdmRoleDto role = new IdmRoleDto(UUID.randomUUID());

		IdmIdentityRoleDto one = prepareIdentityRole(contract, role, LocalDate.now().minusDays(10), LocalDate.now().plusDays(10));
		IdmIdentityRoleDto two = prepareIdentityRole(contract, role, LocalDate.now().plusDays(15), LocalDate.now().plusDays(20));


		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		Assert.assertNull(duplicated);
		
		IdmFormAttributeDto attribute = prepareAttributeTwo();
		setValue(one, attribute, value.plusDays(1));

		duplicated = service.getDuplicated(one, two, null);
		Assert.assertNull(duplicated);

		setValue(two, attribute, value.minusDays(1));

		duplicated = service.getDuplicated(one, two, null);
		Assert.assertNull(duplicated);

		setValue(two, attribute, value.minusDays(5));
		setValue(one, attribute, value.minusDays(5));

		// Still not duplicated (validity)
		duplicated = service.getDuplicated(one, two, null);
		Assert.assertNull(duplicated);
		
		LocalDate validFrom = LocalDate.now().minusDays(5);
		LocalDate validTill = LocalDate.now().plusDays(5);
		one.setValidFrom(validFrom);
		one.setValidTill(validTill);
		two.setValidFrom(validFrom);
		two.setValidTill(validTill);
		
		duplicated = service.getDuplicated(one, two, null);
		Assert.assertNotNull(duplicated);
		Assert.assertEquals(two.getId(), duplicated.getId());

		// Duplicated by validity
		duplicated = service.getDuplicated(one, two, Boolean.TRUE);
		Assert.assertNotNull(duplicated);
		Assert.assertEquals(two.getId(), duplicated.getId());

		one.setValidFrom(validFrom.plusDays(10));
		one.setValidTill(validTill.plusDays(15));
		two.setValidFrom(validFrom);
		two.setValidTill(validTill);

		duplicated = service.getDuplicated(one, two, Boolean.TRUE);
		Assert.assertNull(duplicated);
	}

	@Test
	public void testSubdefinitionManuallyMultivaluedSame() {
		IdmIdentityContractDto contract = new IdmIdentityContractDto(UUID.randomUUID());
		IdmRoleDto role = new IdmRoleDto(UUID.randomUUID());

		IdmIdentityRoleDto one = prepareIdentityRole(contract, role, null, null);
		IdmIdentityRoleDto two = prepareIdentityRole(contract, role, null, null);


		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		Assert.assertNotNull(duplicated);
		Assert.assertEquals(two.getId(), duplicated.getId());

		IdmFormAttributeDto attribute = prepareAttributeThree();
		setValue(one, attribute, Lists.newArrayList("1", "2", "3"));

		duplicated = service.getDuplicated(one, two, null);
		Assert.assertNull(duplicated);

		setValue(two, attribute, Lists.newArrayList("2", "1", "1"));

		duplicated = service.getDuplicated(one, two, null);
		Assert.assertNull(duplicated);

		setValue(two, attribute, Lists.newArrayList("3", "1", "2"));
		duplicated = service.getDuplicated(one, two, null);
		Assert.assertNotNull(duplicated);
		Assert.assertEquals(two.getId(), duplicated.getId());

		setValue(two, attribute, Lists.newArrayList("1", "2", "3"));
		duplicated = service.getDuplicated(one, two, null);
		Assert.assertNotNull(duplicated);
		Assert.assertEquals(two.getId(), duplicated.getId());

		duplicated = service.getDuplicated(one, two, Boolean.TRUE);
		Assert.assertNotNull(duplicated);
		Assert.assertEquals(two.getId(), duplicated.getId());
		
		setValue(two, attribute, Lists.newArrayList("1", "2"));
		duplicated = service.getDuplicated(one, two, Boolean.FALSE);
		Assert.assertNull(duplicated);
		
		setValue(two, attribute, Lists.newArrayList("1", "2", "3"));
		
		duplicated = service.getDuplicated(one, two, Boolean.FALSE);
		Assert.assertNotNull(duplicated);
		Assert.assertEquals(two.getId(), duplicated.getId());

		duplicated = service.getDuplicated(one, two, Boolean.TRUE);
		Assert.assertNotNull(duplicated);
		Assert.assertEquals(two.getId(), duplicated.getId());
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
		IdmRoleDto role = new IdmRoleDto(UUID.randomUUID());
		IdmFormAttributeDto attribute = prepareAttributeOne();
		
		List<IdmIdentityRoleDto> roles = createAndCheckDuplicityAutomaticIdentityRole(role,
				LocalDate.now().minusDays(10), LocalDate.now().plusDays(20), // AUTO
				LocalDate.now().minusDays(10), LocalDate.now().plusDays(5), // MAN
				MANUALLY_DUPLICATED);

		IdmIdentityRoleDto manually = roles.stream().filter(r -> { return r.getAutomaticRole() == null && r.getDirectRole() == null;}).findFirst().orElse(null);
		IdmIdentityRoleDto automatic = roles.stream().filter(r -> { return r.getAutomaticRole() != null || r.getDirectRole() != null;}).findFirst().orElse(null);
		Assert.assertNotNull(automatic);
		Assert.assertNotNull(manually);
		Assert.assertNotEquals(manually.getId(), automatic.getId());
		setValue(automatic, attribute, attribute.getDefaultValue());
		setValue(manually, attribute, attribute.getDefaultValue());

		IdmIdentityRoleDto duplicated = service.getDuplicated(manually, automatic, null);
		Assert.assertNotNull(duplicated);
		Assert.assertEquals(manually.getId(), duplicated.getId());
		
		setValue(manually, attribute, Long.valueOf(1233333));

		duplicated = service.getDuplicated(manually, automatic, null);
		Assert.assertNull(duplicated);

		duplicated = service.getDuplicated(manually, automatic, Boolean.TRUE);
		Assert.assertNotNull(duplicated);
		Assert.assertEquals(manually.getId(), duplicated.getId());
		
		duplicated = service.getDuplicated(manually, automatic, null);
		Assert.assertNull(duplicated);

		duplicated = service.getDuplicated(manually, automatic, Boolean.TRUE);
		Assert.assertNotNull(duplicated);
		Assert.assertEquals(manually.getId(), duplicated.getId());

		setValue(manually, attribute, ATTRIBUTE_ONE_DEFAULT_VALUE);

		duplicated = service.getDuplicated(manually, automatic, null);
		Assert.assertNotNull(duplicated);
		Assert.assertEquals(manually.getId(), duplicated.getId());

		duplicated = service.getDuplicated(automatic, manually, null);
		Assert.assertNotNull(duplicated);
		Assert.assertEquals(manually.getId(), duplicated.getId());
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
		List<IdmIdentityRoleDto> roles = createAndCheckDuplicityAutomaticIdentityRole(
				LocalDate.now().minusDays(5), LocalDate.now().plusDays(10), // AUTO
				null, null, // MAN
				MANUALLY_DUPLICATED);

		IdmIdentityRoleDto manually = roles.stream().filter(r -> { return r.getAutomaticRole() == null && r.getDirectRole() == null;}).findFirst().orElse(null);
		IdmIdentityRoleDto automatic = roles.stream().filter(r -> { return r.getAutomaticRole() != null || r.getDirectRole() != null;}).findFirst().orElse(null);
		assertNotNull(automatic);
		assertNotNull(manually);
		assertNotEquals(manually.getId(), automatic.getId());
		
		IdmFormAttributeDto attribute = prepareAttributeThree();
		setValue(manually, attribute, Lists.newArrayList("1"));

		IdmIdentityRoleDto duplicated = service.getDuplicated(manually, automatic, null);
		assertNull(duplicated);

		duplicated = service.getDuplicated(manually, automatic, Boolean.TRUE);
		assertNotNull(duplicated);
		assertEquals(manually.getId(), duplicated.getId());
	}

	@Test
	public void testTwoDiffContract() {
		IdmIdentityContractDto contractOne = new IdmIdentityContractDto(UUID.randomUUID());
		IdmIdentityContractDto contractTwo = new IdmIdentityContractDto(UUID.randomUUID());
		IdmRoleDto role = new IdmRoleDto(UUID.randomUUID());

		IdmIdentityRoleDto one = prepareIdentityRole(contractOne, role, null, null);
		IdmIdentityRoleDto two = prepareIdentityRole(contractTwo, role, null, null);

		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		Assert.assertNull(duplicated);
	}

	@Test
	public void testTwoDiffRoles() {
		IdmIdentityContractDto contract = new IdmIdentityContractDto(UUID.randomUUID());
		
		IdmRoleDto roleOne = new IdmRoleDto(UUID.randomUUID());
		IdmRoleDto roleTwo = new IdmRoleDto(UUID.randomUUID());

		IdmIdentityRoleDto one = prepareIdentityRole(contract, roleOne, null, null);
		IdmIdentityRoleDto two = prepareIdentityRole(contract, roleTwo, null, null);

		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		Assert.assertNull(duplicated);
	}

	@Test
	public void testDuplicatedWithFakeIdentityRolesWithId() {
		IdmRoleDto role = new IdmRoleDto(UUID.randomUUID());
		IdmIdentityContractDto contract = new IdmIdentityContractDto(UUID.randomUUID());

		IdmIdentityRoleDto one = new IdmIdentityRoleDto(UUID.randomUUID());
		one.setIdentityContract(contract.getId());
		one.setIdentityContractDto(contract);
		one.setRole(role.getId());
		one.setCreated(ZonedDateTime.now().minusDays(5));

		IdmIdentityRoleDto two = new IdmIdentityRoleDto(UUID.randomUUID());
		two.setIdentityContract(contract.getId());
		two.setIdentityContractDto(contract);
		two.setRole(role.getId());
		two.setCreated(ZonedDateTime.now().minusDays(10));

		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		assertNotNull(duplicated);
		assertEquals(one, duplicated);
	}

	@Test
	public void testDuplicatedWithFakeIdentityRolesWithoutId() {
		IdmRoleDto role = new IdmRoleDto(UUID.randomUUID());
		IdmIdentityContractDto contract = new IdmIdentityContractDto(UUID.randomUUID());

		IdmIdentityRoleDto one = new IdmIdentityRoleDto();
		one.setIdentityContract(contract.getId());
		one.setIdentityContractDto(contract);
		one.setRole(role.getId());
		one.setCreated(ZonedDateTime.now().minusDays(5));

		IdmIdentityRoleDto two = new IdmIdentityRoleDto();
		two.setIdentityContract(contract.getId());
		two.setIdentityContractDto(contract);
		two.setRole(role.getId());
		two.setCreated(ZonedDateTime.now().minusDays(10));

		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		assertNotNull(duplicated);
	}

	@Test
	public void testDuplicatedWithFakeIdentityRolesWithValid() {
		IdmRoleDto role = new IdmRoleDto(UUID.randomUUID());
		IdmIdentityContractDto contract = new IdmIdentityContractDto(UUID.randomUUID());

		IdmIdentityRoleDto one = new IdmIdentityRoleDto();
		one.setIdentityContract(contract.getId());
		one.setIdentityContractDto(contract);
		one.setRole(role.getId());
		one.setValidFrom(LocalDate.now().plusDays(10));
		one.setCreated(ZonedDateTime.now().minusDays(5));

		IdmIdentityRoleDto two = new IdmIdentityRoleDto();
		two.setIdentityContract(contract.getId());
		two.setIdentityContractDto(contract);
		two.setValidTill(LocalDate.now().plusDays(5));
		two.setRole(role.getId());
		two.setCreated(ZonedDateTime.now().minusDays(10));

		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		assertNull(duplicated);
	}
	
	private IdmFormInstanceDto setValue(IdmIdentityRoleDto identityRole, IdmFormAttributeDto attribute, Serializable value) {
		IdmFormInstanceDto formInstance;
		if (identityRole.getEavs().isEmpty()) {
			formInstance = new IdmFormInstanceDto();
			IdmFormDefinitionDto subdefinition = new IdmFormDefinitionDto(UUID.randomUUID());
			subdefinition.setFormAttributes(Lists.newArrayList(attribute));
			formInstance.setFormDefinition(subdefinition);
		} else {
			formInstance = identityRole.getEavs().get(0);
			if (formInstance.getMappedAttributeByCode(attribute.getCode()) == null) {
				formInstance.getFormDefinition().addFormAttribute(attribute);
			}
		}
		
		// Remove current attribute
		List<IdmFormValueDto> values = new ArrayList<>(formInstance.getValues());
		values.removeIf(val -> val.getFormAttribute().equals(attribute.getId()));
		//
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
		
		formInstance.setValues(values);

		// Set back to identity role eavs attributes
		identityRole.setEavs(Lists.newArrayList(formInstance));
		//
		return formInstance;
	}

	private IdmFormAttributeDto prepareAttributeOne() {
		IdmFormAttributeDto one = new IdmFormAttributeDto(ATTRIBUTE_ONE.toString());
		one.setId(ATTRIBUTE_ONE);
		one.setPersistentType(PersistentType.LONG);
		one.setDefaultValue(String.valueOf(ATTRIBUTE_ONE_DEFAULT_VALUE));
		one.setRequired(false);
		return one;
	}

	private IdmFormAttributeDto prepareAttributeTwo() {
		IdmFormAttributeDto two = new IdmFormAttributeDto(ATTRIBUTE_TWO.toString());
		two.setId(ATTRIBUTE_TWO);
		two.setPersistentType(PersistentType.DATE);
		two.setRequired(false);
		return two;
	}

	private IdmFormAttributeDto prepareAttributeThree() {
		IdmFormAttributeDto three = new IdmFormAttributeDto(ATTRIBUTE_THREE.toString());
		three.setId(ATTRIBUTE_THREE);
		three.setPersistentType(PersistentType.SHORTTEXT);
		three.setMultiple(true);
		three.setRequired(false);
		return three;
	}
	
	private List<IdmIdentityRoleDto> createAndCheckDuplicitIdentityRole(
			LocalDate validFromOne, LocalDate validTillOne,
			LocalDate validFromTwo, LocalDate validTillTwo, 
			Boolean firstDuplicit) {
		// assigned roles owner
		IdmIdentityContractDto contract = new IdmIdentityContractDto(UUID.randomUUID());
		IdmRoleDto role = new IdmRoleDto(UUID.randomUUID());
		//
		IdmIdentityRoleDto one = prepareIdentityRole(contract, role, validFromOne, validTillOne);
		IdmIdentityRoleDto two = prepareIdentityRole(contract, role, validFromTwo, validTillTwo);
		List<IdmIdentityRoleDto> allByIdentity = Lists.newArrayList(one, two);
		//
		// eval
		IdmIdentityRoleDto duplicated = service.getDuplicated(one, two, null);
		//
		if (firstDuplicit == null) {
			Assert.assertNull(duplicated);
		} else if (BooleanUtils.isTrue(firstDuplicit)) {
			Assert.assertNotNull(duplicated);
			Assert.assertEquals(one.getId(), duplicated.getId());
		} else {
			Assert.assertNotNull(duplicated);
			Assert.assertEquals(two.getId(), duplicated.getId());
		}

		// Different order
		duplicated = service.getDuplicated(two, one, null);
		
		if (firstDuplicit == null) {
			Assert.assertNull(duplicated);
		} else if (BooleanUtils.isTrue(firstDuplicit)) {
			Assert.assertNotNull(duplicated);
			Assert.assertEquals(one.getId(), duplicated.getId());
		} else {
			Assert.assertNotNull(duplicated);
			Assert.assertEquals(two.getId(), duplicated.getId());
		}

		return allByIdentity;
	}
	
	private List<IdmIdentityRoleDto> createAndCheckDuplicityAutomaticIdentityRole(LocalDate validFromOne, LocalDate validTillOne,
			LocalDate validFromTwo, LocalDate validTillTwo, boolean manuallyDuplicit) {
		IdmRoleDto role = new IdmRoleDto(UUID.randomUUID());
		//
		return createAndCheckDuplicityAutomaticIdentityRole(role, validFromOne, validTillOne, validFromTwo, validTillTwo, manuallyDuplicit);
	}

	private List<IdmIdentityRoleDto> createAndCheckDuplicityAutomaticIdentityRole(
			IdmRoleDto role, 
			LocalDate validFromOne, LocalDate validTillOne,
			LocalDate validFromTwo, LocalDate validTillTwo, 
			boolean manuallyDuplicit) {
		// assigned roles owner
		IdmIdentityContractDto contract = new IdmIdentityContractDto(UUID.randomUUID());
		contract.setValidFrom(validFromOne);
		contract.setValidTill(validTillOne);
		//
		IdmIdentityRoleDto automatic = prepareIdentityRole(contract, role, validFromOne, validTillOne);
		automatic.setAutomaticRole(UUID.randomUUID());
		//
		IdmIdentityRoleDto manually = prepareIdentityRole(contract, role, validFromTwo, validTillTwo);

		List<IdmIdentityRoleDto> allByIdentity = Lists.newArrayList(automatic, manually);
		Assert.assertEquals(2, allByIdentity.size());

		// Fill eavs (if exists)
		automatic = fillEavs(automatic);
		manually = fillEavs(manually);

		IdmIdentityRoleDto duplicated = service.getDuplicated(manually, automatic, null);
		if (manuallyDuplicit) {
			Assert.assertNotNull(duplicated);
			Assert.assertEquals(manually.getId(), duplicated.getId());
			Assert.assertNotEquals(automatic.getId(), duplicated.getId());
		} else {
			Assert.assertNull(duplicated);
		}

		duplicated = service.getDuplicated(automatic, manually, null);
		if (manuallyDuplicit) {
			Assert.assertNotNull(duplicated);
			Assert.assertEquals(manually.getId(), duplicated.getId());
			Assert.assertNotEquals(automatic.getId(), duplicated.getId());
		} else {
			Assert.assertNull(duplicated);
		}

		return allByIdentity;
	}

	private IdmIdentityRoleDto fillEavs(IdmIdentityRoleDto identityRole) {
		// TODO ... load eavs ...
		
		
		return identityRole;
	}
	
	
	private IdmIdentityRoleDto prepareIdentityRole(IdmIdentityContractDto identityContract, IdmRoleDto role, LocalDate validFrom, LocalDate validTill) {
		IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto(UUID.randomUUID());
		identityRole.setIdentityContractDto(identityContract);
		identityRole.setRole(role.getId());
		identityRole.setValidFrom(validFrom);
		identityRole.setValidTill(validTill);
		identityRole.setCreated(ZonedDateTime.now());
		//
		// tests (e.g. deduplications) uses created date for removing duplicate roles - artificial slow down is here just for this purposes
		// FIXME: fix all deduplication tests dependent on created date
		getLong();
		//
		return identityRole;
	}
	
	/**
	 * FIXME: fix all deduplication tests dependent on created date
	 * 
	 * @return
	 */
	private long getLong() {
		try {
			Thread.sleep(1);
		} catch (InterruptedException ex) {
			throw new CoreException(ex);
		}
		return System.currentTimeMillis();
	}

}
