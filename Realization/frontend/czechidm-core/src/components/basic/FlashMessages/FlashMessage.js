import React, { PropTypes } from 'react';
//
import AbstractContextComponent from '../AbstractContextComponent/AbstractContextComponent';
import Alert from '../Alert/Alert';
import DateValue from '../DateValue/DateValue';

/**
 * Renders flash message as Alert
 */
export default class FlashMessage extends AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
  }

  render() {
    const { rendered, message, showDate, onClose, style } = this.props;
    //
    if (!rendered || !message) {
      return null;
    }
    //
    let _title = message.title;
    if (showDate) {
      _title = (
        <div>
          <small> <DateValue value={message.date} format={this.i18n('format.datetime')}/></small>
          <div>{ _title }</div>
        </div>
      );
    }
    return (
      <Alert
        level={message.level}
        title={_title}
        text={message.message}
        onClose={onClose}
        style={style}/>
    );
  }
}

FlashMessage.propTypes = {
  rendered: PropTypes.bool,
  message: PropTypes.object,
  showDate: PropTypes.bool,
  /**
   * Close function - if it's set, then close icon is shown and this method is called on icon click
   */
  onClose: PropTypes.func
};
FlashMessage.defaultProps = {
  rendered: true,
  message: null,
  showDate: false
};
