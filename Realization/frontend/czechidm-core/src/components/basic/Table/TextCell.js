import React from 'react';
//
import DefaultCell from './DefaultCell';
import ShortText from '../ShortText/ShortText';

/**
 * Renders cell with text content.
 * Parametrs are automatically propagated from table / row / column
 *
 * @param number rowIndex
 * @param array[json] input data
 * @param property column key
 * @param maxLength short text maxLength
 * @param props other optional properties
 *
 * @author Radek Tomiška
 */
const TextCell = ({rowIndex, data, property, maxLength, ...props}) => {
  const propertyValue = DefaultCell.getPropertyValue(data[rowIndex], property);
  //
  return (
    <DefaultCell {...props}>
      {
        !maxLength
        ?
        propertyValue
        :
        <ShortText value={ propertyValue } maxLength={ maxLength }/>
      }
    </DefaultCell>
  );
};

export default TextCell;
