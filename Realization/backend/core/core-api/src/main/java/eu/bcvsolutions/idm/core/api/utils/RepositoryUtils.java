package eu.bcvsolutions.idm.core.api.utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.ResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;

/**
 * Utils for Spring Data repositories.
 * 
 * @author Radek Tomiška
 */
public abstract class RepositoryUtils {

	/**
	 * Return collection of entity ids usable in repository query. 
	 * If {@code entities} is null or empty, returns collection with non-existent {@code Long} id, so could be used in IN clause etc.
	 * 
	 * @param entities
	 * @return
	 */
	public static List<UUID> queryEntityIds(List<? extends AbstractEntity> entities) {
		List<UUID> entityIds = new ArrayList<>();
		if (entities != null && !entities.isEmpty()) {
			for(AbstractEntity entity : entities) {
				entityIds.add(entity.getId());
			}
		} else {
			// add non-existent long id 
			entityIds.add(UUID.randomUUID());
		}
		return entityIds;
	}
	
	/**
	 * TODO: constraint name - result code mapping
	 * 
	 * @param ex
	 * @return
	 */
	public static ResultCode resolveResultCode(DataIntegrityViolationException  ex) {
		throw new UnsupportedOperationException("not implemented", ex);
	}
	
	/**
	 * Returns valid predicate to current date.
	 * 
	 * @param path to {@link ValidableEntity}
	 * @param builder
	 * @return complex predicate
	 */
	public static Predicate getValidPredicate(Path<? extends ValidableEntity> path, CriteriaBuilder builder) {
		return getValidPredicate(path, builder, null);
	}
	
	/**
	 * Returns valid predicate to given date.
	 * 
	 * @param path to {@link ValidableEntity}
	 * @param builder
	 * @param date date to compare. Current date is used, when null is given.
	 * @return complex predicate
	 */
	public static Predicate getValidPredicate(Path<? extends ValidableEntity> path, CriteriaBuilder builder, LocalDate date) {
		Assert.notNull(path, "Path is required to get predicate.");
		Assert.notNull(builder, "Initialized criteria builder is required to get predicate.");
		if (date == null) {
			date = LocalDate.now();
		}
		//
		return builder.and(
			builder.or(
					builder.lessThanOrEqualTo(path.get(ValidableEntity.PROPERTY_VALID_FROM), date),
					builder.isNull(path.get(ValidableEntity.PROPERTY_VALID_FROM))
					),
			builder.or(
					builder.greaterThanOrEqualTo(path.get(ValidableEntity.PROPERTY_VALID_TILL), date),
					builder.isNull(path.get(ValidableEntity.PROPERTY_VALID_TILL))
					)
		);
	}
	
	/**
	 * Returns valid now or in future predicate in current date (now).
	 * Mostly used for identity contracts.
	 * 
	 * @see IdmIdentityContractDto
	 * @param path to {@link ValidableEntity}
	 * @param builder
	 * @return complex predicate
	 * @since 10.3.0
	 */
	public static Predicate getValidNowOrInFuturePredicate(Path<? extends ValidableEntity> path, CriteriaBuilder builder) {
		return getValidNowOrInFuturePredicate(path, builder, null);
	}
	
	/**
	 * Returns valid now or in future predicate to given date.
	 * Mostly used for identity contracts.
	 * 
	 * @see IdmIdentityContractDto
	 * @param path to {@link ValidableEntity}
	 * @param builder
	 * @param date date to compare. Current date is used, when null is given.
	 * @return complex predicate
	 * @since 10.3.0
	 */
	public static Predicate getValidNowOrInFuturePredicate(Path<? extends ValidableEntity> path, CriteriaBuilder builder, LocalDate date) {
		Assert.notNull(path, "Path is required to get predicate.");
		Assert.notNull(builder, "Initialized criteria builder is required to get predicate.");
		if (date == null) {
			date = LocalDate.now();
		}
		//
		return builder.or(
				builder.greaterThanOrEqualTo(path.get(ValidableEntity.PROPERTY_VALID_TILL), date),
				builder.isNull(path.get(ValidableEntity.PROPERTY_VALID_TILL))
				);
	}
	
}
