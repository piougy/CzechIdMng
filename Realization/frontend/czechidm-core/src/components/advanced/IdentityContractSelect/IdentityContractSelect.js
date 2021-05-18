import React from 'react';
//
import EntitySelectBox from '../EntitySelectBox/EntitySelectBox';
import IdentityContractOptionDecorator from './IdentityContractOptionDecorator';
import IdentityContractValueDecorator from './IdentityContractValueDecorator';

/**
* Component for select contracts.
*
* @author Radek Tomiška
* @since 11.1.0
*/
export default class IdentityContractSelect extends EntitySelectBox {

  render() {
    const { rendered, entityType, ...others } = this.props;
    //
    if (!rendered) {
      return null;
    }
    if (entityType && entityType !== 'identity') {
      LOGGER.warn(`IdentityContractSelect supports identity entity type only, given [${ entityType }] type will be ignored.`);
    }
    //
    return (
      <EntitySelectBox
        ref="selectComponent"
        entityType="contract"
        { ...others }/>
    );
  }
}

IdentityContractSelect.propTypes = {
  ...EntitySelectBox.propTypes
};
IdentityContractSelect.defaultProps = {
  ...EntitySelectBox.defaultProps,
  optionComponent: IdentityContractOptionDecorator,
  valueComponent: IdentityContractValueDecorator
};
