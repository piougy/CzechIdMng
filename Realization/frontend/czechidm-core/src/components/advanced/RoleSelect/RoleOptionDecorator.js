import * as Basic from '../../basic';

/**
 * Role select option decorator.
 *
 * @author Radek Tomiška
 * @since 9.4.1
 */
export default class RoleOptionDecorator extends Basic.SelectBox.OptionDecorator {

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
