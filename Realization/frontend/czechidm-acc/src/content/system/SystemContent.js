import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import { Basic } from 'czechidm-core';
import { SystemManager } from '../../redux';
import SystemDetail from './SystemDetail';

const manager = new SystemManager();

/**
 * Target system detail content
 */
class SystemContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'acc:content.system.detail';
  }

  componentDidMount() {
    this.selectNavigationItems(['sys-systems', 'system-detail']);
    const { entityId } = this.props.params;

    if (this._isNew()) {
      this.context.store.dispatch(manager.receiveEntity(entityId, { }));
    } else {
      this.getLogger().debug(`[SystemContent] loading entity detail [id:${entityId}]`);
      this.context.store.dispatch(manager.fetchEntity(entityId));
    }
  }

  _isNew() {
    const { query } = this.props.location;
    return (query) ? query.new : null;
  }

  render() {
    const { entity, showLoading } = this.props;
    return (
      <Basic.Row>
        <div className={this._isNew() ? 'col-lg-offset-1 col-lg-10' : 'col-lg-12'}>
          {
            showLoading
            ?
            <Basic.Loading isStatic showLoading />
            :
            <SystemDetail uiKey="system-detail" entity={entity} />
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
  const { entityId } = component.params;
  //
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(SystemContent);
