package eu.bcvsolutions.idm.core.api.dto;

import org.springframework.hateoas.core.Relation;

/**
 * Dto representing one cache.
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 * @author Radek Tomiška
 */
@Relation(collectionRelation = "caches")
public class IdmCacheDto extends AbstractComponentDto {

    private static final long serialVersionUID = 1L;
    //
    private long size;

    /**
     * Item count in cache.
     * 
     * @return item count in cache
     */
    public long getSize() {
        return size;
    }

    /**
     * Item count in cache.
     * 
     * @param size item count in cache
     */
    public void setSize(long size) {
        this.size = size;
    }
}
