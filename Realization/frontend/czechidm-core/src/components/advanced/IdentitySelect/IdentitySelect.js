import React from 'react';
//
import EntitySelectBox from '../EntitySelectBox/EntitySelectBox';
import IdentityOptionDecorator from './IdentityOptionDecorator';
import IdentityValueDecorator from './IdentityValueDecorator';

/**
* Component for select identities.
*
* @author Radek Tomiška
* @since 10.1.0
*/
export default class IdentitySelect extends EntitySelectBox {

  render() {
    const { rendered, entityType, ...others } = this.props;
    //
    if (!rendered) {
      return null;
    }
    if (entityType && entityType !== 'identity') {
      LOGGER.warn(`IdentitySelect supports identity entity type only, given [${ entityType }] type will be ignored.`);
    }
    //
    return (
      <EntitySelectBox
        ref="selectComponent"
        entityType="identity"
        { ...others }/>
    );
  }
}

IdentitySelect.propTypes = {
  ...EntitySelectBox.propTypes
};
IdentitySelect.defaultProps = {
  ...EntitySelectBox.defaultProps,
  optionComponent: IdentityOptionDecorator,
  valueComponent: IdentityValueDecorator
};
