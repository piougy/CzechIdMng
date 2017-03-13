import React from 'react';
//
import DefaultCell from './DefaultCell';
import Icon from '../Icon/Icon';

/**
 * Renders cell with boolean content as disabled checkbox
 * Parametrs are automatically propagated from table / row / column
 *
 * TODO: fix key issue - when input is used, then property walue is mixed through sorting, editing etc.
 *
 * @param number rowIndex
 * @param array[json] input data
 * @param property column key
 * @param props other optional properties
 */
const BooleanCell = ({rowIndex, data, property, propertyValue, ...props}) => {
  let _propertyValue = propertyValue;
  if (propertyValue === undefined || propertyValue === null) {
    _propertyValue = DefaultCell.getPropertyValue(data[rowIndex], property);
  }
  //
  return (
    <DefaultCell {...props}>
      {
        _propertyValue
        ?
        <Icon value="fa:check-square-o" disabled/>
        :
        <Icon value="fa:square-o" disabled/>
      }
    </DefaultCell>
  );
};

export default BooleanCell;
