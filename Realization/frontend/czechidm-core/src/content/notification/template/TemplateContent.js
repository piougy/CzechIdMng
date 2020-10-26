import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
//
import * as Basic from '../../../components/basic';
import { NotificationTemplateManager } from '../../../redux';
import TemplateDetail from './TemplateDetail';

const manager = new NotificationTemplateManager();

/**
 * Notification template detail content.
 *
 * @author Ondřej Kopr
 * @author Radek Tomiška
 */
class TemplateContent extends Basic.AbstractContent {

  getContentKey() {
    return 'content.notificationTemplate';
  }

  _getIsNew() {
    const { query } = this.props.location;
    return (query) ? query.new : null;
  }

  componentDidMount() {
    this.selectNavigationItem('notification-templates');
    const { entityId } = this.props.match.params;
    const isNew = this._getIsNew();
    if (isNew) {
      this.context.store.dispatch(manager.receiveEntity(entityId, { }));
    } else {
      this.getLogger().debug(`[TemplateContent] loading entity detail [id:${entityId}]`);
      this.context.store.dispatch(manager.fetchEntity(entityId));
    }
  }

  render() {
    const { template, showLoading } = this.props;
    const isNew = this._getIsNew();
    return (
      <Basic.Div>
        <Helmet title={
            isNew
            ?
            this.i18n('titleNew')
            :
            this.i18n('title')
        } />

        <Basic.PageHeader>
          <Basic.Icon value="fa:envelope-square"/>
          {' '}
          {
            isNew
            ?
            this.i18n('headerNew')
            :
            this.i18n('header')
          }
        </Basic.PageHeader>

        <Basic.Panel showLoading={ showLoading } >
          {
            !template
            ||
            <TemplateDetail entity={ template } isNew={ !!isNew } uiKey="templateDetail" />
          }
        </Basic.Panel>
      </Basic.Div>
    );
  }
}

TemplateContent.propTypes = {
  notification: PropTypes.object,
  showLoading: PropTypes.bool
};
TemplateContent.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.match.params;
  //
  return {
    template: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(TemplateContent);
