import IdentityService from './IdentityService';

/**
 * Identity projection - post (~create) / get (getById) is supported now only.
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
class IdentityProjectionService extends IdentityService {

  getApiPath() {
    return '/identity-projection';
  }

}

export default IdentityProjectionService;
