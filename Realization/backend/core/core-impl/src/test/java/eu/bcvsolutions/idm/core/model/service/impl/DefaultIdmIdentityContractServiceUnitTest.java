package eu.bcvsolutions.idm.core.model.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import eu.bcvsolutions.idm.core.api.config.domain.TreeConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.config.domain.EntityToUuidConverter;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeTypeRepository;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Identity contract unit tests:
 * - prime contract
 * - contract state
 * 
 * @author Radek Tomiška
 */
public class DefaultIdmIdentityContractServiceUnitTest extends AbstractUnitTest {

	@Mock private IdmIdentityContractRepository repository;
	@Mock private FormService formService;
	@Mock private EntityEventManager entityEventManager;
	@Mock private IdmTreeTypeRepository treeTypeRepository;
	@Mock private TreeConfiguration treeConfiguration;
	@Mock private IdmTreeNodeRepository treeNodeRepository;
	@Mock private IdmContractSliceService contractSliceService;
	@Spy private ModelMapper modelMapper = new ModelMapper();
	//
	@InjectMocks 
	private DefaultIdmIdentityContractService service;
	
	@Before
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void init() {
		Converter<? extends BaseEntity, UUID> entityToUiid = new EntityToUuidConverter(modelMapper, null);
		TypeMap typeMapEntityToUiid = modelMapper.createTypeMap(IdmTreeNode.class, UUID.class);
		typeMapEntityToUiid.setConverter(entityToUiid);
		TypeMap typeMapEntityToUiid2 = modelMapper.createTypeMap(IdmTreeType.class, UUID.class);
		typeMapEntityToUiid2.setConverter(entityToUiid);
	}
	
	@Test
	public void testSimplePrimeContract() {
		List<IdmIdentityContract> contracts = new ArrayList<>();
		IdmIdentityContract otherContract = new IdmIdentityContract(UUID.randomUUID());
		otherContract.setMain(false);
		IdmIdentityContract mainContract = new IdmIdentityContract(UUID.randomUUID());
		mainContract.setMain(true);
		contracts.add(otherContract);
		contracts.add(mainContract);
		//
		when(repository.findAllByIdentity_Id(any(UUID.class), any())).thenReturn(contracts);	
		when(treeConfiguration.getDefaultType()).thenReturn(null);
		when(contractSliceService.find(any(IdmContractSliceFilter.class), (PageRequest) any())).thenReturn(new PageImpl<IdmContractSliceDto>(new ArrayList<>()));
		
		//
		Assert.assertEquals(mainContract.getId(), service.getPrimeContract(UUID.randomUUID()).getId());
	}
	
	@Test
	public void testPrimeContractWithWorkingPosition() {
		List<IdmIdentityContract> contracts = new ArrayList<>();
		IdmIdentityContract contractWithPosition = new IdmIdentityContract(UUID.randomUUID());
		contractWithPosition.setMain(false);
		IdmTreeNode workPosition = new IdmTreeNode();
		workPosition.setId(UUID.randomUUID());
		workPosition.setTreeType(new IdmTreeType(UUID.randomUUID()));
		contractWithPosition.setWorkPosition(workPosition);
		//
		IdmIdentityContract otherContract = new IdmIdentityContract(UUID.randomUUID());
		otherContract.setMain(false);
		contracts.add(contractWithPosition);
		contracts.add(otherContract);
		//
		when(repository.findAllByIdentity_Id(any(UUID.class), any())).thenReturn(contracts);		
		when(treeConfiguration.getDefaultType()).thenReturn(null);
		when(contractSliceService.find(any(IdmContractSliceFilter.class), (PageRequest) any())).thenReturn(new PageImpl<IdmContractSliceDto>(new ArrayList<>()));
		
		//
		Assert.assertEquals(contractWithPosition.getId(), service.getPrimeContract(UUID.randomUUID()).getId());
	}
	
	@Test
	public void testPrimeContractWithDefaultTreeType() {
		List<IdmIdentityContract> contracts = new ArrayList<>();
		IdmIdentityContract contractWithDefaultPosition = new IdmIdentityContract(UUID.randomUUID());
		contractWithDefaultPosition.setMain(false);
		IdmTreeNode defaultWorkPosition = new IdmTreeNode();
		defaultWorkPosition.setId(UUID.randomUUID());
		IdmTreeType defaultTreeType = new IdmTreeType(UUID.randomUUID());
		defaultWorkPosition.setTreeType(defaultTreeType);
		contractWithDefaultPosition.setWorkPosition(defaultWorkPosition);
		//
		IdmIdentityContract otherContract = new IdmIdentityContract(UUID.randomUUID());
		otherContract.setMain(false);
		IdmTreeNode workPosition = new IdmTreeNode();
		workPosition.setId(UUID.randomUUID());
		workPosition.setTreeType(new IdmTreeType(UUID.randomUUID()));
		otherContract.setWorkPosition(workPosition);
		//
		contracts.add(contractWithDefaultPosition);
		contracts.add(otherContract);
		//
		when(repository.findAllByIdentity_Id(any(UUID.class), any())).thenReturn(contracts);		
		when(treeConfiguration.getDefaultType()).thenReturn(new IdmTreeTypeDto(defaultTreeType.getId()));
		when(contractSliceService.find(any(IdmContractSliceFilter.class), (PageRequest) any())).thenReturn(new PageImpl<IdmContractSliceDto>(new ArrayList<>()));
		
		//
		Assert.assertEquals(contractWithDefaultPosition.getId(), service.getPrimeContract(UUID.randomUUID()).getId());
	}
	
