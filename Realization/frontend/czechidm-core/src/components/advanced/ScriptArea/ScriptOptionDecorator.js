import React from 'react';
//
import * as Basic from '../../basic';
import ScriptCategoryEnum from '../../../enums/ScriptCategoryEnum';

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
    if (!entity) {
      return null;
    }
    return (
      <Basic.Div>
        <Basic.Div>
          { this.i18n('entity.Script.category') }: <strong>{ ScriptCategoryEnum.getNiceLabel(entity.category) }</strong>
        </Basic.Div>
        {
          entity.description
          ?
          (
            <Basic.Div>
              { entity.description.replace(/(<([^>]+)>)/ig, '') }
            </Basic.Div>
          )
          :
          null
        }
      </Basic.Div>
    );
  }
}
