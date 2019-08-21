import * as Basic from '../../basic';

/**
 * Script select option decorator.
 *
 * @author Radek Tomiška
 * @since 9.7.3
 */
export default class ScriptOptionDecorator extends Basic.SelectBox.OptionDecorator {

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'component:script';
  }

  /**
   * Returns description.
   *
   * @return {string}
   */
  getDescription(entity) {
    if (!entity || !entity.description) {
      return null;
    }
    return entity.description.replace(/(<([^>]+)>)/ig, '');
  }
}
