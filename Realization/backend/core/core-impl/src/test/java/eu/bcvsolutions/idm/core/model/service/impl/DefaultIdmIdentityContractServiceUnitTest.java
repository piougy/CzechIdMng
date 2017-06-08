package eu.bcvsolutions.idm.core.model.service.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.modelmapper.ModelMapper;

import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeTypeRepository;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Identity contract tests
 * 
 * @author Radek Tomiška
 */
public class DefaultIdmIdentityContractServiceUnitTest extends AbstractUnitTest {

	@Mock 
	private IdmIdentityContractRepository repository;
	@Mock 
	private FormService formService;
	@Mock 
	private EntityEventManager entityEventManager;
	@Mock 
	private IdmTreeTypeRepository treeTypeRepository;
	@Mock 
	private IdmTreeNodeRepository treeNodeRepository;
	@Spy 
	private ModelMapper modelMapper = new ModelMapper();
	@InjectMocks 
	private DefaultIdmIdentityContractService service;
	
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
		when(treeTypeRepository.findOneByDefaultTreeTypeIsTrue()).thenReturn(null);
		//
		Assert.assertEquals(mainContract.getId(), service.getPrimeContract(UUID.randomUUID()).getId());
	}
	
	@Test
	public void testPrimeContractWithWorkingPosition() {
		List<IdmIdentityContract> contracts = new ArrayList<>();
		IdmIdentityContract otherContract = new IdmIdentityContract(UUID.randomUUID());
		otherContract.setMain(false);
		IdmIdentityContract contractWithPosition = new IdmIdentityContract(UUID.randomUUID());
		contractWithPosition.setMain(false);
		IdmTreeNode workPosition = new IdmTreeNode();
		workPosition.setTreeType(new IdmTreeType());
		contractWithPosition.setWorkPosition(workPosition);
		contracts.add(otherContract);
		contracts.add(contractWithPosition);
		//
		when(repository.findAllByIdentity_Id(any(UUID.class), any())).thenReturn(contracts);		
		when(treeTypeRepository.findOneByDefaultTreeTypeIsTrue()).thenReturn(null);
		//
		Assert.assertEquals(contractWithPosition.getId(), service.getPrimeContract(UUID.randomUUID()).getId());
	}
	
	@Test
	public void testPrimeContractWithDefaultTreeType() {
		List<IdmIdentityContract> contracts = new ArrayList<>();
		IdmIdentityContract otherContract = new IdmIdentityContract(UUID.randomUUID());
		otherContract.setMain(false);
		IdmTreeNode workPosition = new IdmTreeNode();
		workPosition.setTreeType(new IdmTreeType(UUID.randomUUID()));
		otherContract.setWorkPosition(workPosition);
		//
		IdmIdentityContract contractWithDefaultPosition = new IdmIdentityContract(UUID.randomUUID());
		contractWithDefaultPosition.setMain(false);
		IdmTreeNode defaultWorkPosition = new IdmTreeNode();
		IdmTreeType defaultTreeType = new IdmTreeType(UUID.randomUUID());
		defaultWorkPosition.setTreeType(defaultTreeType);
		contractWithDefaultPosition.setWorkPosition(defaultWorkPosition);
		//
		contracts.add(otherContract);
		contracts.add(contractWithDefaultPosition);
		//
		when(repository.findAllByIdentity_Id(any(UUID.class), any())).thenReturn(contracts);		
		when(treeTypeRepository.findOneByDefaultTreeTypeIsTrue()).thenReturn(defaultTreeType);
		//
		Assert.assertEquals(contractWithDefaultPosition.getId(), service.getPrimeContract(UUID.randomUUID()).getId());
	}
}
