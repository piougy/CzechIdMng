import React, { PropTypes } from 'react';
//
import * as Basic from 'app/components/basic';
import { IdentityManager } from 'core/redux';

/**
 * List of notifications
 */
export default class NotificationRecipient extends Basic.AbstractComponent {

  constructor(props) {
    super(props);
    this.identityManager = new IdentityManager();
  }

  render() {
    const { recipient } = this.props;
    //
    if (!recipient) {
      return null;
    }
    return (
      <div>
        {
          recipient.realRecipient
          ||
          !recipient._embedded
          ||
          this.identityManager.getNiceLabel(recipient._embedded.identityRecipient)
        }
      </div>
    );
  }
}

NotificationRecipient.propTypes = {
  recipient: PropTypes.object
};
NotificationRecipient.defaultProps = {
};
