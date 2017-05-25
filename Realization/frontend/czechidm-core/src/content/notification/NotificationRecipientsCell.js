import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import NotificationRecipient from './NotificationRecipient';
import { NotificationRecipientManager } from '../../redux';
import { Basic, Domain } from 'czechidm-core';


const recipientManager = new NotificationRecipientManager();

/**
 * Renders cell with notification recipient content
 *
 * @author Peter Šourek
 * @author Radek Tomiška
 */
class NotificationRecipientsCell extends Basic.AbstractContent {

  constructor(props) {
    super(props);
  }

  componentDidMount() {
    const { notifId } = this.props;
    const params = new Domain.SearchParameters().setFilter('notification', notifId);
    this.context.store.dispatch(recipientManager.fetchEntities(params, notifId));
  }


  render() {
    const { identityOnly, _showLoading, _recipients } = this.props;

    if (_showLoading) {
      return (
        <Basic.Icon value="refresh" showLoading />
      );
    }

    if (!_recipients) {
      return null;
    }

    return (
      <span>
        {
          _recipients.map(recipient => {
            return (
              <NotificationRecipient recipient={recipient} identityOnly={identityOnly} />
            );
          })
        }
      </span>
  );
  }
}

NotificationRecipientsCell.propTypes = {
  identityOnly: PropTypes.bool,
  notifId: PropTypes.string.required
};

NotificationRecipientsCell.defaultProps = {
  identityOnly: true
};

function select(state, component) {
  const entities = recipientManager.getEntities(state, component.notifId);
  const result = {
    _recipients: entities,
    _showLoading: recipientManager.isShowLoading(state, component.notifId)
  };
  return result;
}

export default connect(select)(NotificationRecipientsCell);
