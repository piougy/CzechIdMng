import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import * as Basic from '../../../components/basic';
import { SmsManager } from '../../../redux';
import NotificationDetail from '../NotificationDetail';

const manager = new SmsManager();

/**
 * Sms audit log detail content.
 *
 * @author Peter Sourek
 */
class SmsContent extends Basic.AbstractContent {

  getContentKey() {
    return 'content.sms';
  }

  componentDidMount() {
    this.selectNavigationItem('notification-sms');
    const { entityId } = this.props.match.params;
    //
    this.getLogger().debug(`[SmsContent] loading entity detail [id:${entityId}]`);
    this.context.store.dispatch(manager.fetchEntity(entityId));
  }

  render() {
    const { entity, showLoading } = this.props;
    return (
      <Basic.Div>
        <Helmet title={this.i18n('title')} />

        <Basic.PageHeader>
          <Basic.Icon value="fa:envelope-o"/>
          {' '}
          {this.i18n('header')}
        </Basic.PageHeader>

        <Basic.Loading isStatic showLoading={showLoading} />
        {
          !entity
          ||
          <NotificationDetail notification={ entity } showTopic={ false }/>
        }

      </Basic.Div>
    );
  }
}

SmsContent.propTypes = {
  entity: PropTypes.object,
  showLoading: PropTypes.bool
};
SmsContent.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.match.params;
  //
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(SmsContent);
