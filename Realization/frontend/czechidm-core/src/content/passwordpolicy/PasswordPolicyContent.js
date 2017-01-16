import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import * as Basic from '../../components/basic';
import { PasswordPolicyManager } from '../../redux';
import PasswordPolicyDetail from './PasswordPolicyDetail';
import PasswordPolicyTypeEnum from '../../enums/PasswordPolicyTypeEnum';
import AbstractEnum from '../../enums/AbstractEnum';

const passwordPolicyManager = new PasswordPolicyManager();

/**
 * Password policies content
 */
class PasswordPolicyContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.passwordPolicies';
  }

  componentDidMount() {
    this.selectNavigationItem('passwordPolicies');
    const { entityId } = this.props.params;

    if (this._getIsNew()) {
      this.context.store.dispatch(passwordPolicyManager.receiveEntity(entityId, {
        type: AbstractEnum.findKeyBySymbol(PasswordPolicyTypeEnum, PasswordPolicyTypeEnum.VALIDATE)
      }));
    } else {
      this.getLogger().debug(`[TypeContent] loading entity detail [id:${entityId}]`);
      this.context.store.dispatch(passwordPolicyManager.fetchEntity(entityId));
    }
  }

  /**
   * Method check if exist params new
   */
  _getIsNew() {
    const { query } = this.props.location;
    return (query) ? query.new : null;
  }

  render() {
    const { entity, showLoading } = this.props;
    return (
      <div>
        {
          this._getIsNew()
          ?
          <Helmet title={this.i18n('create.title')} />
          :
          <Helmet title={this.i18n('edit.title')} />
        }
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        {
          !entity
          ||
          <Basic.PageHeader>
            <Basic.Icon value="fa:clone"/>
            {' '}
            {
              this._getIsNew()
              ?
              this.i18n('create.header')
              :
              <span>{entity.name} <small>{this.i18n('edit.header')}</small></span>
            }
          </Basic.PageHeader>
        }

        <Basic.Panel>
          <Basic.Loading isStatic showLoading={showLoading} />
          {
            !entity
            ||
            <PasswordPolicyDetail entity={entity} />
          }
        </Basic.Panel>

      </div>
    );
  }
}

PasswordPolicyContent.propTypes = {
  showLoading: PropTypes.bool
};
PasswordPolicyContent.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;
  //
  return {
    entity: passwordPolicyManager.getEntity(state, entityId),
    showLoading: passwordPolicyManager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(PasswordPolicyContent);
