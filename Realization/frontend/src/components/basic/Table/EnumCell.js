

import React from 'react';
//
import DefaultCell from './DefaultCell';

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
      {
        !propertyValue
        ?
        null
        :
        !enumClass
        ?
        propertyValue
        :
        !enumClass.getLevel(propertyValue)
        ?
        enumClass.getNiceLabel(propertyValue)
        :
        <span className={'label label-' + enumClass.getLevel(propertyValue)}>{enumClass.getNiceLabel(propertyValue)}</span>
      }
    </DefaultCell>
  );
}

export default EnumCell;
