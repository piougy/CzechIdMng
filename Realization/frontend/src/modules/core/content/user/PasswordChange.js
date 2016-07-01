'use strict';

import React, { PropTypes } from 'react';
import ReactDOM from 'react-dom';
import Helmet from 'react-helmet';
import { Link }  from 'react-router';
import classnames from 'classnames';
import { connect } from 'react-redux';
//
import * as Basic from '../../../../components/basic';
import * as Utils from '../../utils';
import { IdentityService } from '../../../../modules/core/services';
import { SettingManager } from '../../../../redux';
import { SecurityManager } from '../../../../modules/core/redux';

const DISABLED = 'DISABLED';
const ALL_ONLY = 'ALL_ONLY';
const CUSTOM = 'CUSTOM';

const UI_KEY = 'user-accounts';
const RESOURCE_IDM = '0:czechidm';

const identityService = new IdentityService();
const settingManager = new SettingManager();
const securityManager = new SecurityManager();

class PasswordChange extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      preload: true,
      showLoading: false,
      accounts: []
    }
  }

  getContentKey() {
    return 'content.user.passwordChange';
  }

  /**
   * TODO: move to manager
   *
   * @return {[type]} [description]
   */
  _canPasswordChange() {
    const { passwordChangeType, userContext } = this.props;
    const { userID } = this.props.params;
    return (passwordChangeType && passwordChangeType !== DISABLED && userID === userContext.username) || SecurityManager.isAdmin(userContext);
  }

  _initForm() {
    const { userID } = this.props.params;
    const { passwordChangeType } = this.props;
    if (this._canPasswordChange()) {
      this.setState({
        preload: false,
        accounts: []
      }, () => {
        let resources = [RESOURCE_IDM];
        this.refs.form.setData({
          resources: resources,
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
    const { userID } = this.props.params;
    const formData = this.refs.form.getData();
    if (formData.newPassword !== formData.newPasswordAgain) {
      return;
    }

    this.setState({
      showLoading: true
    }, this.refs.form.processStarted());
    //
    let requestData = {
      identity: userID,
      oldPassword: btoa(formData.oldPassword),  // base64
      newPassword: btoa(formData.newPassword),  // base64
      resources: []
    }
    formData.resources.map(resourceValue => {
      if (resourceValue === RESOURCE_IDM) {
        requestData.idm = true;
      } else {
        requestData.resources.push(resourceValue);
      }
    });

    identityService.passwordChange(userID, requestData)
    .then(response => {
      this.setState({
        showLoading: false
      },this.refs.form.processEnded());
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
    .then(json => {
      let resources = (requestData.idm) ? 'czechidm' : '';
      if (requestData.resources.length > 0 && resources) {
        resources += ', '
      }
      resources += requestData.resources.join();

      this.addMessage({
        message: this.i18n('message.success', { resources: resources, username: userID })
      });
      // new token has to be set to security to prevent user logout
      this.context.store.dispatch(securityManager.reloadToken());
      //
      this.refs.form.processEnded();
      /*
      // TODO: do we want reset password input after change?
      this.refs.form.setData({
        resources: formData.resources,
        oldPassword: null,
        newPassword: null,
        newPasswordAgain: null
      });*/
    })
    .catch(error => {
      this.addError(error);
    });
  }

  _getOptions() {
    const { userID } = this.props.params;
    const { accounts } = this.state;
    let options = [
      { value: RESOURCE_IDM, niceLabel: 'czechidm (' + userID + ')'}
    ];
    if (accounts) {
      accounts.map(account => {
        options.push({ value: account.resource, niceLabel: this.identityAccountManager.getNiceLabel(account) });
      });
    }
    return options;
  }

  _validatePassword(property,onlyValidate,value,result){
    if (onlyValidate){
      this.refs[property].validate();
      return result;
    }
    if (result.error){
      return result;
    }
    let opositeValue = this.refs[property].getValue();
    if (opositeValue !== value){
      return {error:{key:'passwords_not_same'}}
    }
    return result;
  }

  render() {
    const { passwordChangeType, requireOldPassword, userContext } = this.props;
    const { preload, showLoading } = this.state;
    const allOnlyWarningClassNames = classnames(
      'form-group',
      { 'hidden': passwordChangeType !== ALL_ONLY || SecurityManager.isAdmin(userContext) }
    );
    const accountOptions = this._getOptions();

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

                  <Basic.AbstractForm ref="form">
                    <Basic.TextField type="password" ref="oldPassword" label={this.i18n('password.old')} hidden={!requireOldPassword || SecurityManager.isAdmin(userContext)} required={requireOldPassword && !SecurityManager.isAdmin(userContext)}/>
                    <Basic.TextField type="password" ref="newPassword"
                      validate={this._validatePassword.bind(this, 'newPasswordAgain', true)}
                      label={this.i18n('password.new')} required/>
                    <Basic.TextField type="password" ref="newPasswordAgain"
                      validate={this._validatePassword.bind(this, 'newPassword', false)}
                      label={this.i18n('password.newAgain')} required/>

                    <Basic.EnumSelectBox
                      ref="resources"
                      label={this.i18n('resources.label')}
                      placeholder={this.i18n('resources.placeholder')}
                      multiSelect={true}
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

PasswordChange.propTypes = {
  passwordChangeType: PropTypes.oneOf([DISABLED, ALL_ONLY, CUSTOM]),
  requireOldPassword: PropTypes.bool,
  userContext: PropTypes.object,
}
PasswordChange.defaultProps = {
  passwordChangeType: ALL_ONLY,
  requireOldPassword: true,
  userContext: null
}

function select(state) {
  return {
    //passwordChangeType: settingManager.getValue(state, 'password.change') || DISABLED,
    //requireOldPassword: settingManager.getValue(state, 'password.change.require.old') === 'true',
    userContext: state.security.userContext
  }
}
export default connect(select)(PasswordChange)
