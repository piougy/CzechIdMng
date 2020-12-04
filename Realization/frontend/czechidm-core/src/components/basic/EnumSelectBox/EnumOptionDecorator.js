import OptionDecorator from '../../basic/SelectBox/OptionDecorator';

/**
 * Enum select option decorator.
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
export default class EnumOptionDecorator extends OptionDecorator {

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon(entity) {
    if (entity && entity._iconKey) {
      return entity._iconKey;
    }
    return null;
  }

}
