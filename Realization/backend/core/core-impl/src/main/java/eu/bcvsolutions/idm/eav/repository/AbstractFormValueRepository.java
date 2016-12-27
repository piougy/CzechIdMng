package eu.bcvsolutions.idm.eav.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.eav.dto.filter.FormValueFilter;
import eu.bcvsolutions.idm.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;

/**
 * Abstract form attribute values repository
 * 
 * @author Radek Tomiška
 *
 * @param <E> Form attribute value type
 * @param <O> Values owner type
 */
@NoRepositoryBean
public interface AbstractFormValueRepository<O extends FormableEntity, E extends AbstractFormValue<O>> extends AbstractEntityRepository<E, FormValueFilter<O>> {
	
	/**
	 * Quick search 
	 */
	@Override
	@Query(value = "select e from #{#entityName} e " + " where"
			+ " (?#{[0].owner} is null or e.owner = ?#{[0].owner})"
			+ " and"
			+ " (?#{[0].formDefinition} is null or e.formAttribute.formDefinition = ?#{[0].formDefinition})"
			+ " and"
			+ " (?#{[0].formAttribute} is null or e.formAttribute = ?#{[0].formAttribute})")
	Page<E> find(FormValueFilter<O> filter, Pageable pageable);
	
	/**
	 * Returns all form values by given owner (from all definitions)
	 * 
	 * @param owner
	 * @return
	 */
	List<E> findByOwner(@Param("owner") O owner);
	
	/**
	 * Returns form values by given owner and definition ordered by seq
	 * 
	 * @param owner
	 * @param formDefiniton
	 * @return
	 */
	List<E> findByOwnerAndFormAttribute_FormDefinitionOrderBySeqAsc(@Param("owner") O owner, @Param("formDefinition") IdmFormDefinition formDefiniton);
}
