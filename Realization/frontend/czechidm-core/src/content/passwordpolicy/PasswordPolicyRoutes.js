import React from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import PasswordPolicyBasic from './PasswordPolicyBasic';
import { PasswordPolicyManager } from '../../redux';

const passwordPolicyManager = new PasswordPolicyManager();

/**
 * Default content (routes diff) for password policies.
 *
 * @author Ondrej Kopr
 * @author Radek Tomiška
 */
class PasswordPolicyRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.passwordPolicies';
  }

  componentDidMount() {
    const { entityId } = this.props.match.params;
    if (!this._getIsNew()) {
      this.getLogger().debug(`[TypeContent] loading entity detail [id:${entityId}]`);
      this.context.store.dispatch(passwordPolicyManager.fetchEntity(entityId));
    }
  }

  /**
   * Method check if exist params new
   */
  _getIsNew() {
    const { query } = this.props.location;
    if (query) {
      return !!query.new;
    }
    return false;
  }

  render() {
    const { entity, showLoading } = this.props;

    return (
      <Basic.Div>
        {
          this._getIsNew()
          ?
          <Helmet title={this.i18n('create.title')} />
          :
          <Helmet title={this.i18n('edit.title')} />
        }
        {
          this._getIsNew()
          ?
          <PasswordPolicyBasic match={ this.props.match } isNew />
          :
          <Basic.Div>
            <Advanced.DetailHeader
              icon="component:password-policy"
              entity={ entity }
              showLoading={ !entity && showLoading }
              back="/password-policies">
              { passwordPolicyManager.getNiceLabel(entity) } <small> { this.i18n('edit.header')}</small>
            </Advanced.DetailHeader>

            <Advanced.TabPanel position="left" parentId="password-policies" match={ this.props.match }>
              {this.getRoutes()}
            </Advanced.TabPanel>
          </Basic.Div>
        }
      </Basic.Div>
    );
  }
}

PasswordPolicyRoutes.propTypes = {
};
PasswordPolicyRoutes.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.match.params;
  //
  return {
    entity: passwordPolicyManager.getEntity(state, entityId),
    showLoading: passwordPolicyManager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(PasswordPolicyRoutes);
