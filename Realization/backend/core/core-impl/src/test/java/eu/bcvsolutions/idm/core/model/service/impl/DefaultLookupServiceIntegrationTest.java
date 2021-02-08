package eu.bcvsolutions.idm.core.model.service.impl;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Default lookup service test:
 * - get dto by uuid / code
 * - get entity by uuid / code
 * - get default owner type / id
 * - get embedded dto
 *
 * @author Radek Tomiška
 *
 */
@Transactional
public class DefaultLookupServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired private ApplicationContext context;
	//
	private DefaultLookupService lookupService;

	@Before
	public void init() {
		lookupService = context.getAutowireCapableBeanFactory().createBean(DefaultLookupService.class);
	}

	@Test
	public void testIdentityLookupByUuid() {
		IdmIdentityDto dto = getHelper().createIdentity((GuardedString) null);
		//
		// by dto class
		Assert.assertEquals(dto.getId(), lookupService.lookupDto(IdmIdentityDto.class, dto.getId()).getId());
		Assert.assertEquals(dto.getId(), lookupService.lookupEntity(IdmIdentityDto.class, dto.getId()).getId());
		// by entity class
		Assert.assertEquals(dto.getId(), lookupService.lookupDto(IdmIdentity.class, dto.getId()).getId());
		Assert.assertEquals(dto.getId(), lookupService.lookupEntity(IdmIdentity.class, dto.getId()).getId());
		// by string class
		Assert.assertEquals(dto.getId(), lookupService.lookupDto(IdmIdentity.class.getCanonicalName(), dto.getId()).getId());
		Assert.assertEquals(dto.getId(), lookupService.lookupDto(IdmIdentityDto.class.getCanonicalName(), dto.getId()).getId());
	}

	@Test
	public void testIdentityLookupByCode() {
		IdmIdentityDto dto = getHelper().createIdentity((GuardedString) null);
		//
		// by dto class
		Assert.assertEquals(dto.getId(), lookupService.lookupDto(IdmIdentityDto.class, dto.getCode()).getId());
		Assert.assertEquals(dto.getId(), lookupService.lookupEntity(IdmIdentityDto.class, dto.getCode()).getId());
		// by entity class
		Assert.assertEquals(dto.getId(), lookupService.lookupDto(IdmIdentity.class, dto.getCode()).getId());
		Assert.assertEquals(dto.getId(), lookupService.lookupEntity(IdmIdentity.class, dto.getCode()).getId());
		// by string class
		Assert.assertEquals(dto.getId(), lookupService.lookupDto(IdmIdentity.class.getCanonicalName(), dto.getCode()).getId());
		Assert.assertEquals(dto.getId(), lookupService.lookupDto(IdmIdentityDto.class.getCanonicalName(), dto.getCode()).getId());
	}

	@Test
	public void testIdentityLookupByStringUuid() {
		IdmIdentityDto dto = getHelper().createIdentity((GuardedString) null);
		//
		// by dto class
		Assert.assertEquals(dto.getId(), lookupService.lookupDto(IdmIdentityDto.class, dto.getId().toString()).getId());
		Assert.assertEquals(dto.getId(), lookupService.lookupEntity(IdmIdentityDto.class, dto.getId().toString()).getId());
		// by entity class
		Assert.assertEquals(dto.getId(), lookupService.lookupDto(IdmIdentity.class, dto.getId().toString()).getId());
		Assert.assertEquals(dto.getId(), lookupService.lookupEntity(IdmIdentity.class, dto.getId().toString()).getId());
		// by string class
		Assert.assertEquals(dto.getId(), lookupService.lookupDto(IdmIdentity.class.getCanonicalName(), dto.getId().toString()).getId());
		Assert.assertEquals(dto.getId(), lookupService.lookupDto(IdmIdentityDto.class.getCanonicalName(), dto.getId().toString()).getId());
	}

	@Test
	public void testGetOwnerId() {
		IdmIdentityDto dto = getHelper().createIdentity((GuardedString) null);
		//
		Assert.assertEquals(dto.getId(), lookupService.getOwnerId(dto));
		Assert.assertNull(lookupService.getOwnerId(new IdmIdentityDto()));
	}

	@Test
	public void testGetOwnerType() {
		IdmIdentityDto owner = getHelper().createIdentity((GuardedString) null);
		//
		Assert.assertEquals(IdmIdentity.class.getCanonicalName(), lookupService.getOwnerType(IdmIdentity.class));
		Assert.assertEquals(IdmIdentity.class.getCanonicalName(), lookupService.getOwnerType(IdmIdentityDto.class));
		//
		Assert.assertEquals(IdmIdentity.class.getCanonicalName(), lookupService.getOwnerType(owner));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetWrongOwnerType() {
		lookupService.getOwnerType(ModuleDescriptorDto.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testWrongIdentifiableType() {
		IdmIdentityDto dto = getHelper().createIdentity((GuardedString) null);
		//
		lookupService.lookupDto("wrongType", dto.getId());
	}
	
	@Test
	public void testLookupEmbeddedPresent() {
		IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
		IdmIdentityContractDto contract = new IdmIdentityContractDto(UUID.randomUUID());
		//
		identityRole.setIdentityContractDto(contract);
		//
		IdmIdentityContractDto lookupContract = lookupService.lookupEmbeddedDto(identityRole, IdmIdentityRoleDto.PROPERTY_IDENTITY_CONTRACT);
		//
		Assert.assertNotNull(lookupContract);
		Assert.assertEquals(contract, lookupContract);
	}
	
	@Test
	public void testLookupEmbeddedNotPresent() {
		IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(getHelper().createIdentity((GuardedString) null));
		//
		identityRole.setIdentityContract(contract.getId());
		//
		IdmIdentityContractDto lookupContract = lookupService.lookupEmbeddedDto(identityRole, IdmIdentityRoleDto.PROPERTY_IDENTITY_CONTRACT);
		//
		Assert.assertNotNull(lookupContract);
		Assert.assertEquals(contract, lookupContract);
	}
	
	@Test
	public void testLookupEmbeddedNotExist() {
		IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
		//
		identityRole.setIdentityContract(UUID.randomUUID());
		//
		IdmIdentityContractDto lookupContract = lookupService.lookupEmbeddedDto(identityRole, IdmIdentityRoleDto.PROPERTY_IDENTITY_CONTRACT);
		//
		Assert.assertNull(lookupContract);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testLookupEmbeddedNotEmbeddable() {
		IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
		//
		identityRole.setValidFrom(LocalDate.now());
		//
		lookupService.lookupEmbeddedDto(identityRole, IdmIdentityRole_.validTill);
	}
	
	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void testLookupEmbeddedNotSerializable() {
		AbstractDto mockDto = new AbstractDto() {

			private static final long serialVersionUID = 1L;
			
			@Embedded(dtoClass = IdmIdentityRoleDto.class)
			private GuardedString wrong = new GuardedString("mock");
			
			
			public GuardedString getWrong() {
				return wrong;
			}
			
			public void setWrong(GuardedString wrong) {
				this.wrong = wrong;
			}
		};
		//
		lookupService.lookupEmbeddedDto(mockDto, "wrong");
	}
	
	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void testLookupEmbeddedReflectionFailed() {
		AbstractDto mockDto = new AbstractDto() {

			private static final long serialVersionUID = 1L;
			
			@Embedded(dtoClass = IdmIdentityRoleDto.class)
			private IdmIdentityRoleDto wrong;
			
			
			public IdmIdentityRoleDto getWrong() {
				throw new UnsupportedOperationException("mock");
			}
			
			public void setWrong(IdmIdentityRoleDto wrong) {
				this.wrong = wrong;
			}
		};
		//
		lookupService.lookupEmbeddedDto(mockDto, "wrong");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testLookupEntityLookupNotExist() {
		AbstractDto mockDto = new AbstractDto() {
			private static final long serialVersionUID = 1L;
		};
		//
		Assert.assertNull(lookupService.lookupEntity(mockDto.getClass(), "wrong"));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testLookupDtoLookupNotExist() {
		AbstractDto mockDto = new AbstractDto() {
			private static final long serialVersionUID = 1L;
		};
		//
		Assert.assertNull(lookupService.lookupDto(mockDto.getClass(), "wrong"));
	}
	
	@Test
	public void testLookupEntityNotExist() {
		Assert.assertNull(lookupService.lookupEntity(IdmIdentityRoleDto.class, UUID.randomUUID()));
	}
	
	@Test
	public void testLookupDtoNotExist() {
		Assert.assertNull(lookupService.lookupDto(IdmIdentityRoleDto.class, UUID.randomUUID()));
	}
	
	@Test
	public void testLookupByExampleLookupNotRegistered() {
		Assert.assertNull(lookupService.lookupByExample(new IdmIdentityDto(UUID.randomUUID())));
	}
	
	@Test
	public void testLookupTreeNodeByExample() {
		IdmTreeTypeDto treeTypeOne = getHelper().createTreeType();
		IdmTreeTypeDto treeTypeTwo = getHelper().createTreeType();
		IdmTreeNodeDto nodeOne = getHelper().createTreeNode(treeTypeOne, null);
		IdmTreeNodeDto nodeTwo = getHelper().createTreeNode(treeTypeOne, null);
		IdmTreeNodeDto nodeTwoOther = getHelper().createTreeNode(treeTypeTwo, nodeTwo.getCode(), null);
		//
		IdmTreeNodeDto example = new IdmTreeNodeDto();
		Assert.assertNull(lookupService.lookupByExample(example)); // more results => example is not specified correctly
		//
		example.setCode(getHelper().createName());
		Assert.assertNull(lookupService.lookupByExample(example)); // no results => not found
		//
		example.setCode(nodeOne.getCode());
		Assert.assertEquals(nodeOne, lookupService.lookupByExample(example)); // one is unique by code in all types
		//
		example.setCode(nodeTwo.getCode());
		Assert.assertNull(lookupService.lookupByExample(example)); // more results in different tree type => example is not specified correctly
		//
		example.setTreeType(treeTypeOne.getId());
		Assert.assertEquals(nodeTwo, lookupService.lookupByExample(example));
		//
		IdmTreeTypeDto treeTypeExample = new IdmTreeTypeDto();
		treeTypeExample.setCode(treeTypeTwo.getCode());
		example.getEmbedded().put(IdmTreeNode_.treeType.getName(), treeTypeExample);
		Assert.assertEquals(nodeTwoOther, lookupService.lookupByExample(example));
	}
}
