import React from 'react';
//
import NotificationRecipient from './NotificationRecipient';

/**
 * Renders cell with notification recipient content
 */
const NotificationRecipientCell = ({rowIndex, data, property, identityOnly}) => {
  //
  return (
    <NotificationRecipient recipient={data[rowIndex][property]} identityOnly={identityOnly} />
  );
};

export default NotificationRecipientCell;
