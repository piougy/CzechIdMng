import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import ComponentService from '../../services/ComponentService';
import * as Basic from '../../components/basic';
import { IdentityManager } from '../../redux';
import * as Utils from '../../utils';

const componentService = new ComponentService();
const identityManager = new IdentityManager();

const IDM_NAME = Utils.Config.getConfig('app.name', 'CzechIdM');
const RESOURCE_IDM = `0:${IDM_NAME}`;

const PASSWORD_DOES_NOT_MEET_POLICY = 'PASSWORD_DOES_NOT_MEET_POLICY';


/**
 * Password change component contains all components defined in component descriptor
 * with type password-change-component
 *
 * @author Ondřej Kopr
 */
class PasswordChangeForm extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  _initForm(permissions) {
    const { accountOptions } = this.props;
    if (this._canPasswordChange(permissions)) {
      this.setState({
        preload: false
      }, () => {
        this.refs.form.setData({
          accounts: accountOptions,
          oldPassword: ''
        });
        // focus old password
        this.refs.oldPassword.focus();
      });
    }
    this._preValidate(accountOptions);
  }

  /**
   * Return all components of password-change-component type
   */
  getComponents() {
    return componentService.getPasswordChangeComponent();
  }

  /**
   * Method return password change content defined by component descriptor
   * To every component will be add these prosp:
   * - userContext    - user context
   * - accountOptions - all available account options
   * - entityId       - entity id of user for that will be password changed
   */
  getContent() {
    const {
      userContext,
      accountOptions,
      entityId
    } = this.props;
    //
    const components = this.getComponents();
    const finalComponents = [];
    components.forEach(component => {
      const Component = component.component;
      finalComponents.push(
        <Basic.Col lg={component.col}>
          <Component
            userContext={userContext}
            accountOptions={accountOptions}
            entityId={entityId}/>
        </Basic.Col>);
    });
    return finalComponents;
  }

  /*
   * Method shows password rules before applying change of password
   */
  _preValidate(options) {
    const requestData = {
      accounts: []
    };

    options.forEach(resourceValue => {
      if (resourceValue.value === RESOURCE_IDM) {
        requestData.idm = true;
      } else {
        requestData.accounts.push(resourceValue.value);
      }
    });
    identityManager.preValidate(requestData)
    .then(response => {
      if (response.status === 204) {
        const error = undefined;
        this.setState({
          validationError: error,
          validationDefinition: true
        });

        throw error;
      }
      return response.json();
    })
    .then(json => {
      if (Utils.Response.hasError(json)) {
        const error = Utils.Response.getFirstError(json);
        this.setState({
          validationError: error,
          validationDefinition: true
        });

        throw error;
      }
      return json;
    })
    .catch(error => {
      if (!error) {
        return {};
      }
      if (error.statusEnum === PASSWORD_DOES_NOT_MEET_POLICY) {
        this.addErrorMessage({hidden: true}, error);
      } else {
        this.addError(error);
      }
    });
  }

  getContentKey() {
    return 'content.identity.passwordChange';
  }

  componentDidMount() {
    this.selectSidebarItem('profile-password');
  }

  render() {
    let components = this.getContent();
      //
      // if no one components exists show warning
    if (_.size(components) === 0) {
      components = (
          <Basic.Col lg={7}>
            <Basic.Alert level="warning" text={this.i18n('message.noContent')}/>
          </Basic.Col>
        );
    }
    //
    return (
      <div>
        <Helmet title={this.i18n('title')} />
          <Basic.Row>
            { components }
          </Basic.Row>
      </div>
    );
  }
}

PasswordChangeForm.propTypes = {
  userContext: PropTypes.object,
  accountOptions: PropTypes.object,
  entityId: PropTypes.string
};
PasswordChangeForm.defaultProps = {
  userContext: null
};

export default connect()(PasswordChangeForm);
