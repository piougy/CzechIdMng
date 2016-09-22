import React, { PropTypes } from 'react';
//
import * as Basic from '../../basic';

/**
 * Simple date formatter with default format from localization
 */
class DateValue extends Basic.AbstractContextComponent {

  constructor(props) {
    super(props);
  }

  render() {
    const { format, rendered, showTime, ...others } = this.props;
    if (!rendered) {
      return null;
    }

    let _format = format;
    if (!_format) {
      if (showTime) {
        _format = this.i18n('format.datetime');
      } else {
        _format = this.i18n('format.date');
      }
    }

    return (
      <Basic.DateValue format={_format} {...others}/>
    );
  }
}

DateValue.propTypes = {
  ...Basic.DateValue.propTypes,
  showTime: PropTypes.bool
};

DateValue.defaultProps = {
  ...Basic.DateValue.defaultProps,
  showTime: false
};


export default DateValue;
