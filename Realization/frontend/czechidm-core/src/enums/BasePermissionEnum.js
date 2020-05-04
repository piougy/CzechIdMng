import AbstractEnum from './AbstractEnum';

/**
 * Idm base permissions.
 *
 * @see IdmBasePermission
 * @author Radek Tomiška
 * @since 10.3.0
 */
export default class BasePermissionEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:permission.base.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }
}

BasePermissionEnum.ADMIN = Symbol('ADMIN');
BasePermissionEnum.COUNT = Symbol('COUNT');
BasePermissionEnum.AUTOCOMPLETE = Symbol('AUTOCOMPLETE');
BasePermissionEnum.READ = Symbol('READ');
BasePermissionEnum.CREATE = Symbol('CREATE');
BasePermissionEnum.UPDATE = Symbol('UPDATE');
BasePermissionEnum.DELETE = Symbol('DELETE');
BasePermissionEnum.EXECUTE = Symbol('EXECUTE');
