import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';

import { Basic, Advanced } from 'czechidm-core';

import { PasswordResetService } from '../services';

const pwdResetService = new PasswordResetService();

/**
 * This component realizes reset of users forgoten password
 *
 * @author Peter Sourek
 */
class PasswordReset extends Basic.AbstractContent {
  constructor(props, context) {
    super(props, context);
    this.state = {
      showLoading: false
    };
  }

  getContentKey() {
    return 'pwdreset:content.password-reset';
  }

  componentDidMount() {
    this.selectNavigationItem('pwdreset');
  }

  back() {
    this.context.router.push('/');
  }

  handleSubmit(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid() || !this.refs.recaptcha.validate()) {
      return;
    }

    this.setState({
      showLoading: true
    });

    this.context.store.dispatch(
      pwdResetService.requestReset(
        {
          identifyingAttribute: this.refs.userIdentifier.getValue()
        },
        this._afterSave.bind(this)
      )
    );
  }

  _afterSave(json, error) {
    if (error) {
      this.setState(
        {
          showLoading: false
        },
        this.refs.form.processEnded()
      );
      this.addError(error);
    } else {
      this.addMessage({ message: this.i18n('pwdreset:passwordResetCreated') });
      this.context.router.push('/');
    }
  }

  render() {
    const { showLoading } = this.state;

    return (
      <div>
        <Helmet title={this.i18n('pwdreset:title')} />
        <div className="row">
          <div className="col-sm-offset-4 col-sm-4">
            <form onSubmit={this.handleSubmit.bind(this)}>
              <Basic.Panel showLoading={showLoading} >
                <Basic.PanelHeader text={this.i18n('pwdreset:title')} />
                <Basic.AbstractForm ref="form" className="panel-body">
                  <Basic.TextField
                    ref="userIdentifier"
                    label={this.i18n('core:entity.Identity.username')}
                  />
                  <Advanced.Recaptcha ref="recaptcha" required />
                </Basic.AbstractForm>
                <Basic.PanelFooter>
                  <Basic.Button level="link" onClick={this.back.bind(this)}>
                    {this.i18n('core:button.back')}
                  </Basic.Button>
                  <Basic.Button type="submit" level="success">
                    {this.i18n('pwdreset:button.reset-password')}
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

PasswordReset.propTypes = {};
PasswordReset.defaultProps = {
  showLogin: false
};

function select() {
  return {};
}

export default connect(select)(PasswordReset);
