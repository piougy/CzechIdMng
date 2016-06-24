'use strict';

import React from 'react';
//
import DefaultCell from './DefaultCell';

/**
 * Renders cell with text content.
 * Parametrs are automatically propagated from table / row / column
 * 
 * @param number rowIndex
 * @param array[json] input data
 * @param property column key
 * @param props other optional properties
 */
const TextCell = ({rowIndex, data, property, ...props}) => {
  const propertyValue = DefaultCell.getPropertyValue(data[rowIndex], property);
  //
  return (
    <DefaultCell {...props}>
      {propertyValue}
    </DefaultCell>
  );
}

export default TextCell;
