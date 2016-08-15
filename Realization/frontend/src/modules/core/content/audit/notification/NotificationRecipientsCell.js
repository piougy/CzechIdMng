import React from 'react';
//
import NotificationRecipient from './NotificationRecipient';

/**
 * Renders cell with notification recipient content
 */
const NotificationRecipientsCell = ({rowIndex, data, property, identityOnly}) => {
  return (
    <span>
      {
        data[rowIndex][property].map(recipient => {
          return (
            <NotificationRecipient recipient={recipient} identityOnly={identityOnly} />
          );
        })
      }
    </span>
  );
};

export default NotificationRecipientsCell;
