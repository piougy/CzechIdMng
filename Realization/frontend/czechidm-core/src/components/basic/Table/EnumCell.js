import React from 'react';
//
import DefaultCell from './DefaultCell';
import EnumValue from '../EnumValue/EnumValue';

/**
 * Renders cell with link and text content.
 * Parametrs are automatically propagated from table / row / column

 * @param number rowIndex
 * @param array[json] input data
 * @param property column key
 * @param to - router link
 * @param className className
 * @param title - html title
 * @param props other optional properties
 */
const EnumCell = ({rowIndex, data, property, enumClass, ...props}) => {
  const propertyValue = DefaultCell.getPropertyValue(data[rowIndex], property);
  //
  return (
    <DefaultCell {...props}>
      <EnumValue value={propertyValue} enum={enumClass}/>
    </DefaultCell>
  );
};

export default EnumCell;
