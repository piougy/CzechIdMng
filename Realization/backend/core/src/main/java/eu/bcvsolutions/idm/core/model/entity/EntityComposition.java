package eu.bcvsolutions.idm.core.model.entity;

/**
 * Represents entity composition
 * 
 * @author Radek Tomiška 
 *
 * @param <T>
 */
public interface EntityComposition<T extends AbstractEntity> {
	
	/**
	 * Superior entity
	 * @return
	 */
	T getSuperior();
	
	/**
	 * Sub entity
	 * @return
	 */
	T getSub();
	
}
