package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

import eu.bcvsolutions.idm.core.api.dto.IdmProfileDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmProfileFilter;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Operations with profiles
 * 
 * @author Radek Tomiška
 * @since 9.0.0
 */
public interface IdmProfileService extends 
		EventableDtoService<IdmProfileDto, IdmProfileFilter>,
		AuthorizableService<IdmProfileDto>,
		ScriptEnabled {
	
	/**
	 * Return profile for given identity username / id
	 *
	 * @param identityIdentifier identity username / id
	 * @param permission permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	IdmProfileDto findOneByIdentity(Serializable identityIdentifier, BasePermission... permission);
	
	/**
	 * Return profile for given identifier (id/ username), if profile doesn't exist
	 * create new one.
	 *
	 * @param identityIdentifier identity username / id
	 * @param permission permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	IdmProfileDto findOrCreateByIdentity(Serializable identityIdentifier, BasePermission... permission);
	
	/**
	 * Upload new image version for the given profile
	 * 
	 * @param profile persisted profile
	 * @param data
	 * @param fileName
	 * @param permission permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	IdmProfileDto uploadImage(IdmProfileDto profile, MultipartFile data, String fileName, BasePermission... permission);
	
	/**
	 * Delete profile image (all versions)
	 * 
	 * @param profile persisted profile
	 * @param permission permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	IdmProfileDto deleteImage(IdmProfileDto profile, BasePermission... permission); 
}
