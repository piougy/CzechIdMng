

import React, { Component, PropTypes } from 'react';
import moment from 'moment';
//
import DefaultCell from './DefaultCell';

/**
 * Renders cell with date content.
 * Parametrs are automatically propagated from table / row / column
 * @param number rowIndex
 * @param array[json] input data
 * @param property column key
 * @param props other optional properties
 */
const TextCell = ({rowIndex, data, property, format, ...props}) => {
  const propertyValue = DefaultCell.getPropertyValue(data[rowIndex], property);
  //
  return (
    <DefaultCell {...props}>
      { propertyValue ? moment(propertyValue).format(format) : null }
    </DefaultCell>
  );
};

export default TextCell;
