import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { DelegationDefinitionManager, IdentityManager } from '../../redux';
import DelegationDefinitionDetail from './DelegationDefinitionDetail';

const manager = new DelegationDefinitionManager();
const identityManager = new IdentityManager();

/**
 * Definition of a delegation.
 *
 * @author Vít Švanda
 * @since 10.4.0
 */
class DelegationDefinitionRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.delegation-definitions';
  }

  componentDidMount() {
    const { delegationId } = this.props.match.params;

    if (!this._getIsNew()) {
      this.context.store.dispatch(manager.fetchEntity(delegationId));
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
          <Helmet title={ this.i18n('edit.title')} />
        }
        {
          (this._getIsNew() || !entity)
          ||
          <Advanced.DetailHeader
            entity={ entity }
            showLoading={ showLoading }
            back="/delegation-definitions">
            { this.i18n('content.delegation-definitions.detailHeader',
              {delegator: identityManager.getNiceLabel(entity._embedded.delegator),
                delegate: identityManager.getNiceLabel(entity._embedded.delegate),
                escape: false })}
          </Advanced.DetailHeader>
        }
        {
          this._getIsNew()
          ?
          <DelegationDefinitionDetail isNew match={ this.props.match } location={ this.props.location} />
          :
          <Advanced.TabPanel position="left" parentId="delegation-definitions" match={ this.props.match }>
            { this.getRoutes() }
          </Advanced.TabPanel>
        }

      </Basic.Div>
    );
  }
}

DelegationDefinitionRoutes.propTypes = {
  entity: PropTypes.object,
  showLoading: PropTypes.bool
};
DelegationDefinitionRoutes.defaultProps = {
};

function select(state, component) {
  const { delegationId } = component.match.params;
  //
  return {
    entity: manager.getEntity(state, delegationId),
    showLoading: manager.isShowLoading(state, null, delegationId)
  };
}

export default connect(select)(DelegationDefinitionRoutes);
