

import React, { PropTypes } from 'react';
import classNames from 'classnames';
//
import * as Basic from '../../basic';

export default class FilterDateTimePicker extends Basic.DateTimePicker {

}

FilterDateTimePicker.propTypes = {
  ...Basic.DateTimePicker.propTypes,
  /**
   * Field name - if is not set, then ref is used
   * @type {[type]}
   */
  field: PropTypes.string,
  /**
   * Filter relation
   * TODO: enumeration
   */
  relation: PropTypes.oneOf(['EQ', 'NEQ', 'LT', 'LE', 'GT', 'GE', 'IN', 'IS_NULL', 'IS_NOT_NULL', 'IS_EMPTY', 'IS_NOT_EMPTY'])
};
const { labelSpan, componentSpan, ...otherDefaultProps } = Basic.DateTimePicker.defaultProps; // labelSpan etc. override
FilterDateTimePicker.defaultProps = {
  ...otherDefaultProps,
  relation: 'EQ',
  labelSpan: 'col-lg-4',
  componentSpan: 'col-lg-8'
};
