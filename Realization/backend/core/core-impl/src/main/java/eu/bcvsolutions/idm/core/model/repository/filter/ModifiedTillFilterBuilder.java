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
import eu.bcvsolutions.idm.core.api.dto.filter.ModifiedTillFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.repository.filter.BaseFilterBuilder;

/**
 * Filter for filtering entities changed till given time stamp (auditable.modified or uditable.created <= modifiedTill).
 * Created date is used as fallback, if modified is {@code null} => creation is the last modification.
 * 
 * @author Radek Tomiška
 * @since 10.2.0
 */
@Component()
public class ModifiedTillFilterBuilder<E extends AbstractEntity> extends BaseFilterBuilder<E, DataFilter> {

	
	
	@Override
	public String getName() {
		return ModifiedTillFilter.PARAMETER_MODIFIED_TILL;
	}

	@Override
	public Predicate getPredicate(Root<E> root, AbstractQuery<?> query, CriteriaBuilder builder, DataFilter filter) {
		ZonedDateTime modifiedTill = filter.getModifiedTill();
		
		if (modifiedTill == null) {
			return null;
		}

		return builder.or(
				builder.lessThanOrEqualTo(root.get(AbstractEntity_.modified), modifiedTill),
				builder.and(
						builder.isNull(root.get(AbstractEntity_.modified)), // modified is null => creation is last modification
						builder.lessThanOrEqualTo(root.get(AbstractEntity_.created), modifiedTill)
				)
		);
	}

	@Override
	public Page<E> find(DataFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Find by modified till only is not supported, use concrete service instead.");
	}
}
