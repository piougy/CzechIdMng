import ValueDecorator from '../SelectBox/ValueDecorator';

/**
 * Enum select value decorator.
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
export default class EnumValueDecorator extends ValueDecorator {

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
