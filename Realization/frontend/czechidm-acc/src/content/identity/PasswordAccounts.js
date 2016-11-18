import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import classnames from 'classnames';
import _ from 'lodash';
//
import { Basic, Managers, Utils, Services } from 'czechidm-core';
import { IdentityAccountManager } from '../../redux';

const DISABLED = 'DISABLED';
const ALL_ONLY = 'ALL_ONLY';
const CUSTOM = 'CUSTOM';

const RESOURCE_IDM = '0:czechidm';

const identityAccountManager = new IdentityAccountManager();
const securityManager = new Managers.SecurityManager();
const identityService = new Services.IdentityService();

/**
 * TODO: one component for password change with czechidm-core?
 * TODO: In this component include password change and send props with account options
 */

class PasswordAccounts extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      preload: true,
      showLoading: false,
      accounts: []
    };
  }

  componentDidMount() {
    this.selectSidebarItem('profile-password');
    this._initForm();
  }

  getContentKey() {
    // TODO
    return 'content.identity.passwordChange';
  }

  _initForm() {
    const { entityId } = this.props.params;
    const defaultSearchParameters = identityAccountManager.getDefaultSearchParameters().setFilter('ownership', true);

    this.context.store.dispatch(identityAccountManager.fetchEntities(defaultSearchParameters, `${entityId}-accounts`, (identityAccounts) => {
      if (this._canPasswordChange()) {
        this.setState({
          preload: false,
          accounts: identityAccounts
        }, () => {
          const resources = [RESOURCE_IDM];
          this.refs.form.setData({
            resources,
            oldPassword: '',
            newPassword: '',
            newPasswordAgain: ''
          });
          // focus old password
          this.refs.oldPassword.focus();
        });
      }
    }));
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

  _getOptions() {
    const { entityId } = this.props.params;
    const { accounts } = this.state;
    const options = [
      { value: RESOURCE_IDM, niceLabel: 'czechidm (' + entityId + ')'}
    ];

    if (accounts._embedded) {
      accounts._embedded.identityAccounts.map(acc => {
        options.push({
          value: acc.id,
          niceLabel: identityAccountManager.getNiceLabelWithSystem(acc.account._embedded.system.name, acc._embedded.identity.username) });
      });
    }
    return options;
  }

  /**
   * TODO: move to manager
   *
   * @return {[type]} [description]
   */
  _canPasswordChange() {
    const { passwordChangeType, userContext } = this.props;
    const { entityId } = this.props.params;
    return (passwordChangeType && passwordChangeType !== DISABLED && entityId === userContext.username) || Managers.SecurityManager.isAdmin(userContext);
  }

  save(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const { entityId } = this.props.params;
    const formData = this.refs.form.getData();
    if (formData.newPassword !== formData.newPasswordAgain) {
      return;
    }
    // set showLoading
    this.setState({
      showLoading: true
    }, this.refs.form.processStarted());
    //
    const requestData = {
      identity: entityId,
      oldPassword: btoa(formData.oldPassword),  // base64
      newPassword: btoa(formData.newPassword),  // base64
      resources: [] // TODO: this isn't resources, but account! Refactor BE
    };
    formData.resources.map(resourceValue => {
      if (resourceValue === RESOURCE_IDM) {
        requestData.idm = true;
      } else {
        requestData.resources.push(resourceValue);
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
      let resources = (requestData.idm) ? 'czechidm' : '';
      if (requestData.resources.length > 0 && resources) {
        resources += ', ';
      }

      const allOptions = this._getOptions();
      const optionsNames = [];
      requestData.resources.forEach(obj => {
        optionsNames.push(
          allOptions[_.findKey(allOptions, ['value', obj])].niceLabel
        );
      });
      resources += optionsNames.join();

      this.addMessage({
        message: this.i18n('message.success', { resources, username: entityId })
      });
      // new token has to be set to security to prevent user logout
      this.context.store.dispatch(securityManager.reloadToken());
      //
      this.refs.form.processEnded();
      // TODO: do we want reset password input after change?
      this.refs.form.setData({
        resources: formData.resources,
        oldPassword: null,
        newPassword: null,
        newPasswordAgain: null
      });
    })
    .catch(error => {
      this.addError(error);
    });
  }

  render() {
    const { preload } = this.state;
    const { passwordChangeType, requireOldPassword, userContext } = this.props;

    const accountOptions = this._getOptions();

    const allOnlyWarningClassNames = classnames(
      'form-group',
      { 'hidden': passwordChangeType !== ALL_ONLY || Managers.SecurityManager.isAdmin(userContext) }
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
                    rendered={Managers.SecurityManager.isAdmin(userContext)}
                    style={{ margin: '15px 0 0 0'}}/>

                  <Basic.AbstractForm ref="form" className="form-horizontal">
                    <Basic.TextField type="password" ref="oldPassword" label={this.i18n('password.old')} hidden={!requireOldPassword || Managers.SecurityManager.isAdmin(userContext)} required={requireOldPassword && !Managers.SecurityManager.isAdmin(userContext)}/>
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
                      multiSelect
                      options={accountOptions}
                      required
                      disabled={passwordChangeType === ALL_ONLY && !Managers.SecurityManager.isAdmin(userContext)}/>

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

PasswordAccounts.propTypes = {
  passwordChangeType: PropTypes.oneOf([DISABLED, ALL_ONLY, CUSTOM]),
  requireOldPassword: PropTypes.bool,
  userContext: PropTypes.object
};
PasswordAccounts.defaultProps = {
  passwordChangeType: ALL_ONLY,
  requireOldPassword: true,
  userContext: null
};

function select(state) {
  return {
    // passwordChangeType: settingManager.getValue(state, 'password.change') || DISABLED,
    // requireOldPassword: settingManager.getValue(state, 'password.change.require.old') === 'true',
    userContext: state.security.userContext
  };
}
export default connect(select)(PasswordAccounts);
