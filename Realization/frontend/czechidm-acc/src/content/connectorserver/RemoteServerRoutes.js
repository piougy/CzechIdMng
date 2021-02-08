import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import { Basic, Advanced, Utils } from 'czechidm-core';
import { RemoteServerManager } from '../../redux';
import RemoteServerDetail from './RemoteServerDetail';

const manager = new RemoteServerManager();

/**
 * Remote server with connectors.
 *
 * @author Radek Tomiška
 * @since 10.8.0
 */
class RemoteServerRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'acc:content.remote-servers';
  }

  componentDidMount() {
    const { entityId } = this.props.match.params;

    if (!this._getIsNew()) {
      this.getLogger().debug(`[FormContent] loading entity detail [id:${entityId}]`);
      this.context.store.dispatch(manager.fetchEntity(entityId));
    }
  }

  /**
   * Method check if exist params new
   */
  _getIsNew() {
    return !!Utils.Ui.getUrlParameter(this.props.location, 'new');
  }

  render() {
    const { entity, showLoading } = this.props;
    return (
      <Basic.Div>
        {
          this._getIsNew()
          ?
          <Helmet title={ this.i18n('create.title') } />
          :
          <Helmet title={ this.i18n('edit.title') } />
        }
        {
          (this._getIsNew() || !entity)
          ||
          <Advanced.DetailHeader
            entity={ entity }
            showLoading={ showLoading }
            icon="component:server"
            back="/connector-servers">
            { this.i18n('edit.header', { record: manager.getNiceLabel(entity), escape: false }) }
          </Advanced.DetailHeader>
        }
        {
          this._getIsNew()
          ?
          <RemoteServerDetail isNew match={ this.props.match } />
          :
          <Advanced.TabPanel position="left" parentId="sys-connector-servers" match={ this.props.match }>
            { this.getRoutes() }
          </Advanced.TabPanel>
        }

      </Basic.Div>
    );
  }
}

RemoteServerRoutes.propTypes = {
  entity: PropTypes.object,
  showLoading: PropTypes.bool
};
RemoteServerRoutes.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.match.params;
  //
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(RemoteServerRoutes);
