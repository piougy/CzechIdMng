import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { DelegationDefinitionManager } from '../../redux';
import DelegationDefinitionDetail from '../delegation/DelegationDefinitionDetail';

const manager = new DelegationDefinitionManager();

/**
 * Definition of a delegation.
 *
 * @author Vít Švanda
 * @since 10.4.0
 */
class IdentityDelegationDefinitionRoutes extends Basic.AbstractContent {

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
    const { entity } = this.props;

    return (
      <Basic.Div>
        {
          this._getIsNew()
          ?
          <Helmet title={ this.i18n('create.title') } />
          :
          <Helmet title={ this.i18n('edit.title')} />
        }
        <Basic.ContentHeader>
          <Basic.Icon value="fa:dolly"/>
          {' '}
          { this.i18n('header') }
        </Basic.ContentHeader>
        <Basic.Div rendered={this._getIsNew()}>
          <DelegationDefinitionDetail isNew match={ this.props.match } location={ this.props.location} />
        </Basic.Div>
        <Basic.Div rendered={!this._getIsNew() && entity}>
          <Advanced.TabPanel position="top" parentId="identity-delegation-definitions" match={ this.props.match }>
            { this.getRoutes() }
          </Advanced.TabPanel>
        </Basic.Div>
      </Basic.Div>
    );
  }
}

IdentityDelegationDefinitionRoutes.propTypes = {
  entity: PropTypes.object,
  showLoading: PropTypes.bool
};
IdentityDelegationDefinitionRoutes.defaultProps = {
};

function select(state, component) {
  const { delegationId } = component.match.params;
  //
  return {
    entity: manager.getEntity(state, delegationId),
    showLoading: manager.isShowLoading(state, null, delegationId)
  };
}

export default connect(select)(IdentityDelegationDefinitionRoutes);
