package eu.bcvsolutions.idm.core.model.repository.processor;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.data.history.Revision;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.rest.IdmRevisionController;

/**
 * Assembler for back links (HATEOAS) to revisions. 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@SuppressWarnings("rawtypes")
public class RevisionAssembler<T extends BaseEntity> extends ResourceAssemblerSupport<T, ResourceWrapper> {
	
	public RevisionAssembler(Class<?> controllerClass, Class<ResourceWrapper> resourceType) {
		super(controllerClass, resourceType);
	}
	
	public RevisionAssembler() {
		super(BaseEntity.class, ResourceWrapper.class);
	}

	/**
	 * Method generate selfLink to entity used from revision
	 * findOne is from CrudRepository.
	 * @return ResourceWrapper
	 */
	@Override
	@Deprecated
	public ResourceWrapper toResource(T entity) {
		ResourceWrapper<T> wrapper = new ResourceWrapper<T>(entity);
		Link selfLink = linkTo(methodOn(IdmRevisionController.class).findRevisions(String.valueOf(entity.getId()))).withSelfRel();
		wrapper.add(selfLink);
		return wrapper;
	}
	
	public ResourceWrapper toResource(Class<?> controllerClass, String entityIdentifier, T entity, Long revId) {
		ResourceWrapper<T> wrapper = new ResourceWrapper<T>(entity);
		//Link selfLink = linkTo((methodOn(clazz)).findRevision(String.valueOf(entityIdentifier), revId)).withSelfRel();
		//wrapper.add(selfLink);
		return wrapper;
	}
	
	public ResourceWrapper toResource(Class<?> controllerClass, String entityIdentifier, Revision<Long, ? extends BaseEntity> entity, Long revId) {
		ResourceWrapper<Revision<Long, ? extends BaseEntity>> wrapper = new ResourceWrapper<Revision<Long, ? extends BaseEntity>>(entity);
//		Link selfLink = linkTo((methodOn(clazz)).findRevision(String.valueOf(entityIdentifier), revId)).withSelfRel();
//		wrapper.add(selfLink);
		return wrapper;
	}

}
