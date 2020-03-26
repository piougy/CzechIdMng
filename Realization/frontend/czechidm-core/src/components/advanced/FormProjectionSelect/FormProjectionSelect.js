import React from 'react';
//
import { FormProjectionManager } from '../../../redux';
import EntitySelectBox from '../EntitySelectBox/EntitySelectBox';
import FormProjectionOptionDecorator from './FormProjectionOptionDecorator';
import FormProjectionValueDecorator from './FormProjectionValueDecorator';

const formProjectionManager = new FormProjectionManager();

/**
* Component for select form projections.
*
* @author Radek Tomiška
* @since 10.2.0
*/
export default class FormProjectionSelect extends EntitySelectBox {

  render() {
    const { rendered, entityType, niceLabel, manager, ...others } = this.props;
    //
    if (!rendered) {
      return null;
    }
    if (entityType && entityType !== 'formProjection') {
      LOGGER.warn(`FormProjectionSelect supports formProjection entity type only, given [${ entityType }] type will be ignored.`);
    }
    //
    return (
      <EntitySelectBox
        ref="selectComponent"
        entityType="formProjection"
        manager={ manager }
        niceLabel={ niceLabel || ((entity) => {
          return manager.getLocalization(entity, 'label', manager.getNiceLabel(entity));
        })}
        { ...others }/>
    );
  }
}

FormProjectionSelect.propTypes = {
  ...EntitySelectBox.propTypes
};
FormProjectionSelect.defaultProps = {
  ...EntitySelectBox.defaultProps,
  optionComponent: FormProjectionOptionDecorator,
  valueComponent: FormProjectionValueDecorator,
  manager: formProjectionManager
};
