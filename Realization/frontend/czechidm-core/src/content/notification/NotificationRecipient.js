import React, { PropTypes } from 'react';
//
import * as Basic from '../../components/basic';
import { IdentityManager } from '../../redux';

/**
 * Notification recipient
 */
export default class NotificationRecipient extends Basic.AbstractComponent {

  constructor(props) {
    super(props);
    this.identityManager = new IdentityManager();
  }

  render() {
    const { rendered, recipient, identityOnly, ...others } = this.props;
    //
    if (!recipient || !rendered) {
      return null;
    }
    return (
      <div {...others}>
        {
          identityOnly
          ?
          this.identityManager.getNiceLabel(recipient._embedded.identityRecipient)
          :
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
  ...Basic.AbstractComponent.propTypes,
  recipient: PropTypes.object,
  identityOnly: PropTypes.bool
};
NotificationRecipient.defaultProps = {
  ...Basic.AbstractComponent.defaultProps,
  identityOnly: false
};
