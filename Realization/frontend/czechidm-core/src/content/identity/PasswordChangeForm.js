import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import classnames from 'classnames';
import _ from 'lodash';
//
import * as Advanced from '../../components/advanced';
import * as Basic from '../../components/basic';
import * as Utils from '../../utils';
import { IdentityService } from '../../services';
import { SecurityManager, IdentityManager, ConfigurationManager } from '../../redux';
import ValidationMessage from './ValidationMessage';

const RESOURCE_IDM = '0:CzechIdM';

const PASSWORD_DOES_NOT_MEET_POLICY = 'PASSWORD_DOES_NOT_MEET_POLICY';

const identityService = new IdentityService();
const securityManager = new SecurityManager();
const identityManager = new IdentityManager();

/**
 *
 * @author Ondřej Kopr
 */
class PasswordChangeForm extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      preload: true,
      showLoading: false,
      accounts: []
    };
  }

  getContentKey() {
    return 'content.identity.passwordChange';
  }

  /**
   * Return true when currently logged user can change password
   *
   */
  _canPasswordChange() {
    const { passwordChangeType, _permissions, userContext } = this.props;
    //
    // Check if current logged user is admin
    if (SecurityManager.isAdmin(userContext)) {
      return true;
    }
    //
    return identityManager.canChangePassword(passwordChangeType, _permissions);
  }

  _initForm() {
    const { accountOptions } = this.props;
    if (this._canPasswordChange()) {
      this.setState({
        preload: false,
        accounts: []
      }, () => {
        this.refs.form.setData({
          accounts: accountOptions,
          oldPassword: ''
        });
        // focus old password
        this.refs.oldPassword.focus();
      });
    }
  }

  componentDidMount() {
    this.selectSidebarItem('profile-password');
    this._initForm();
  }

  save(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const { entityId, accountOptions, userContext } = this.props;
    const formData = this.refs.form.getData();

    // add data from child component to formData
    formData.newPassword = this.refs.passwords.getValue();

    if (!this.refs.passwords.validate()) {
      return;
    }

    this.setState({
      showLoading: true
    }, this.refs.form.processStarted());
    //
    const requestData = {
      oldPassword: formData.oldPassword,
      newPassword: formData.newPassword,
      accounts: []
    };
    if (IdentityManager.PASSWORD_ALL_ONLY && !SecurityManager.isAdmin(userContext)) {
      requestData.all = true;
      requestData.idm = true;
    } else {
      formData.accounts.map(resourceValue => {
        if (resourceValue === RESOURCE_IDM) {
          requestData.idm = true;
        } else {
          requestData.accounts.push(resourceValue);
        }
      });
    }

    identityService.passwordChange(entityId, requestData)
    .then(response => {
      this.setState({
        showLoading: false
      }, this.refs.form.processEnded());
      if (response.status === 204) {
        return {};
      }
      return response.json();
    })
    .then(json => {
      if (Utils.Response.hasError(json)) {
        const error = Utils.Response.getFirstError(json);

        this.setState({
          validationError: error
        });

        throw error;
      }
      return json;
    })
    .then(json => {
      let accounts = (json.idm) ? 'CzechIdM' : '';
      if (json.accounts.length > 0 && accounts) {
        accounts += ', ';
      }

      const optionsNames = [];
      json.accounts.forEach(obj => {
        optionsNames.push(
          accountOptions[_.findKey(accountOptions, ['value', obj])].niceLabel
        );
      });
      accounts += optionsNames.join();

      this.addMessage({
        message: this.i18n('message.success', { accounts, username: entityId })
      });
      this.setState({
        validationError: null
      });
      // new token has to be set to security to prevent user logout
      this.context.store.dispatch(securityManager.reloadToken());
      //
      this.refs.form.processEnded();

      // TODO: do we want reset password input after change?
      /* this.refs.form.setData({
        accounts: formData.accounts,
        oldPassword: null,
        newPassword: null,
        newPasswordAgain: null
      }); */
    })
    .catch(error => {
      if (error.statusEnum === PASSWORD_DOES_NOT_MEET_POLICY) {
        this.addErrorMessage({hidden: true}, error);
      } else {
        this.addError(error);
      }

      this.refs.form.setData({
        accounts: formData.accounts,
        oldPassword: formData.oldPassword
      });
      this.refs.passwords.setValue(formData.newPassword);
    });
  }

  render() {
    const { passwordChangeType, userContext, accountOptions, requireOldPasswordConfig, entityId } = this.props;
    const { preload, validationError } = this.state;
    const allOnlyWarningClassNames = classnames(
      'form-group',
      { 'hidden': passwordChangeType !== IdentityManager.idm || SecurityManager.isAdmin(userContext) }
    );
    //
    // if current user is admin, old password is never required
    let oldPasswordRequired = (entityId === userContext.username) && !SecurityManager.isAdmin(userContext);
    if (oldPasswordRequired) {
      oldPasswordRequired = requireOldPasswordConfig;
    }
    //
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <form onSubmit={this.save.bind(this)}>
          <Basic.Row>
            <Basic.Panel className="col-lg-7 no-border">
              <Basic.PanelHeader text={this.i18n('header')}/>

              <Basic.Loading className="static" showLoading={preload && this._canPasswordChange()}/>
              <Basic.Alert
                level="warning"
                icon="exclamation-sign"
                text={ this.i18n('changeType.DISABLED') }
                rendered={ passwordChangeType === IdentityManager.PASSWORD_DISABLED && !SecurityManager.isAdmin(userContext)}/>
              <Basic.Alert
                level="warning"
                icon="exclamation-sign"
                text={ this.i18n('permission.failed') }
                rendered={ !this._canPasswordChange() && passwordChangeType !== IdentityManager.PASSWORD_DISABLED }/>
              {
                (!this._canPasswordChange() || preload)
                ||
                <div>
                  <Basic.Alert
                    icon="info-sign"
                    text={this.i18n('message.isAdmin')}
                    rendered={ SecurityManager.isAdmin(userContext) }
                    style={{ margin: '15px 0 0 0'}}/>

                  <Basic.AbstractForm ref="form">
                    <Basic.TextField type="password" ref="oldPassword" label={this.i18n('password.old')}
                      hidden={!oldPasswordRequired}
                      required={oldPasswordRequired}/>

                    <Advanced.PasswordField className="form-control" ref="passwords" />

                    <Basic.EnumSelectBox
                      ref="accounts"
                      label={this.i18n('accounts.label')}
                      placeholder={this.i18n('accounts.placeholder')}
                      multiSelect
                      options={accountOptions}
                      required
                      disabled={passwordChangeType === IdentityManager.PASSWORD_ALL_ONLY && !SecurityManager.isAdmin(userContext)}/>

                    <div className={allOnlyWarningClassNames}>
                      <Basic.Alert key="changeAllOnly" icon="exclamation-sign" text={this.i18n('changeType.ALL_ONLY')} className="last no-margin"/>
                    </div>
                  </Basic.AbstractForm>
                  <Basic.PanelFooter>
                    <Basic.Button
                      type="submit"
                      level="success"
                      showLoading={this.state.showLoading}>{this.i18n('button.change')}
                    </Basic.Button>
                  </Basic.PanelFooter>
                </div>
              }
            </Basic.Panel>
            <div className="col-lg-5 no-border last">
              <ValidationMessage error={validationError} />
            </div>
          </Basic.Row>
        </form>
      </div>
    );
  }
}

PasswordChangeForm.propTypes = {
  requireOldPassword: PropTypes.bool,
  userContext: PropTypes.object,
  accountOptions: PropTypes.object,
  showLoading: PropTypes.bool,
  entityId: PropTypes.string
};
PasswordChangeForm.defaultProps = {
  requireOldPassword: true,
  userContext: null
};

function select(state, component) {
  return {
    passwordChangeType: ConfigurationManager.getPublicValue(state, 'idm.pub.core.identity.passwordChange'),
    requireOldPasswordConfig: ConfigurationManager.getPublicValueAsBoolean(state, 'idm.pub.core.identity.passwordChange.requireOldPassword'),
    _permissions: identityManager.getPermissions(state, null, component.entityId)
  };
}

export default connect(select)(PasswordChangeForm);
