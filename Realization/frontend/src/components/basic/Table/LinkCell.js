import React from 'react';
import { Link } from 'react-router';
import { formatPattern, getParamNames } from 'react-router/lib/PatternUtils';
//
import DefaultCell from './DefaultCell';
import Immutable from 'immutable';

/**
 * Fills href parameter values from ginen rowData / entity object
 *
 * @param  {string} to href
 * @param  {object} rowData entity
 * @return {string} formated href
 */
function _resolveToWithParameters(to, rowData) {
  const parameterNames = getParamNames(to);
  let parameterValues = new Immutable.Map({});
  parameterNames.map(parameter => {
    parameterValues = parameterValues.set(parameter, DefaultCell.getPropertyValue(rowData, parameter));
  });
  return formatPattern(to, parameterValues.toJS());
}

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
const LinkCell = ({rowIndex, data, property, to, className, title, ...props}) => {
  const propertyValue = DefaultCell.getPropertyValue(data[rowIndex], property);
  //
  return (
    <DefaultCell {...props}>
      {
        propertyValue
        ?
        <Link to={_resolveToWithParameters(to, data[rowIndex])} title={title} className={className}>
          {propertyValue}
        </Link>
        :
        null
      }
    </DefaultCell>
  );
};

export default LinkCell;
