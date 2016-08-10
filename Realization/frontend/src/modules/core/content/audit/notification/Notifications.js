import React from 'react';
import Helmet from 'react-helmet';
//
import * as Basic from 'app/components/basic';
import { NotificationManager } from 'core/redux';
import NotificationTable from './NotificationTable';

/**
 * List of notifications
 */
export default class Notifications extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.notificationManager = new NotificationManager();
  }

  getContentKey() {
    return 'content.notifications';
  }

  componentDidMount() {
    this.selectNavigationItem('notifications');
  }

  render() {
    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.PageHeader>
          <Basic.Icon value="fa:envelope"/>
          {' '}
          {this.i18n('header')}
        </Basic.PageHeader>

        <Basic.Panel>
          <NotificationTable uiKey="notification_table" notificationManager={this.notificationManager} filterOpened/>
        </Basic.Panel>

      </div>
    );
  }
}

Notifications.propTypes = {
};
Notifications.defaultProps = {
};
