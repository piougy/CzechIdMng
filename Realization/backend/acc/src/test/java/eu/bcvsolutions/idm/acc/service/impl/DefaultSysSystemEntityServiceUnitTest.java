package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.identityconnectors.framework.common.objects.ObjectClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;

import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.repository.AccAccountRepository;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningOperationRepository;
import eu.bcvsolutions.idm.acc.repository.SysSystemEntityRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.impl.IcObjectClassImpl;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Unit test for {@link DefaultSysSystemEntityService}
 *
 * @author Peter Sourek <peter.sourek@bcvsolutions.eu>
 */
public class DefaultSysSystemEntityServiceUnitTest extends AbstractUnitTest {

	@Mock
	SysSystemEntityRepository systemEntityRepository;
	@Mock
	AccAccountRepository accountRepository;
	@Mock
	SysProvisioningOperationRepository provisioningOperationRepository;
	@Mock
	SysSystemService systemService;
	@Mock
	DefaultSysSystemMappingService mappingService;
	@Mock
	SysSchemaObjectClassService objectClassService;
	@Mock
	ModelMapper objectMapper;
	@InjectMocks
	DefaultSysSystemEntityService service;

	@Test
	public void getObjectClassForSystemEntityNoMapping() throws Exception {
		SysSystemEntityDto dto = new SysSystemEntityDto();

		IcObjectClass result = service.getObjectClassForSystemEntity(dto);
		assertNull(result);
	}

	@Test
	public void getObjectClassForSystemEntityProvisioningMapping() throws Exception {
		final UUID SYSTEM_ID = UUID.randomUUID();
		final UUID OBJECT_CLASS_ID = UUID.randomUUID();
		SysSystemMappingDto sysSystemMappingDto = new SysSystemMappingDto();
		sysSystemMappingDto.setObjectClass(OBJECT_CLASS_ID);
		SysSystemEntityDto dto = new SysSystemEntityDto();
		dto.setSystem(SYSTEM_ID);
		SysSchemaObjectClassDto objectClassDto = new SysSchemaObjectClassDto();
		objectClassDto.setObjectClassName(ObjectClass.ACCOUNT_NAME);
		sysSystemMappingDto.setObjectClass(OBJECT_CLASS_ID);
		//
		when(mappingService.findBySystemId(eq(SYSTEM_ID), eq(SystemOperationType.PROVISIONING), any())).thenReturn(Collections.singletonList(sysSystemMappingDto));
		when(objectClassService.get(eq(OBJECT_CLASS_ID))).thenReturn(objectClassDto);
		//
		IcObjectClass result = service.getObjectClassForSystemEntity(dto);
		assertNotNull(result);
		assertEquals(result, new IcObjectClassImpl(ObjectClass.ACCOUNT_NAME));
	}

	@Test
	public void getObjectClassForSystemEntityProvisioningMappingSyncMappings() throws Exception {
		final UUID SYSTEM_ID = UUID.randomUUID();
		final UUID OBJECT_CLASS_ID = UUID.randomUUID();
		final UUID OBJECT_CLASS_ID2 = UUID.randomUUID();
		final UUID OBJECT_CLASS_ID3 = UUID.randomUUID();

		SysSystemMappingDto sysSystemMappingDto = new SysSystemMappingDto();
		sysSystemMappingDto.setObjectClass(OBJECT_CLASS_ID);

		SysSystemMappingDto sysSystemMappingDto2 = new SysSystemMappingDto();
		sysSystemMappingDto2.setObjectClass(OBJECT_CLASS_ID2);

		SysSystemMappingDto sysSystemMappingDto3 = new SysSystemMappingDto();
		sysSystemMappingDto3.setObjectClass(OBJECT_CLASS_ID3);

		SysSystemEntityDto dto = new SysSystemEntityDto();
		dto.setSystem(SYSTEM_ID);

		SysSchemaObjectClassDto objectClassDto = new SysSchemaObjectClassDto();
		objectClassDto.setObjectClassName(ObjectClass.ACCOUNT_NAME);

		SysSchemaObjectClassDto objectClassDto2 = new SysSchemaObjectClassDto();
		objectClassDto2.setObjectClassName(ObjectClass.GROUP_NAME);

		//
		when(mappingService.findBySystemId(eq(SYSTEM_ID), eq(SystemOperationType.PROVISIONING), any())).thenReturn(Collections.singletonList(sysSystemMappingDto));
		when(mappingService.findBySystemId(eq(SYSTEM_ID), eq(SystemOperationType.SYNCHRONIZATION), any()))
			.thenReturn(Stream.of(sysSystemMappingDto2, sysSystemMappingDto3).collect(Collectors.toList()));
		when(objectClassService.get(eq(OBJECT_CLASS_ID))).thenReturn(objectClassDto);
		when(objectClassService.get(eq(OBJECT_CLASS_ID2))).thenReturn(objectClassDto2);
		when(objectClassService.get(eq(OBJECT_CLASS_ID3))).thenReturn(objectClassDto2);
		//
		IcObjectClass result = service.getObjectClassForSystemEntity(dto);
		assertNotNull(result);
		assertEquals(result, new IcObjectClassImpl(ObjectClass.ACCOUNT_NAME));
	}

	@Test
	public void getObjectClassForSystemEntitySyncMappings() throws Exception {
		final UUID SYSTEM_ID = UUID.randomUUID();
		final UUID OBJECT_CLASS_ID = UUID.randomUUID();
		final UUID OBJECT_CLASS_ID2 = UUID.randomUUID();


		SysSystemMappingDto sysSystemMappingDto = new SysSystemMappingDto();
		sysSystemMappingDto.setObjectClass(OBJECT_CLASS_ID);

		SysSystemMappingDto sysSystemMappingDto2 = new SysSystemMappingDto();
		sysSystemMappingDto2.setObjectClass(OBJECT_CLASS_ID2);

		SysSystemEntityDto dto = new SysSystemEntityDto();
		dto.setSystem(SYSTEM_ID);

		SysSchemaObjectClassDto objectClassDto = new SysSchemaObjectClassDto();
		objectClassDto.setObjectClassName(ObjectClass.GROUP_NAME);

		SysSchemaObjectClassDto objectClassDto2 = new SysSchemaObjectClassDto();
		objectClassDto2.setObjectClassName(ObjectClass.GROUP_NAME);

		//
		when(mappingService.findBySystemId(eq(SYSTEM_ID), eq(SystemOperationType.PROVISIONING), any())).thenReturn(Collections.emptyList());
		when(mappingService.findBySystemId(eq(SYSTEM_ID), eq(SystemOperationType.SYNCHRONIZATION), any()))
			.thenReturn(Stream.of(sysSystemMappingDto2, sysSystemMappingDto).collect(Collectors.toList()));
		when(objectClassService.get(eq(OBJECT_CLASS_ID))).thenReturn(objectClassDto);
		when(objectClassService.get(eq(OBJECT_CLASS_ID2))).thenReturn(objectClassDto2);
		//
		IcObjectClass result = service.getObjectClassForSystemEntity(dto);
		assertNotNull(result);
		assertEquals(result, new IcObjectClassImpl(ObjectClass.GROUP_NAME));
	}


}