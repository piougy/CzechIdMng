package eu.bcvsolutions.idm.core.bulk.operation.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.bulk.operation.AbstractBulkOperation;
import eu.bcvsolutions.idm.core.api.bulk.operation.BulkOperationManager;
import eu.bcvsolutions.idm.core.api.bulk.operation.IdmBulkOperation;
import eu.bcvsolutions.idm.core.api.bulk.operation.dto.IdmBulkOperationDto;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmBulkOperationItemDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmBulkOperationItemService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.entity.IdmBulkOperationItem;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.repository.IdmBulkOperationItemRepository;
import eu.bcvsolutions.idm.core.scheduler.api.dto.LongRunningFutureTask;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;

/**
 * Implementation of bulk operations
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Service("bulkOperationManager")
public class DefaultBulkOperationManager implements BulkOperationManager {

	private final PluginRegistry<AbstractBulkOperation<? extends BaseDto>, Class<? extends AbstractEntity>> pluginExecutors;
	private final LongRunningTaskManager taskManager;
	
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private IdmBulkOperationItemService bulkOperationItemService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmBulkOperationItemRepository bulkOperationItemRepository;
	
	@Autowired
	public DefaultBulkOperationManager(
			List<AbstractBulkOperation<? extends BaseDto>> evaluators,
			LongRunningTaskManager taskManager) {
		pluginExecutors = OrderAwarePluginRegistry.create(evaluators);
		//
		
		Boolean.valueOf("1");
		this.taskManager = taskManager;
	}
	
	@Override
	public IdmBulkOperationDto processOperation(IdmBulkOperationDto operationDto) {
		AbstractBulkOperation<? extends BaseDto> executor = getOperationForDto(operationDto);
		//
		executor = (AbstractBulkOperation<?>) AutowireHelper.createBean(executor.getClass());
		//
		executor.setOperation(operationDto);
		//
		LongRunningFutureTask<Boolean> execute = taskManager.execute(executor);
		operationDto.setLongRunningTaskId(execute.getExecutor().getLongRunningTaskId());
		return operationDto;
	}
	
	@Override
	public List<IdmBulkOperationDto> getAvailableOperations(
			Class<? extends AbstractEntity> entity) {
		List<AbstractBulkOperation<? extends BaseDto>> operations = pluginExecutors.getPluginsFor(entity);
		//
		List<IdmBulkOperationDto> result = new ArrayList<>();
		for (IdmBulkOperation<? extends BaseDto> operation : operations) {
			IdmBulkOperationDto operationDto = new IdmBulkOperationDto();
			operationDto.setEntityClass(operation.getEntityClass());
			operationDto.setFilterClass(operation.getFilterClass());
			operationDto.setModule(operation.getModule());
			operationDto.setName(operation.getName());
			result.add(operationDto);
		}
		return result;
	}

	private AbstractBulkOperation<? extends BaseDto> getOperationForDto(IdmBulkOperationDto operationDto) {
		Assert.notNull(operationDto);
		Assert.notNull(operationDto.getEntityClass());
		try {
			Class<?> forName = Class.forName(operationDto.getEntityClass());
			if (AbstractEntity.class.isAssignableFrom(forName)) {
				List<AbstractBulkOperation<?>> operations = pluginExecutors.getPluginsFor((Class<? extends AbstractEntity>) forName);
				//
				for (AbstractBulkOperation<? extends BaseDto> operation : operations) {
					if (operation.getName().equals(operation.getName())) {
						return operation;
					}
				}
			}
		} catch (ClassNotFoundException e) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("bulkOperationClass", operationDto.getEntityClass()), e);
		}
		throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("bulkOperationName", operationDto.getName()));
	}

	@Override
	public void test() {
		Specification<IdmIdentity> spec = new Specification<IdmIdentity>() {
			@Override
			public Predicate toPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return (cb.isNotNull(root.get(IdmIdentity_.description)));
			}
		};
		
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<UUID> cq = criteriaBuilder.createQuery(UUID.class);
		Root<IdmIdentity> root = cq.from(IdmIdentity.class);
		
		cq.select(root.get(IdmIdentity_.id));
		
		cq.where(spec.toPredicate(root, cq, criteriaBuilder));
		
		DateTime test1 = new DateTime();
		List<UUID> uuids = entityManager.createQuery(cq).getResultList();
		

		DateTime test2 = new DateTime();
		Period period = new Period( test1 , DateTime.now() ) ;
		
//		Session session = (Session) this.entityManager.getDelegate();
//		Transaction tx = session.beginTransaction();
		
		for (UUID uuid : uuids) {
			IdmBulkOperationItem dto = new IdmBulkOperationItem();
			dto.setId(UUID.randomUUID());
			dto.setEntityId(uuid);
			dto.setOperationId(UUID.randomUUID());
			dto.setEntityClass(IdmIdentity.class.getName());
			
//			session.save(dto);
			//dto = bulkOperationItemService.save(dto);
		}
//
//		tx.commit();
//		session.close();
		
		bulkOperationItemRepository.save(entities)
		
		
		System.out.println("###########  size: " + uuids.size() + " load time: "  + period.toString());
		period = new Period( test2 , DateTime.now() ) ;
		System.out.println("###########   save time: "  + period.toString());
		
		
		
//		Specification<UUID> spec = new Specification<UUID>() {
//			@Override
//			public Predicate toPredicate(Root<UUID> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
//				return cb.equal(root.get(IdmIdentity_.firstName), "asd");
//			}
//		};
//		
//		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//	    CriteriaQuery<IdmIdentity> criteriaQuery = criteriaBuilder.createQuery(IdmIdentity.class);
//	    Root<IdmIdentity> root = criteriaQuery.from(IdmIdentity.class);
//	    Predicate predicate = spec.toPredicate(root, criteriaQuery, criteriaBuilder);
//	    criteriaQuery.where(predicate);
//	    
//	    List<UUID> resultList = entityManager.createQuery(criteriaQuery).getResultList();
	}

}
