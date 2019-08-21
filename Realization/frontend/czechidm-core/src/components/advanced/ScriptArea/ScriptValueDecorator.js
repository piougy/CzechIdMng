import * as Basic from '../../basic';

/**
 * Script select value decorator.
 *
 * @author Radek Tomiška
 * @since 9.7.3
 */
export default class ScriptValueDecorator extends Basic.SelectBox.ValueDecorator {

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'component:script';
  }

}
