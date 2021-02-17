import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
//
import { Basic, Managers } from 'czechidm-core';
import { SystemManager } from '../../redux';
import SystemDetail from './SystemDetail';

const manager = new SystemManager();

/**
 * Target system detail content
 *
 * @author Radek Tomiška
 */
class SystemContent extends Basic.AbstractContent {

  getContentKey() {
    return 'acc:content.system.detail';
  }

  getNavigationKey() {
    return 'system-detail';
  }

  componentDidMount() {
    super.componentDidMount();
    //
    const { entityId } = this.props.match.params;
    //
    this.context.store.dispatch(manager.fetchAvailableFrameworks());
    this.context.store.dispatch(manager.fetchAvailableRemoteConnector(entityId));
    if (this._isNew()) {
      this.context.store.dispatch(manager.receiveEntity(entityId, { }));
    } else {
      this.getLogger().debug(`[SystemContent] loading entity detail [id:${entityId}]`);
      this.context.store.dispatch(manager.fetchEntity(entityId));
    }
  }

  _isNew() {
    if (this.props.location) {
      const { query } = this.props.location;
      return (query) ? query.new : null;
    }
    return false;
  }

  render() {
    const { entity, showLoading, availableFrameworks, wizardStepId } = this.props;

    return (
      <Basic.Row>
        <div className={ this._isNew() && !this.isWizard() ? 'col-lg-offset-1 col-lg-10' : 'col-lg-12' }>
          {
            showLoading || !availableFrameworks
            ?
            <Basic.Loading isStatic showLoading />
            :
            <SystemDetail ref="systemDetail" uiKey="system-detail" entity={ entity } wizardStepId={wizardStepId} />
          }
        </div>
      </Basic.Row>
    );
  }
}

SystemContent.propTypes = {
  entity: PropTypes.object,
  showLoading: PropTypes.bool
};
SystemContent.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.match.params;
  //
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId),
    availableFrameworks: Managers.DataManager.getData(state, SystemManager.AVAILABLE_CONNECTORS),
    availableRemoteFrameworks: Managers.DataManager.getData(state, SystemManager.AVAILABLE_REMOTE_CONNECTORS)
  };
}

export default connect(select, null, null, { forwardRef: true})(SystemContent);
