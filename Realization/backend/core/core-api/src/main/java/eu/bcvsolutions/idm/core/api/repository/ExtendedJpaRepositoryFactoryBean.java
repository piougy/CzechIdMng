package eu.bcvsolutions.idm.core.api.repository;
import java.io.Serializable;

import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

/**
 * Provides @ExtendedJpaRepository implementation for the repositories.
 * 
 * @author Radek Tomiška
 *
 * @since 9.6.0
 * @param <R>
 * @param <T>
 * @param <I>
 */
public class ExtendedJpaRepositoryFactoryBean<R extends JpaRepository<T, I>, T, I extends Serializable> 
		extends JpaRepositoryFactoryBean<R, T, I> {
 
    public ExtendedJpaRepositoryFactoryBean(Class<? extends R> repositoryInterface) {
		super(repositoryInterface);
	}

	@Override
    protected RepositoryFactorySupport createRepositoryFactory(EntityManager em) {
        return new BaseRepositoryFactory<>(em);
    }
 
    private static class BaseRepositoryFactory<T, I extends Serializable> extends JpaRepositoryFactory {
 
        public BaseRepositoryFactory(EntityManager em) {
            super(em);
        }
        
		@Override
		@SuppressWarnings("unchecked")
        protected SimpleJpaRepository<?, ?> getTargetRepository(RepositoryInformation information, EntityManager em ) {
        	return new ExtendedJpaRepository<T, I>((Class<T>) information.getDomainType(), em);
        }
 
        @Override
        protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
            return ExtendedJpaRepository.class;
        }
    }
}