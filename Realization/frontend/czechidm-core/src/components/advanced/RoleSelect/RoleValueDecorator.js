import * as Basic from '../../basic';

/**
 * Role select value decorator.
 *
 * @author Radek Tomiška
 * @since 9.5.0
 */
export default class RoleValueDecorator extends Basic.SelectBox.ValueDecorator {

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon(entity) {
    if (entity && entity.childrenCount > 0) {
      return 'component:business-role';
    }
    return 'component:role';
  }

}
