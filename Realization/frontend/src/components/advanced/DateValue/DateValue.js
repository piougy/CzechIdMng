'use strict';

import React, { PropTypes } from 'react';
import classNames from 'classnames';
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
    const { format, rendered, ...others } = this.props;
    if (!rendered) {
      return null;
    }

    let _format = format;
    if (!_format) {
      _format = this.i18n('format.date');
    }

    return (
      <Basic.DateValue format={_format} {...others}/>
    );
  }
}

DateValue.propTypes = {
  ...Basic.DateValue.propTypes
}

DateValue.defaultProps = {
  ...Basic.DateValue.defaultProps
}


export default DateValue;
