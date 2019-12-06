/**
 * Base app package. All components are scanned automatically under this package.
 * 
 * @author Radek Tomiška 
 *
 */
@TypeDef(defaultForType = UUID.class, typeClass = UUIDBinaryType.class) // TODO: https://stackoverflow.com/questions/40272819/hibernate-5-breaks-legacy-handling-of-java-uuid-in-postgresql
package eu.bcvsolutions.idm;

import java.util.UUID;

import org.hibernate.annotations.TypeDef;
import org.hibernate.type.UUIDBinaryType;
