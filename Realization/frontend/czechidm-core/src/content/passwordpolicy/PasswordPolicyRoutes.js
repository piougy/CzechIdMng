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
 * Default content (routes diff) for password policies
 */

class PasswordPolicyRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.passwordPolicies';
  }

  componentDidMount() {
    const { entityId } = this.props.params;
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
      return query.new ? true : false;
    }
    return false;
  }

  render() {
    const { entity } = this.props;

    return (
      <div>
        {
          this._getIsNew()
          ?
          <Helmet title={this.i18n('create.title')} />
          :
          <Helmet title={this.i18n('edit.title')} />
        }
        {
          this._getIsNew()
          ||
          <Basic.ContentHeader showLoading={!entity} text={
              <div>
                {passwordPolicyManager.getNiceLabel(entity)} <small> {this.i18n('edit.header')}</small>
              </div>
            }/>
        }
        {
          this._getIsNew()
          ?
          <PasswordPolicyBasic params={this.props.params} isNew />
          :
          <Advanced.TabPanel position="left" parentId="password-policies" params={this.props.params}>
            {this.props.children}
          </Advanced.TabPanel>
        }
      </div>
    );
  }
}

PasswordPolicyRoutes.propTypes = {
};
PasswordPolicyRoutes.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;
  //
  return {
    entity: passwordPolicyManager.getEntity(state, entityId)
  };
}

export default connect(select)(PasswordPolicyRoutes);
