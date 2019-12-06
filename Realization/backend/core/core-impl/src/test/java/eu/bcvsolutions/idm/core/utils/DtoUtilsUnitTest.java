package eu.bcvsolutions.idm.core.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Common dto helpers test
 *
 * @author Radek Tomiška
 *
 */
public class DtoUtilsUnitTest extends AbstractUnitTest {

	private static final String PROPERTY = IdmIdentityRoleDto.PROPERTY_IDENTITY_CONTRACT; // IdmIdentityRole_.identityContract could not be used in unit test (see DtoUtilsIntegrationTest)

	@Test
	public void testEmbedded() {
		IdmIdentityRoleDto dto = new IdmIdentityRoleDto();
		IdmIdentityContractDto contract = new IdmIdentityContractDto(UUID.randomUUID());
		dto.getEmbedded().put(PROPERTY, contract);
		//
		IdmIdentityContractDto embedded = DtoUtils.getEmbedded(dto, PROPERTY, IdmIdentityContractDto.class);
		Assert.assertEquals(contract, embedded);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyEmbeddedWIthoutDefault() {
		IdmIdentityRoleDto dto = new IdmIdentityRoleDto();
		//
		DtoUtils.getEmbedded(dto, PROPERTY, IdmIdentityContractDto.class);
	}


	@Test
	public void testEmbeddedWithtDefault() {
		IdmIdentityRoleDto dto = new IdmIdentityRoleDto();
		//
		Assert.assertNull(DtoUtils.getEmbedded(dto, PROPERTY, IdmIdentityContractDto.class, null));
	}

	@Test
	public void testEmbeddedDto() {
		IdmIdentityRoleDto dto = new IdmIdentityRoleDto();
		IdmIdentityContractDto contract = new IdmIdentityContractDto(UUID.randomUUID());
		dto.getEmbedded().put(PROPERTY, contract);
		//
		IdmIdentityContractDto embedded = DtoUtils.getEmbedded(dto, PROPERTY);
		Assert.assertEquals(contract, embedded);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyEmbeddedWIthoutDefaultDto() {
		IdmIdentityRoleDto dto = new IdmIdentityRoleDto();
		//
		DtoUtils.getEmbedded(dto, PROPERTY);
	}

	@Test
	public void testEmbeddedWithtDefaultDto() {
		IdmIdentityRoleDto dto = new IdmIdentityRoleDto();
		//
		Assert.assertNull(DtoUtils.getEmbedded(dto, PROPERTY, (IdmIdentityRoleDto) null));
	}

	@Test
	public void testToUuid() {
		UUID uuid = UUID.randomUUID();
		//
		Assert.assertEquals(uuid, DtoUtils.toUuid(uuid.toString()));
		Assert.assertEquals(uuid, DtoUtils.toUuid(uuid));
	}

	@Test
	public void testToZonedDateTime() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		//
		Assert.assertNull(DtoUtils.toZonedDateTime(null));
		Assert.assertEquals(now, DtoUtils.toZonedDateTime(now));
		Assert.assertEquals(now, DtoUtils.toZonedDateTime(org.joda.time.DateTime.now().withMillis(now.toInstant().toEpochMilli())));
	}

	@Test
	public void testToLocalDate() {
		LocalDate now = LocalDate.now();
		//
		Assert.assertNull(DtoUtils.toLocalDate(null));
		Assert.assertEquals(now, DtoUtils.toLocalDate(now));
		Assert.assertEquals(now, DtoUtils.toLocalDate(org.joda.time.LocalDate.fromDateFields(
				new Date(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()))));
	}

	@Test
	public void testClearAuditFields() {
		IdmIdentityDto auditable = new IdmIdentityDto();
		auditable.setId(UUID.randomUUID());
		auditable.setCreated(ZonedDateTime.now());
		auditable.setCreator("mock");
		auditable.setCreatorId(UUID.randomUUID());
		auditable.setModified(ZonedDateTime.now());
		auditable.setModifier("mock");
		auditable.setModifierId(UUID.randomUUID());
		auditable.setOriginalCreator("mock");
		auditable.setOriginalCreatorId(UUID.randomUUID());
		auditable.setOriginalModifier("mock");
		auditable.setOriginalModifierId(UUID.randomUUID());
		auditable.setTransactionId(UUID.randomUUID());
		//
		DtoUtils.clearAuditFields(auditable);
		//
		Assert.assertNotNull(auditable.getId()); // identifier is not cleared
		Assert.assertNull(auditable.getCreated());
		Assert.assertNull(auditable.getCreator());
		Assert.assertNull(auditable.getCreatorId());
		Assert.assertNull(auditable.getModified());
		Assert.assertNull(auditable.getModifier());
		Assert.assertNull(auditable.getModifierId());
		Assert.assertNull(auditable.getOriginalCreator());
		Assert.assertNull(auditable.getOriginalCreatorId());
		Assert.assertNull(auditable.getOriginalModifier());
		Assert.assertNull(auditable.getOriginalModifierId());
		Assert.assertNull(auditable.getTransactionId());
	}
}
