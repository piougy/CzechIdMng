import React, { PropTypes } from 'react';
import EnumSelectBox from '../EnumSelectBox/EnumSelectBox';

export default class BooleanSelectBox extends EnumSelectBox {

}

BooleanSelectBox.propTypes = {
  ...EnumSelectBox.propTypes,
  value: PropTypes.oneOfType([PropTypes.object, PropTypes.string])
};
BooleanSelectBox.defaultProps = {
  ...EnumSelectBox.defaultProps,
  options: [
    // TODO: localization - move options to constructor - wait for i18n is ready
    { value: 'true', niceLabel: 'Ano' },
    { value: 'false', niceLabel: 'Ne' }
  ]
};
