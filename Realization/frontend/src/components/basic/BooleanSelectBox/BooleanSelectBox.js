'use strict';

import React, { PropTypes } from 'react';
import classNames from 'classnames';
//
import EnumSelectBox from '../EnumSelectBox/EnumSelectBox';

export default class BooleanSelectBox extends EnumSelectBox {

}

BooleanSelectBox.propTypes = {
  ...EnumSelectBox.propTypes,
};
const { options, ...otherDefaultProps } = EnumSelectBox.defaultProps; // labelSpan etc. override
BooleanSelectBox.defaultProps = {
  ...otherDefaultProps,
  options: [
    // TODO: localization - move options to constructor - wait for i18n is ready
    { value: 'true', niceLabel: 'Ano' },
    { value: 'false', niceLabel: 'Ne' }
  ]
};
