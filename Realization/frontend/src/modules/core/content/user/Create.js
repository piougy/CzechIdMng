import React from 'react';
import _ from 'lodash';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import Joi from 'joi';
import Immutable from 'immutable';
//
import * as Basic from 'app/components/basic';
import { AuthenticateService } from 'core/services';
import { IdentityManager } from 'core/redux';
import ApiOperationTypeEnum from 'core/enums/ApiOperationTypeEnum';

const identityManager = new IdentityManager();

class Profile extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.state = {
      showLoading: false,
      generatePassword: false,
      generatePasswordShowLoading: false
    };
  }

 initData() {
   // TODO: load data from redux store
   const { generatePassword } = this.state;
   this.transformData({
     generatePassword,
     password: '',
     passwordAgain: ''
   }, null, ApiOperationTypeEnum.GET);
   this.refs.username.focus();
   if (generatePassword) {
     this.generatePassword();
   }
 }

  componentDidMount() {
    this.selectNavigationItem('users');
    // TODO: load data from redux store
    this.initData();
  }

  componentDidUpdate() {
    // nothing
  }

  save(editContinue = 'CLOSE', event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const formData = this.refs.form.getData();
    if (formData.password !== formData.passwordAgain) {
      this.addMessage({
        key: 'form-error',
        message: this.i18n('content.user.profile.validation.passwordsNotMatch'),
        level: 'warning'
      });
      return;
    }

    this.setState({
      showLoading: true
    }, this.refs.form.processStarted());

    const result = _.merge({}, formData, {
      password: btoa(formData.password) // base64
    });
    delete result.passwordAgain;
    delete result.generatePassword;
    // TODO: transform to redux
    identityManager.getService().create(result)
    .then(json => {
      this.setState({
        showLoading: false
      }, () => {
        this.refs.form.processEnded();
        this.addMessage({
          message: this.i18n('content.user.create.message.success', { username: json.username})
        });
        switch (editContinue) {
          case 'EDIT': {
            this.context.router.replace(`user/${json.username}/profile`);
            break;
          }
          case 'NEW': {
            this.initData();
            this.context.router.replace(`user/new`);
            break;
          }
          default : {
            this.context.router.push(`users`);
          }
        }
      });
    }).catch(error => {
      this.setState({
        showLoading: false
      }, () => {
        this.refs.form.processEnded();
        this.transformData(null, error, ApiOperationTypeEnum.UPDATE);
      });
    });
  }

  transformData(json, error, operationType) {
    let result;
    if (json) {
      result = _.merge({}, json, { email: '' });
    }
    this.refs.form.setData(result, error, operationType);
  }

  setNewPassword(password) {
    const formData = this.refs.form.getData();
    _.merge(formData, {
      password,
      passwordAgain: password
    });
    this.refs.form.setData(formData);
  }

  generatePassword(event = null) {
    const generate = event ? event.currentTarget.checked : true;
    this.setState({
      generatePassword: generate
    });
    if (!generate) {
      return;
    }
    /*
    // generate
    this.setState({
      generatePasswordShowLoading: true
    })
    identityManager.getService().generatePassword()
    .then(response => {
      return response.json();
    })
    .then(json => {
      if (!json.error) {
        this.setNewPassword(json.password);
        this.setState({
          generatePasswordShowLoading: false,
          generatePassword: true
        });
      } else {
        this.addError(json.error);
        this.setState({
          generatePasswordShowLoading: false,
          generatePassword: false
        });
      }
    })
    .catch(error => {
      this.addError(error);
      this.setState({
        generatePasswordShowLoading: false,
        generatePassword: false
      });
    });*/
    // TODO: rest service for password generate
    this.setNewPassword(AuthenticateService.generatePassword());
  }

  canEditMap() {
    let canEditMap = new Immutable.Map();
    canEditMap = canEditMap.set('isSaveEnabled', true);
    return canEditMap;
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
    const { showLoading, generatePassword, generatePasswordShowLoading } = this.state;

    return (
      <Basic.Row>
        <div className="col-lg-offset-1 col-lg-10">
          <Helmet title={this.i18n('content.user.create.title')} />
          <form onSubmit={this.save.bind(this, 'CLOSE')}>
            <Basic.Panel>
              <Basic.PanelHeader text={this.i18n('content.user.create.header')}/>

              <Basic.AbstractForm ref="form" className="form-horizontal">
                <div className="col-lg-7">
                  <Basic.TextField ref="username" label={this.i18n('content.user.profile.username')} required validation={Joi.string().min(3).max(30)}/>
                  <Basic.TextField ref="lastName" label={this.i18n('content.user.profile.lastName')} required/>
                  <Basic.TextField ref="firstName" label={this.i18n('content.user.profile.firstName')}/>
                  <Basic.TextField ref="titleBefore" label={this.i18n('entity.Identity.titleBefore')}/>
                  <Basic.TextField ref="titleAfter" label={this.i18n('entity.Identity.titleAfter')}/>
                  <Basic.TextField ref="email" label={this.i18n('content.user.profile.email.label')} placeholder={this.i18n('content.user.profile.email.placeholder')} validation={Joi.string().email()}/>
                  {/*
                  <Basic.SelectBox
                    ref="idmManager"
                    service={identityManager.getService()}
                    searchInFields={['lastName', 'name','email']}
                    label={this.i18n('entity.Identity.idmManager')}
                    readOnly={!canEditMap.get('idmManager')}
                  />
                  <Basic.SelectBox
                    ref="homeOrganisation"
                    service={organizationManager.getService()}
                    searchInFields={['name']}
                    label={this.i18n('entity.Identity.homeOrganisation')}
                    required
                  />
                  */}
                  <Basic.Checkbox ref="disabled" label={this.i18n('entity.Identity.disabled')}/>
                  <Basic.TextArea ref="description" label={this.i18n('content.user.profile.description.label')} placeholder={this.i18n('content.user.profile.description.placeholder')} rows={4} />
                </div>
                <div className="col-lg-5">
                  <Basic.Checkbox ref="generatePassword" label={this.i18n('content.user.create.button.generate')} onChange={this.generatePassword.bind(this)}/>
                  <Basic.TextField
                    type={generatePassword || generatePasswordShowLoading ? 'text' : 'password'}
                    ref="password"
                    label={this.i18n('entity.Identity.password')}
                    required={!generatePassword}
                    readOnly={generatePassword || generatePasswordShowLoading}
                    validate={this._validatePassword.bind(this, 'passwordAgain', true)}/>
                  <Basic.TextField
                    type={generatePassword || generatePasswordShowLoading ? 'text' : 'password'}
                    ref="passwordAgain"
                    label={this.i18n('entity.Identity.passwordAgain')}
                    required={!generatePassword}
                    readOnly={generatePassword || generatePasswordShowLoading}
                    validate={this._validatePassword.bind(this, 'password', false)}/>
                </div>
              </Basic.AbstractForm>

              <Basic.PanelFooter>
                <Basic.Button type="button" level="link" onClick={this.context.router.goBack} showLoading={showLoading}>{this.i18n('button.back')}</Basic.Button>

                <Basic.SplitButton level="success" title={this.i18n('button.create')} onClick={this.save.bind(this, 'CLOSE')} showLoading={showLoading} pullRight dropup>
                  <Basic.MenuItem eventKey="1" onClick={this.save.bind(this, 'EDIT')}>{this.i18n('button.createContinue')}</Basic.MenuItem>
                  <Basic.MenuItem eventKey="1" onClick={this.save.bind(this, 'NEW')}>{this.i18n('button.createNew')}</Basic.MenuItem>
                </Basic.SplitButton>

              </Basic.PanelFooter>
            </Basic.Panel>
            {/* onEnter action */}
            <input type="submit" className="hidden"/>
          </form>
        </div>
      </Basic.Row>
    );
  }
}

Profile.propTypes = {
  userContext: React.PropTypes.object
};
Profile.defaultProps = {
  userContext: null
};

function select(state) {
  return {
    userContext: state.security.userContext
  };
}

export default connect(select)(Profile);
