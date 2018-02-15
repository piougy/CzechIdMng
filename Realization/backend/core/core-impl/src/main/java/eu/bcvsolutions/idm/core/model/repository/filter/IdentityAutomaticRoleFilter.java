package eu.bcvsolutions.idm.core.model.repository.filter;
 
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
 
/**
 * Filter by automatic role
 * 
 * @author Petr Adamec
 * @author Radek Tomiška
 */
@Component
@Description("Filter by automatic role")
public class IdentityAutomaticRoleFilter extends AbstractFilterBuilder<IdmIdentity, IdmIdentityFilter> {
   
    @Autowired
    public IdentityAutomaticRoleFilter(IdmIdentityRepository repository) {
        super(repository);
    }
    
    @Override
    public String getName() {
        return IdmIdentityFilter.PARAMETER_AUTOMATIC_ROLE;
    }
 
    @Override
    public Predicate getPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmIdentityFilter filter) {
        if (filter.getAutomaticRoleId() == null) {
            return null;
        }
       
        Subquery<IdmIdentityRole> subquery = query.subquery(IdmIdentityRole.class);
        Root<IdmIdentityRole> subRoot = subquery.from(IdmIdentityRole.class);
        subquery.select(subRoot);
        subquery.where(
                builder.and(
                        builder.equal(subRoot.get(IdmIdentityRole_.identityContract).get(IdmIdentityContract_.identity), root), // correlation
                        builder.equal(subRoot.get(IdmIdentityRole_.automaticRole).get(AbstractEntity_.id), filter.getAutomaticRoleId())
                        ));
        Predicate predicate = builder.exists(subquery);
        return predicate;
    } 
}
