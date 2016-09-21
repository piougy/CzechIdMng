import React from 'react';
//
import DefaultCell from './DefaultCell';

/**
 * Renders cell with boolean content as disabled checkbox
 * Parametrs are automatically propagated from table / row / column
 * @param number rowIndex
 * @param array[json] input data
 * @param property column key
 * @param props other optional properties
 */
const BooleanCell = ({rowIndex, data, property, ...props}) => {
  const propertyValue = DefaultCell.getPropertyValue(data[rowIndex], property);
  //
  return (
    <DefaultCell {...props}>
      <input type="checkbox" disabled checked={propertyValue} />
    </DefaultCell>
  );
};

export default BooleanCell;
