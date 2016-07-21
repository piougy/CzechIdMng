import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import NotificationSystem from 'react-notification-system';
import merge from 'object-assign';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import { FlashMessagesManager } from '../../../modules/core/redux';
import { i18n } from '../../../modules/core/services/LocalizationService';

const _DEFAULT_MESSAGE = {
  id: null, // internal id
  key: null, // key for unique checking
  title: null,
  message: null,
  level: 'success', // React.PropTypes.oneOf(['success', 'info', 'warning', 'error']),
  position: 'tr', // React.PropTypes.oneOf(['tr', 'tc']),
  autoDismiss: 5,
  dismissible: true,
  action: null,
  hidden: false,
  date: new Date()
};

export class FlashMessages extends AbstractComponent {

  constructor(props, context) {
    super(props, context);
    this.flashMessagesManager = new FlashMessagesManager();
  }

  componentWillReceiveProps(nextProps) {
    const unprocessedMessages = this._getUnproccessedMessages(nextProps.messages, this.props.messages);
    unprocessedMessages.added.map(message => {
      this._showMessage(message);
    });
    unprocessedMessages.hidden.map(message => {
      this._hideMessage(message);
    });
  }

  /**
  * Returns newly {added, hidden} mesaages
  */
  _getUnproccessedMessages(newMessages, oldMessages) {
    let added = [];
    const hidden = [];
    if (!oldMessages || !oldMessages.length) {
      added = newMessages;
    } else {
      // show or hide new
      newMessages.map(message => {
        if (!message.hidden) {
          added[message.id] = message;
        } else {
          hidden[message.id] = message;
        }
      });
    }
    return {
      added,
      hidden
    };
  }

  _hideMessage(message) {
    if (message === null) {
      return;
    }
    this.refs.messages.removeNotification(message.id);
  }

  _onRemove(notification) {
    this.context.store.dispatch(this.flashMessagesManager.hideMessage(notification.uid));
  }

  /**
  * Transforms options to message and adds default props
  */
  static getMessage(options) {
    if (!options) {
      return null;
    }
    let message = options;
    if (message == null) {
      message = i18n('message.success.common', { defaultValue: 'The operation was successfully completed' });
    }
    if (typeof message === 'string') {
      message = merge({}, _DEFAULT_MESSAGE, { message });
    }
    // errors are shown centered by default
    if (message.level && (message.level === 'error' /* || message.level === 'warning' */) && !message.position) {
      message.position = 'tc';
    }
    // add default
    message = merge({}, _DEFAULT_MESSAGE, message);
    if (!message.title && !message.message) {
      message.message = i18n('message.success.common', { defaultValue: 'The operation was successfully completed' });
    }
    if (message.title && typeof message.title === 'object') {
      message.title = JSON.stringify(message.title);
    }
    if (message.message && typeof message.message === 'object') {
      message.message = JSON.stringify(message.message);
    }
    return message;
  }

  /**
  * Adds message to UI
  */
  _showMessage(options) {
    if (!options) {
      return;
    }
    const message = FlashMessages.getMessage(options);
    //
    if (message.hidden) {
      // skip hidden messages
      return;
    }
    // show max 3 messages
    let messageCounter = 0;
    this.props.messages.map(m => {
      if (!m.hidden) {
        messageCounter++;
        if (messageCounter > this.props.maxShown) {
          this._hideMessage(m);
        }
      }
    });
    //
    this.refs.messages.addNotification({
      uid: message.id,
      title: message.title,
      message: message.message,
      level: message.level,
      position: message.position,
      onRemove: (n) => this._onRemove(n),
      dismissible: message.dismissible,
      autoDismiss: (message.level === 'error' ? 0 : (message.level === 'success' ? 5 : 10)),
      action: message.action
    });
  }

  _getNotificationSystemStyles() {
    const styles = {
      Containers: {
        DefaultStyle: {
          top: '15px'
        },
        tr: {
          top: '50px',
          bottom: 'auto',
          left: 'auto',
          right: '0px'
        },
        tc: {
          width: '600px',
          margin: '0px auto 0px -300px'
        }
      },
      NotificationItem: { // Override the notification item
        DefaultStyle: { // Applied to every notification, regardless of the notification level
          margin: '10px 100px 2px 0px'
        },

        success: { // Applied only to the success notification item
          // color: 'red'
        }
      },
      ActionWrapper: {
        DefaultStyle: {
          textAlign: 'right'
        }
      }
    };
    return styles;
  }

  render() {
    const styles = this. _getNotificationSystemStyles();
    return (
      <div id="flash-messages">
        <NotificationSystem ref="messages" style={styles}/>
      </div>
    );
  }
}

FlashMessages.propTypes = {
  messages: PropTypes.array,
  maxShown: PropTypes.number
};
FlashMessages.defaultProps = {
  maxShown: 3
};
FlashMessages.contextTypes = {
  store: PropTypes.object.isRequired
};

// Which props do we want to inject, given the global state?
// Note: use https://github.com/faassen/reselect for better performance.
function select(state) {
  return {
    messages: state.messages.messages.toArray()
  };
}

// Wrap the component to inject dispatch and state into it
// this.refs.form.getWrappedInstance().submit() - could call connected instance
export default connect(select)(FlashMessages);
