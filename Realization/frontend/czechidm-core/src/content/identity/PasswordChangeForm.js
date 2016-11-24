import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import classnames from 'classnames';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import * as Utils from '../../utils';
import { IdentityService } from '../../services';
import { SecurityManager } from '../../redux';

const DISABLED = 'DISABLED';
const ALL_ONLY = 'ALL_ONLY';
const CUSTOM = 'CUSTOM';

const RESOURCE_IDM = '0:czechidm';

const identityService = new IdentityService();
const securityManager = new SecurityManager();

export default class PasswordChangeForm extends Basic.AbstractContent {

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
   * TODO: move to manager
   *
   * @return {[type]} [description]
   */
  _canPasswordChange() {
    const { passwordChangeType, userContext } = this.props;
    const { entityId } = this.props;
    return (passwordChangeType && passwordChangeType !== DISABLED && entityId === userContext.username) || SecurityManager.isAdmin(userContext);
  }

  _initForm() {
    if (this._canPasswordChange()) {
      this.setState({
        preload: false,
        accounts: []
      }, () => {
        const accounts = [RESOURCE_IDM];
        this.refs.form.setData({
          accounts,
          oldPassword: '',
          newPassword: '',
          newPasswordAgain: ''
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
    const { entityId, accountOptions } = this.props;
    const formData = this.refs.form.getData();
    if (formData.newPassword !== formData.newPasswordAgain) {
      return;
    }

    this.setState({
      showLoading: true
    }, this.refs.form.processStarted());
    //
    const requestData = {
      identity: entityId,
      oldPassword: btoa(formData.oldPassword),  // base64
      newPassword: btoa(formData.newPassword),  // base64
      accounts: []
    };
    formData.accounts.map(resourceValue => {
      if (resourceValue === RESOURCE_IDM) {
        requestData.idm = true;
      } else {
        requestData.accounts.push(resourceValue);
      }
    });

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
        throw Utils.Response.getFirstError(json);
      }
      return json;
    })
    .then(() => {
      let accounts = (requestData.idm) ? 'czechidm' : '';
      if (requestData.accounts.length > 0 && accounts) {
        accounts += ', ';
      }
      // accounts += requestData.accounts.join();

      const optionsNames = [];
      requestData.accounts.forEach(obj => {
        optionsNames.push(
          accountOptions[_.findKey(accountOptions, ['value', obj])].niceLabel
        );
      });
      accounts += optionsNames.join();

      this.addMessage({
        message: this.i18n('message.success', { accounts, username: entityId })
      });
      // new token has to be set to security to prevent user logout
      this.context.store.dispatch(securityManager.reloadToken());
      //
      this.refs.form.processEnded();

      // TODO: do we want reset password input after change?
      this.refs.form.setData({
        accounts: formData.accounts,
        oldPassword: null,
        newPassword: null,
        newPasswordAgain: null
      });
    })
    .catch(error => {
      this.addError(error);
    });
  }

  _validatePassword(property, onlyValidate, value, result) {
    if (onlyValidate) {
      this.refs[property].validate();
      return result;
    }
    if (result.error) {
      return result;
    }
    const opositeValue = this.refs[property].getValue();
    if (opositeValue !== value) {
      return {error: {key: 'passwords_not_same'}};
    }
    return result;
  }

  render() {
    const { passwordChangeType, requireOldPassword, userContext, accountOptions } = this.props;
    const { preload } = this.state;
    const allOnlyWarningClassNames = classnames(
      'form-group',
      { 'hidden': passwordChangeType !== ALL_ONLY || SecurityManager.isAdmin(userContext) }
    );

    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <form onSubmit={this.save.bind(this)}>
          <Basic.Row>
            <Basic.Panel className="col-lg-7 no-border last">
              <Basic.PanelHeader text={this.i18n('header')}/>

              <Basic.Loading className="static" showLoading={preload && this._canPasswordChange()}/>
              <Basic.Alert
                level="warning"
                icon="exclamation-sign"
                text={this.i18n('changeType.DISABLED')}
                rendered={!this._canPasswordChange() && passwordChangeType === DISABLED}/>
              <Basic.Alert
                level="warning"
                icon="exclamation-sign"
                text={this.i18n('message.wrongUser')}
                rendered={!this._canPasswordChange() && passwordChangeType !== DISABLED}/>
              {
                (!this._canPasswordChange() || preload)
                ||
                <div>
                  <Basic.Alert
                    icon="info-sign"
                    text={this.i18n('message.isAdmin')}
                    rendered={SecurityManager.isAdmin(userContext)}
                    style={{ margin: '15px 0 0 0'}}/>

                  <Basic.AbstractForm ref="form" className="form-horizontal">
                    <Basic.TextField type="password" ref="oldPassword" label={this.i18n('password.old')} hidden={!requireOldPassword || SecurityManager.isAdmin(userContext)} required={requireOldPassword && !SecurityManager.isAdmin(userContext)}/>
                    <Basic.TextField type="password" ref="newPassword"
                      validate={this._validatePassword.bind(this, 'newPasswordAgain', true)}
                      label={this.i18n('password.new')} required/>
                    <Basic.TextField type="password" ref="newPasswordAgain"
                      validate={this._validatePassword.bind(this, 'newPassword', false)}
                      label={this.i18n('password.newAgain')} required/>

                    <Basic.EnumSelectBox
                      ref="accounts"
                      label={this.i18n('accounts.label')}
                      placeholder={this.i18n('accounts.placeholder')}
                      multiSelect
                      options={accountOptions}
                      required
                      disabled={passwordChangeType === ALL_ONLY && !SecurityManager.isAdmin(userContext)}/>

                      <div className={allOnlyWarningClassNames}>
                        <div className="col-sm-offset-3 col-sm-8">
                          <Basic.Alert key="changeAllOnly" icon="exclamation-sign" text={this.i18n('changeType.ALL_ONLY')} className="last no-margin"/>
                        </div>
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
          </Basic.Row>
        </form>
      </div>
    );
  }
}

PasswordChangeForm.propTypes = {
  passwordChangeType: PropTypes.oneOf([DISABLED, ALL_ONLY, CUSTOM]),
  requireOldPassword: PropTypes.bool,
  userContext: PropTypes.object,
  accountOptions: PropTypes.object,
  showLoading: PropTypes.bool,
  entityId: PropTypes.string
};
PasswordChangeForm.defaultProps = {
  passwordChangeType: ALL_ONLY,
  requireOldPassword: true,
  userContext: null
};
