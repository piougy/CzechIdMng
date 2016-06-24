'use strict';

import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../../components/basic';
import { AdvancedTable, AdvancedColumn, AdvancedColumnLink } from '../../../components/advanced';
import { IdentityManager } from '../../../modules/core/redux/data';
//
import help from './PasswordReset_cs.md';

const identityManager = new IdentityManager();

class PasswordReset extends Basic.AbstractContent {

  constructor(props, context) {
     super(props, context);
     this.state = {
       hasToken: false,
       showLoading: false
     }
  }

  componentDidMount(){
    this.selectNavigationItem('password-reset');
    this.refs.formToken.setData({});
    this.refs.username.focus();
  }

  getContentKey() {
    return 'content.password.reset';
  }

  generateResetToken(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.formToken.isFormValid()) {
      return;
    }
    this.setState({
      showLoading: true
    });

    identityManager.getService().generatePasswordResetToken(this.refs.username.getValue())
    .then(response => {
      this.setState({
        showLoading: false
      });
      if (response.status === 204) {
        return {};
      }
      return response.json();
    })
    .then(json => {
      if (!json.error) {
        this.addMessage({ message: this.i18n('message.generateResetToken.success') });
        this.hasToken();
      } else {
        this.addError(json.error);
      }
    })
    .catch(error => {
      this.addError(error);
    });
  }

  hasToken() {
    this.setState({
      hasToken: true
    }, () => {
      this.refs.formReset.setData({});
      this.refs.token.focus();
    });
  }

  cancel() {
    this.context.router.push('/login');
  }

  passwordReset(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.formReset.isFormValid()) {
      return;
    }
    this.setState({
      showLoading: true
    });
    identityManager.getService().paswordResetByToken(
      this.refs.token.getValue(),
      this.refs.password.getValue()
    )
    .then(response => {
      this.setState({
        showLoading: false
      });
      if (response.status === 204) {
        return {};
      }
      return response.json();
    })
    .then(json => {
      if (!json.error) {
        this.addMessage({ title: this.i18n('message.passwordReset.success.title'), message: this.i18n('message.passwordReset.success.message') });
        this.cancel();
      } else {
        this.addError(json.error);
      }
    })
    .catch(error => {
      this.addError(error);
    });
  }

  _validatePassword(property, onlyValidate, value, result) {
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
    const { showLoading, hasToken } = this.state;

    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <div className="row">
          <div className="col-sm-offset-4 col-sm-4">
            <form onSubmit={this.generateResetToken.bind(this)}>
              <Basic.Panel rendered={!hasToken} showLoading={showLoading}>
                <Basic.PanelHeader text={this.i18n('header')} help={help}/>

                <Basic.AbstractForm ref="formToken" className="panel-body">

                  <Basic.Alert text={this.i18n('message.generateResetToken.info')} className="no-margin"/>

                  <Basic.TextField
                    ref="username"
                    placeholder={this.i18n('username')}
                    required
                    componentSpan="col-sm-12"/>
                </Basic.AbstractForm>

                <Basic.PanelFooter>
                  <Basic.Button level="link" onClick={this.cancel.bind(this)}>
                    {this.i18n('button.cancel')}
                  </Basic.Button>
                  <Basic.Button level="default" onClick={this.hasToken.bind(this)}>
                    {this.i18n('button.haveToken')}
                  </Basic.Button>
                  {' '}
                  <Basic.Button type="submit" level="success">
                    {this.i18n('button.sendToken')}
                  </Basic.Button>
                </Basic.PanelFooter>
              </Basic.Panel>
            </form>

            <form onSubmit={this.passwordReset.bind(this)}>
              <Basic.Panel rendered={hasToken} showLoading={showLoading}>
                <Basic.PanelHeader text={this.i18n('header')}/>

                <Basic.AbstractForm ref="formReset" className="panel-body">

                  <Basic.Alert text={this.i18n('message.passwordReset.info')} className="no-margin"/>

                  <Basic.TextField
                    ref="token"
                    label={this.i18n('token.label')}
                    placeholder={this.i18n('token.placeholder')}
                    required
                    labelSpan="col-md-3"
                    componentSpan="col-md-9"
                  />
                  <Basic.TextField
                    type={'password'}
                    ref="password"
                    label={this.i18n('password')}
                    placeholder={this.i18n('password')}
                    required
                    labelSpan="col-md-3"
                    componentSpan="col-md-9"
                    validate={this._validatePassword.bind(this, 'passwordAgain', true)}/>
                  <Basic.TextField
                    type={'password'}
                    ref="passwordAgain"
                    label={this.i18n('passwordAgain.label')}
                    placeholder={this.i18n('passwordAgain.placeholder')}
                    required
                    labelSpan="col-md-3"
                    componentSpan="col-md-9"
                    validate={this._validatePassword.bind(this, 'password', false)}/>
                </Basic.AbstractForm>


                <Basic.PanelFooter>
                  <Basic.Button level="link" onClick={this.cancel.bind(this)}>
                    {this.i18n('button.cancel')}
                  </Basic.Button>
                  <Basic.Button type="submit" level="success">
                    {this.i18n('button.passwordReset')}
                  </Basic.Button>
                </Basic.PanelFooter>
              </Basic.Panel>
            </form>
          </div>
        </div>
      </div>
    );
  }
}

PasswordReset.propTypes = {
}
PasswordReset.defaultProps = {
}

function select(state) {
  return {
  }
}

export default connect(select)(PasswordReset)
