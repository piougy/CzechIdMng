import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import { Basic } from 'czechidm-core';
import { VsRequestManager } from '../../redux';
import VsRequestDetail from './VsRequestDetail';

const manager = new VsRequestManager();

/**
 * Virtual system request detail wrapper
 *
 * @author Vít Švanda
 */
class VsRequestContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  /**
   * "Shorcut" for localization
   */
  getContentKey() {
    return 'vs:content.vs-request.detail';
  }

  /**
   * Selected navigation item
   */
  getNavigationKey() {
    return 'vs-request-detail';
  }

  componentDidMount() {
    super.componentDidMount();
    //
    const { entityId } = this.props.params;
    if (this._isNew()) {
      // persist new entity to redux
      this.context.store.dispatch(manager.receiveEntity(entityId, { }));
    } else {
      this.context.store.dispatch(manager.fetchEntity(entityId));
    }
  }

  /**
   * Helper - returns `true`, when new entity is created
   */
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
            <VsRequestDetail uiKey="vs-request-detail" entity={entity} />
          }
        </div>
      </Basic.Row>
    );
  }
}

VsRequestContent.propTypes = {
  /**
   * Loaded entity
   */
  entity: PropTypes.object,
  /**
   * Entity is currently loaded from BE
   */
  showLoading: PropTypes.bool
};
VsRequestContent.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;
  //
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(VsRequestContent);
