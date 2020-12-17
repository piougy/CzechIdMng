import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
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
    super.componentDidMount();
    //
    const { entityId } = this.props.match.params;
    const isNew = this._getIsNew();
    if (isNew) {
      this.context.store.dispatch(manager.receiveEntity(entityId, { }));
    } else {
      this.getLogger().debug(`[TemplateContent] loading entity detail [id:${entityId}]`);
      this.context.store.dispatch(manager.fetchEntity(entityId));
    }
  }

  getNavigationKey() {
    return 'notification-templates';
  }

  render() {
    const { template, showLoading } = this.props;
    const isNew = this._getIsNew();
    return (
      <Basic.Div>
        <Advanced.DetailHeader
          icon="fa:envelope-square"
          entity={ template }
          showLoading={ !template && showLoading }
          back="/notification/templates">
          {
            isNew
            ?
            this.i18n('headerNew')
            :
            <span>{ manager.getNiceLabel(template)} <small> { this.i18n('edit.header') }</small></span>
          }

        </Advanced.DetailHeader>

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
  const entity = manager.getEntity(state, entityId);
  if (entity) {
    entity.codeable = {
      code: entity.code,
      name: entity.name
    };
  }
  //
  return {
    template: entity,
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(TemplateContent);
