package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;

import com.google.common.primitives.Shorts;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmGeneratedValueDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmGeneratedValueFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.IdmGeneratedValueService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmGeneratedValue;
import eu.bcvsolutions.idm.core.model.entity.IdmGeneratedValue_;
import eu.bcvsolutions.idm.core.model.repository.IdmGeneratedValueRepository;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Default implementation of service for generate attributes
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class DefaultIdmGeneratedValueService extends
		AbstractReadWriteDtoService<IdmGeneratedValueDto, IdmGeneratedValue, IdmGeneratedValueFilter>
		implements IdmGeneratedValueService {

	public DefaultIdmGeneratedValueService(IdmGeneratedValueRepository repository) {
		super(repository);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.GENERATEDVALUE, getEntityClass());
	}

	@Override
	protected List<Predicate> toPredicates(Root<IdmGeneratedValue> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, IdmGeneratedValueFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);

		if (StringUtils.isNotEmpty(filter.getEntityType())) {
			predicates.add(builder.equal(root.get(IdmGeneratedValue_.entityType), filter.getEntityType()));
		}

		if (filter.getDisabled() != null) {
			predicates.add(builder.equal(root.get(IdmGeneratedValue_.disabled), filter.getDisabled()));
		}

		return predicates;
	}

	@Override
	public List<IdmGeneratedValueDto> getEnabledGenerator(Class<? extends Identifiable> entityType) {
		IdmGeneratedValueFilter filter = new IdmGeneratedValueFilter();
		filter.setDisabled(false);
		filter.setEntityType(entityType.getCanonicalName());

		// we must create new instance of arraylist, given list is unmodifable
		List<IdmGeneratedValueDto> generators = new ArrayList<>(this.find(filter, null).getContent());

		// sort by order
		Collections.sort(generators, new Comparator<IdmGeneratedValueDto>() {
			@Override
			public int compare(IdmGeneratedValueDto o1, IdmGeneratedValueDto o2) {
				return Shorts.compare(o1.getSeq(), o2.getSeq());
			}
		});
		return generators;
	}

}
