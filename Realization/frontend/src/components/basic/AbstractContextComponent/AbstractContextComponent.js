'use strict';

import React, { PropTypes } from 'react';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import { FlashMessagesManager } from '../../../modules/core/redux';
import { i18n } from '../../../modules/core/services/LocalizationService';

/**
 * Automatically injects redux context (store) to component context,
 * localization,
 * add message to context.
 */
class AbstractContextComponent extends AbstractComponent {

  constructor(props, context) {
     super(props, context);
     this.flashMessagesManager = new FlashMessagesManager();
  }

  /**
   * Add flash message, see more in FlashMessages component
   *
   * @param {Message} message
   * @param {Event} event
   */
  addMessage(message, event) {
    if (event) {
      event.preventDefault();
    }
    this.context.store.dispatch(this.flashMessagesManager.addMessage(message));
  }

  /**
   * Add error flash message, see more in FlashMessages component
   *
   * @param {Error} error message (json failure)
   * @param {Event} event
   */
  addError(error, event) {
    if (event) {
      event.preventDefault();
    }
    this.context.store.dispatch(this.flashMessagesManager.addError(error, this.context));
  }

  /**
   * Add error flash message, see more in FlashMessages component
   *
   * @param {Message} message
   * @param {Error} error message (json failure)
   * @param {Event} event
   */
  addErrorMessage(message, error, event) {
    if (event) {
      event.preventDefault();
    }
    this.context.store.dispatch(this.flashMessagesManager.addErrorMessage(message, error, this.context));
  }

  /**
   * Hide all flash message, see more in FlashMessages component
   */
  hideAllMessages() {
    this.context.store.dispatch(this.flashMessagesManager.hideAllMessages());
  }

  /**
   * Hide flash message by id or key
   *
   * @param  {string} idOrKey message id or key
   */
  hideMessage(idOrKey) {
    this.context.store.dispatch(this.flashMessagesManager.hideMessage(idOrKey));
  }

  /**
   * Returns localized message
   * - for supported options see http://i18next.com/pages/doc_features.html
   *
   * @param  {string} key     localization key
   * @param  {object} options parameters
   * @return {string}         localized message
   */
  i18n(key, options) {
    let result = i18n(key, options);
    // escape html
    if (options && options.escape === false && key !== result) {
      result = <span dangerouslySetInnerHTML={{__html: i18n(key, options)}}/>
    }
    return result;
  }

  /**
   * Returns logger, which is configured for whole app in redux store
   *
   * @return {object} logger
   */
  getLogger() {
    return this.context.store.getState().logger;
  }
}

AbstractContextComponent.propTypes = {
  ...AbstractComponent.propTypes
};

AbstractContextComponent.defaultProps = {
  ...AbstractComponent.defaultProps
};

AbstractContextComponent.contextTypes = {
  store: PropTypes.object
};

// Wrap the component to inject dispatch and state into it
export default AbstractContextComponent;
