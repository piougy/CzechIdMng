'use strict';

import React, { PropTypes } from 'react';
import classNames from 'classnames';
//
import * as Basic from '../../basic';

export default class FilterBooleanSelectBox extends Basic.BooleanSelectBox {

}

FilterBooleanSelectBox.propTypes = {
  ...Basic.BooleanSelectBox.propTypes,
  /**
   * Field name - if is not set, then ref is used
   * @type {[type]}
   */
  field: PropTypes.string,
  /**
   * Filter relation
   * TODO: enumeration
   */
  relation: PropTypes.oneOf(['EQ', 'NEQ'])
};
const { labelSpan, componentSpan, ...otherDefaultProps } = Basic.BooleanSelectBox.defaultProps; // labelSpan etc. override
FilterBooleanSelectBox.defaultProps = {
  ...otherDefaultProps,
  relation: 'EQ',
  labelSpan: 'col-lg-4',
  componentSpan: 'col-lg-8'
};
