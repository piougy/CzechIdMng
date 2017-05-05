package eu.bcvsolutions.idm.core.api.entity;

/**
 * Represents entity composition
 * 
 * @param <T>
 * @author Radek Tomiška
 */
public interface EntityComposition<T extends AbstractEntity> {
	
	/**
	 * Superior entity
     *
	 * @return
	 */
	T getSuperior();
	
	/**
	 * Sub entity
     *
	 * @return
	 */
	T getSub();
	
}
