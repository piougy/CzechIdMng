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
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
 
/**
 * Filter for automatic role
 * @author Petr Adamec
 *
 */
@Component
@Description("Automatic role filter - by identity")
public class IdentityAutomaticRoleFilter extends AbstractFilterBuilder<IdmIdentity, IdmIdentityFilter> {
   
    @Autowired
    public IdentityAutomaticRoleFilter(IdmIdentityRepository repository) {
        super(repository);
    }
 
   
 
    @Override
    public Predicate getPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmIdentityFilter filter) {
        if (filter.getAutomaticRoleAttributeId() == null) {
            return null;
        }
       
        Subquery<IdmIdentityContract> subquery = query.subquery(IdmIdentityContract.class);
        Root<IdmIdentityContract> subRoot = subquery.from(IdmIdentityContract.class);
        subquery.select(subRoot);
       
        Subquery<IdmIdentityRole> subQueryContractRole = query.subquery(IdmIdentityRole.class);
        Root<IdmIdentityRole> subRootContractRole = subQueryContractRole.from(IdmIdentityRole.class);
       
        subQueryContractRole.select(subRootContractRole);
        subQueryContractRole.where(
                builder.and(
                        builder.equal(subRootContractRole.get(IdmIdentityRole_.identityContract), subRoot),
                        builder.equal(subRootContractRole.get(IdmIdentityRole_.automaticRole).get(AbstractEntity_.id), filter.getAutomaticRoleAttributeId())
                        ));
        subquery.where(
                builder.and(
                        builder.equal(subRoot.get(IdmIdentityContract_.identity), root),
                        builder.exists(subQueryContractRole)
                        ));
        Predicate predicate = builder.exists(subquery);
        return predicate;
    }
   
    @Override
    public String getName() {
        return "automaticRoleAttributeId";
    }
 
   
 
}