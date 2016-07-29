import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
//
import * as Basic from 'app/components/basic';
import { NotificationManager } from 'core/redux';
import NotificationDetail from './NotificationDetail';

const notificationManager = new NotificationManager();

/**
 * Notification detail content
 */
class NotificationContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.notification';
  }

  componentDidMount() {
    this.selectNavigationItem('notifications');
    const { entityId } = this.props.params;
    //
    this.getLogger().debug(`[NotificationContent] loading entity detail [id:${entityId}]`);
    this.context.store.dispatch(notificationManager.fetchEntity(entityId));
  }

  render() {
    const { notification, showLoading } = this.props;
    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.PageHeader>
          <Basic.Icon value="fa:envelope"/>
          {' '}
          {this.i18n('header')}
        </Basic.PageHeader>

        <Basic.Panel>
          <Basic.Loading isStatic showLoading={showLoading} />
          {
            !notification
            ||
            <NotificationDetail notification={notification} />
          }
          <Basic.PanelFooter rendered={!showLoading}>
            <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
          </Basic.PanelFooter>
        </Basic.Panel>

      </div>
    );
  }
}

NotificationContent.propTypes = {
  notification: PropTypes.object,
  showLoading: PropTypes.bool
};
NotificationContent.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;
  //
  return {
    notification: notificationManager.getEntity(state, entityId),
    showLoading: notificationManager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(NotificationContent);