	@Test
	public void testSimpleValidPrimeContract() {
		List<IdmIdentityContract> contracts = new ArrayList<>();
		IdmIdentityContract invalidContract = new IdmIdentityContract(UUID.randomUUID());
		invalidContract.setMain(true);
		invalidContract.setValidFrom(LocalDate.now().plusDays(1));
		IdmIdentityContract mainContract = new IdmIdentityContract(UUID.randomUUID());
		mainContract.setMain(true);
		contracts.add(invalidContract);
		contracts.add(mainContract);
		//
		when(repository.findAllByIdentity_Id((UUID) any(), any())).thenReturn(contracts);		
		when(treeConfiguration.getDefaultType()).thenReturn(null);
		when(contractSliceService.find((IdmContractSliceFilter) any(), (PageRequest) any())).thenReturn(new PageImpl<IdmContractSliceDto>(new ArrayList<>()));
		//
		Assert.assertEquals(mainContract.getId(), service.getPrimeContract(UUID.randomUUID()).getId());
	}
	
	@Test
	public void testSimpleDisabledPrimeContract() {
		List<IdmIdentityContract> contracts = new ArrayList<>();
		IdmIdentityContract invalidContract = new IdmIdentityContract(UUID.randomUUID());
		invalidContract.setMain(true);
		invalidContract.setDisabled(true);
		IdmIdentityContract mainContract = new IdmIdentityContract(UUID.randomUUID());
		mainContract.setMain(true);
		contracts.add(invalidContract);
		contracts.add(mainContract);
		//
		when(repository.findAllByIdentity_Id(any(UUID.class), any())).thenReturn(contracts);		
		when(treeConfiguration.getDefaultType()).thenReturn(null);
		when(contractSliceService.find(any(IdmContractSliceFilter.class),  (PageRequest) any())).thenReturn(new PageImpl<IdmContractSliceDto>(new ArrayList<>()));
		
		//
		Assert.assertEquals(mainContract.getId(), service.getPrimeContract(UUID.randomUUID()).getId());
	}
	
	/**
	 * Invalid main contract has still higher priority
	 */
	@Test
	public void testDisabledMainContract() {
		List<IdmIdentityContract> contracts = new ArrayList<>();
		IdmIdentityContract invalidContract = new IdmIdentityContract(UUID.randomUUID());
		invalidContract.setMain(true);
		invalidContract.setDisabled(true);
		IdmIdentityContract otherContract = new IdmIdentityContract(UUID.randomUUID());
		otherContract.setMain(false);
		contracts.add(otherContract);
		contracts.add(invalidContract);
		//
		when(repository.findAllByIdentity_Id(any(UUID.class), any())).thenReturn(contracts);		
		when(treeConfiguration.getDefaultType()).thenReturn(null);
		when(contractSliceService.find(any(IdmContractSliceFilter.class), (PageRequest) any())).thenReturn(new PageImpl<IdmContractSliceDto>(new ArrayList<>()));
		
		//
		Assert.assertEquals(invalidContract.getId(), service.getPrimeContract(UUID.randomUUID()).getId());
	}
	
	@Test
	public void testOtherMainContractByFilledValidFrom() {
		List<IdmIdentityContract> contracts = new ArrayList<>();
		IdmIdentityContract oneContract = new IdmIdentityContract(UUID.randomUUID());
		oneContract.setValidFrom(LocalDate.now());
		oneContract.setMain(false);
		IdmIdentityContract twoContract = new IdmIdentityContract(UUID.randomUUID());
		twoContract.setMain(false);
		contracts.add(twoContract);
		contracts.add(oneContract);
		//
		when(repository.findAllByIdentity_Id(any(UUID.class), any())).thenReturn(contracts);		
		when(treeConfiguration.getDefaultType()).thenReturn(null);
		when(contractSliceService.find(any(IdmContractSliceFilter.class), (PageRequest) any())).thenReturn(new PageImpl<IdmContractSliceDto>(new ArrayList<>()));
		
		//
		Assert.assertEquals(twoContract.getId(), service.getPrimeContract(UUID.randomUUID()).getId());
	}
	
	@Test
	public void testOtherMainContractByValidFrom() {
		List<IdmIdentityContract> contracts = new ArrayList<>();
		IdmIdentityContract oneContract = new IdmIdentityContract(UUID.randomUUID());
		oneContract.setValidFrom(LocalDate.now().minusDays(2));
		oneContract.setMain(false);
		IdmIdentityContract twoContract = new IdmIdentityContract(UUID.randomUUID());
		twoContract.setMain(false);
		twoContract.setValidFrom(LocalDate.now().minusDays(1));
		contracts.add(twoContract);
		contracts.add(oneContract);
		//
		when(repository.findAllByIdentity_Id(any(UUID.class), any())).thenReturn(contracts);		
		when(treeConfiguration.getDefaultType()).thenReturn(null);
		when(contractSliceService.find(any(IdmContractSliceFilter.class), (PageRequest) any())).thenReturn(new PageImpl<IdmContractSliceDto>(new ArrayList<>()));
		//
		Assert.assertEquals(oneContract.getId(), service.getPrimeContract(UUID.randomUUID()).getId());
	}
}
