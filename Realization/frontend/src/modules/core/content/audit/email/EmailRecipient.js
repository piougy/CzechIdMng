import React, { PropTypes } from 'react';
import * as Basic from 'app/components/basic';
import { IdentityManager } from 'core/redux';

/**
 * Email recipient
 */
export default class EmailRecipient extends Basic.AbstractComponent {

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

EmailRecipient.propTypes = {
  ...Basic.AbstractComponent.propTypes,
  recipient: PropTypes.object,
  identityOnly: PropTypes.bool
};
EmailRecipient.defaultProps = {
  ...Basic.AbstractComponent.defaultProps,
  identityOnly: false
};
