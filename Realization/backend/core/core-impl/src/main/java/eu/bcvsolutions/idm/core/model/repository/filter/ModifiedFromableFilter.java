package eu.bcvsolutions.idm.core.model.repository.filter;

import java.time.ZonedDateTime;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.ModifiedFromFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.repository.filter.BaseFilterBuilder;

/**
 * Modified from filter (auditable.modified or uditable.created  >= modifiedFrom).
 * Created date is used as fallback, if modified is {@code null} => creation is the last modification.
 * 
 * @author Vít Švanda
 */
@Component
public class ModifiedFromableFilter<E extends AbstractEntity> extends BaseFilterBuilder<E, DataFilter> {

	@Override
	public String getName() {
		return ModifiedFromFilter.PARAMETER_MODIFIED_FROM;
	}

	@Override
	public Predicate getPredicate(Root<E> root, AbstractQuery<?> query, CriteriaBuilder builder, DataFilter filter) {

		ZonedDateTime modifiedFrom = filter.getModifiedFrom();
		
		if (modifiedFrom == null) {
			return null;
		}

		return builder.or(
				builder.greaterThanOrEqualTo(root.get(AbstractEntity_.modified), modifiedFrom),
				builder.and(
						builder.isNull(root.get(AbstractEntity_.modified)), // modified is null => creation is last modification
						builder.greaterThanOrEqualTo(root.get(AbstractEntity_.created), modifiedFrom)
				)
		);
	}

	@Override
	public Page<E> find(DataFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException(
				"Find by modified from only is not supported, use concrete service instead.");
	}
}
