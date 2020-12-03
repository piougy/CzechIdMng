import EntityManager from './EntityManager';
//
import { ProfileService } from '../../services';

/**
 * Profiles
 *
 * @author Radek Tomiška
 */
export default class ProfileManager extends EntityManager {

  constructor() {
    super();
    this.service = new ProfileService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'Profile';
  }

  getCollectionType() {
    return 'profiles';
  }

  /**
   * Init two factor authentication method.
   *
   * @param  {String} profile identifier
   * @param  {String} two factor authentication method
   * @param  {func} cb callback
   * @return {action}
   * @since 10.6.0
   */
  twoFactorAuthenticationInit(id, twoFactorAuthenticationType, cb) {
    const uiKey = this.resolveUiKey(null, id);
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().twoFactorAuthenticationInit(id, twoFactorAuthenticationType)
        .then((twoFactorRegistration) => {
          dispatch(this.dataManager.stopRequest(uiKey, null, () => {
            if (cb) {
              cb(twoFactorRegistration, null);
            }
          }));
        })
        .catch(error => {
          dispatch(this.receiveError(null, uiKey, error, cb));
        });
    };
  }

  /**
   * Confirm two factor authentication method.
   *
   * @param  {String} profile identifier
   * @param  {String} two factor authentication method
   * @param  {TwoFactorRegistrationConfirmDto} two factor tomken and verificatio code
   * @return {action}
   * @since 10.6.0
   */
  twoFactorAuthenticationConfirm(id, twoFactorConfirm, cb) {
    const uiKey = this.resolveUiKey(null, id);
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().twoFactorAuthenticationConfirm(id, twoFactorConfirm)
        .then((profile) => {
          dispatch(this.dataManager.stopRequest(uiKey, null, () => {
            if (cb) {
              cb(profile, null);
            }
          }));
        })
        .catch(error => {
          dispatch(this.receiveError(null, uiKey, error, cb));
        });
    };
  }
}
