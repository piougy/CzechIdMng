'use strict';

import React, { Component, PropTypes } from 'react';
import _ from 'lodash';
import { connect } from 'react-redux';
import Joi from 'joi';
import Immutable from 'immutable';

import * as Basic from '../../../../components/basic';
import * as Advanced from '../../../../components/advanced';
import { IdentitySubordinateManager } from '../../../../redux';
import { SecurityManager, IdentityManager, OrganizationManager } from '../../../../modules/core/redux';
import ApiOperationTypeEnum from '../../../../modules/core/enums/ApiOperationTypeEnum';

const identityManager = new IdentityManager();
const organizationManager = new OrganizationManager();

class Profile extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.state = {
      showDeactivateModal: false,
      showDeactivateLoading: false,
      showLoading: false,
      deactivateLoading: false,
      deactivateCounter: 0,
      deactivateBreak: false,
      showLoadingIdentityTrimmed: false,
      setDataToForm: false
    }
  }

  getContentKey() {
    return 'content.user.profile';
  }

  componentDidMount() {
    this.selectNavigationItems(['user-profile','profile-personal']);
    const { userID } = this.props.params;
    //we set empty data (for hard validate all components)
    this.refs.form.setData({});
    this.context.store.dispatch(identityManager.fetchEntity(userID));
    this.setState({showLoading: true});
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.identity) {
      if (nextProps.identity._trimmed){
        this.setState({showLoadingIdentityTrimmed: true});
      } else {
        this.setState({showLoadingIdentityTrimmed: false});
      }
      if (nextProps.identity !== this.props.identity){
        //after receive new Identity we will hide showLoading on form
        this.setState({showLoading: false, setDataToForm: true});
      }
    }
  }

  componentDidUpdate(){
    if (this.props.identity && !this.props.identity._trimmed && this.state.setDataToForm){
      //We have to set data to form after is rendered
      this.transformData(this.props.identity, null, ApiOperationTypeEnum.GET);
    }
  }

  onSave(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const json = this.refs.form.getData();
    this.saveIdentity(json);
  }

  saveIdentity(json, deactive = false) {
    this.setState({
      showLoading: true,
      setDataToForm: false //Form will not be set new data (we are waiting to saved data)
    });
    const { userID } = this.props.params;
    let result = _.merge({}, json);

    identityManager.getService().patchById(userID, result)
    .then((json) => {
      this.context.store.dispatch(identityManager.fetchEntity(userID));
      if (!deactive) {
        this.addMessage({ level: 'success', key: 'form-success', message: this.i18n('messages.saved', { username: userID }) });
      } else {
        this.addMessage({ level: 'success', key: 'form-success', message: this.i18n('messages.deactivated', { username: userID }) });
      }
    }).catch(ex => {
      this.transformData(null, ex, ApiOperationTypeEnum.UPDATE);
      this.setState({
        showLoading: false
      });
    });
  }

  transformData(json, error, operationType){
    this.refs.form.setData(json, error, operationType);
  }

  closeDeactivateModal() {
    this.setState({ showDeactivateModal: false });
  }

  openDeactivateModal(event) {
    if (event) {
      event.preventDefault();
    }
    this.setState({
      showLoading: true,
      showDeactivateLoading: true
    });
    const { userID } = this.props.params;
    // check, if user has subordinates
    identityManager.getService().searchSubordinates(userID)
    .then(json => {
      if (!json.error){
        this.setState({
          showLoading: false,
          showDeactivateLoading: false,
          subordinatesCount: json.total,
          showDeactivateModal: true
        }, () => {
          if (json.total > 0) {
            this.refs.newIdmManager.validate();
          }
        });
      } else {
        this.setState({
          showLoading: false,
          showDeactivateLoading: false
        });
        this.addError(json.error);
      }
    }).catch(error => {
      this.setState({
        showLoading: false,
        showDeactivateLoading: false
      });
      this.addError(error);
    });
  }

  _validateNewIdmManager(userID, value, result) {
    if (result.error) {
      return result;
    }
    if (value.username === userID) {
      this.addMessage({
        level: 'warning',
        key: 'form-validation-newIdmManager',
        title: this.i18n('validation.newIdmManagerIsTheSame.title', { username: value.username }),
        message: this.i18n('validation.newIdmManagerIsTheSame.message')
      });
      return { error: { key: 'user_is_same'} }
    } else {
      this.hideMessage('form-validation-newIdmManager');
    }
    return result;
  }

  /**
   * Deactivate
   */
  onDeactivate() {
    const { subordinatesCount } = this.state;
    const { userID } = this.props.params;
    if (subordinatesCount > 0) {
      const newIdmManager = this.refs.newIdmManager.getValue();
      this.refs.newIdmManager.validate();
      if (!this.refs.newIdmManager.isValid()) {
        if (!newIdmManager) {
          this.addMessage({
            level: 'warning',
            key: 'form-validation-newIdmManager',
            title: this.i18n('validation.newIdmManagerIsRequired.title'),
            message: this.i18n('validation.newIdmManagerIsRequired.message')
          });
        }
        return;
      }
      //
      this.setState({
        deactivateLoading: true,
        deactivateCounter: 0,
        deactivateBreak: false
      });
      this.switchIdmManager(userID, newIdmManager);
    } else {
      this.deactivateCurrentIdentity();
    }
  }

  requestDeactivateBreak() {
    this.setState({
      deactivateBreak: true,
    });
  }

  receiveDeactivateBreak() {
    const { deactivateCounter } = this.state;
    this.setState({
      deactivateBreak: true,
      deactivateLoading: false
    }, () => {
      this.addMessage({
        level: 'warning',
        key: 'form-validation-newIdmManager',
        title: this.i18n('messages.deactivateBreak.title'),
        message: this.i18n('messages.deactivateBreak.message', { deactivateCounter: deactivateCounter })
      });
    });
    this.openDeactivateModal();
  }

  /**
   * Sets active to false on current identity (current form data) and submits form
   */
  deactivateCurrentIdentity() {
    const { userID } = this.props.params;
    this.setState({
      showLoading: true,
      showDeactivateLoading: false,
      showDeactivateModal: false,
      deactivateLoading: false,
      deactivateCounter: 0,
    });
    let formData = this.refs.form.getData();
    _.merge(formData, {
      disabled: true
    });
    this.saveIdentity(formData, true);
  }

  /**
   * Switch subordinates manager
   */
  switchIdmManager(previousIdmManager, newIdmManager) {
    if (!newIdmManager) {
      return;
    }
    // search and deactivate
    // console.log('switchIdmManager', previousIdmManager, newIdmManager);
    identityManager.getService().searchSubordinates(previousIdmManager)
    .then(json => {
      if (!json.error){
        if (!json.returned) {
          // all identity was switched to new manager ... we can disable
          this.addMessage({
            level: 'success',
            key: 'form-validation-newIdmManager',
            title: this.i18n('messages.switchedIdmManager.title'),
            message: this.i18n('messages.switchedIdmManager.message', { previousIdmManager: previousIdmManager, newIdmManager: newIdmManager })
          });
          this.deactivateCurrentIdentity();
        } else {
          let promises = [];
          json._embedded.forEach(identity => {
            promises.push(this.switchIdmManagerForSubordinate(identity, newIdmManager));
          });
          // next batch
          Promise.all(promises).then(jsons => {
            const { deactivateBreak } = this.state;
            if (!deactivateBreak) {
              this.switchIdmManager(previousIdmManager, newIdmManager);
            } else {
              this.receiveDeactivateBreak();
            }
          }).catch(error => {
            this.receiveDeactivateBreak();
            this.addError(error);
          });
        }
      } else {
        this.receiveDeactivateBreak();
        this.addError(json.error);
      }
    }).catch(error => {
      this.receiveDeactivateBreak();
      this.addError(error);
    });
  }

  /**
   * Switch manager to given identity
   */
  switchIdmManagerForSubordinate(identity, newIdmManager) {
    // console.log(' - switchIdmManagerForSubordinate', identity.name, newIdmManager);
    return identityManager.getService().switchManager(identity.username, newIdmManager)
    .then(response => {
      return response.json();
    })
    .then(json => {
      if (!json.error) {
        const { deactivateCounter } = this.state;
        this.setState({ deactivateCounter: deactivateCounter + 1 });
      } else {
        // vzhledem k asynchronite a opakovani deaktivace v cyklu, dokud naleznu nejakeho podrizeneho provedeme pouze zaznam do logu ...
        this.addErrorMessage({
          hidden: true,
          position: 'tr',
          level: 'warning',
          title: this.i18n('messages.switchIdmManagerFailed.title', { identity: identity })
        }, json.error);
        //this.requestDeactivateBreak();
      }
      return json;
    });
  }

  render() {
    const { userContext, identity } = this.props;
    const { subordinatesCount, deactivateLoading, deactivateCounter, showLoading, showDeactivateLoading,
       deactivateBreak, showDeactivateModal, showLoadingIdentityTrimmed } = this.state;
    const { userID } = this.props.params;
    const canEditMap = identityManager.canEditMap(userContext, identity);
    const deactiveDisabled = !userContext || userID === userContext.username || !canEditMap.get('isSaveEnabled');
    const identitySubordinateManager = new IdentitySubordinateManager(userID);

    let deactivateLabel = (<span style={{color: '#000'}}>{this.i18n('component.basic.ProgressBar.start')}</span>);
    if (deactivateCounter > 0) {
      deactivateLabel = this.i18n('component.basic.ProgressBar.processed') + ' %(now)s / %(max)s';
    }

    return (
      <div>
        <form onSubmit={this.onSave.bind(this)}>
          <Basic.Row>
            <Basic.Panel className="col-lg-7 no-border last" showLoading={showLoadingIdentityTrimmed || showLoading}>
              <Basic.PanelHeader text={this.i18n('header')}/>
              <Basic.AbstractForm ref="form" readOnly={!canEditMap.get('isSaveEnabled')}>
                <Basic.TextField ref="username" readOnly={true} label={this.i18n('content.user.profile.username')} required validation={Joi.string().min(3).max(30)}/>
                <Basic.TextField ref="lastName" label={this.i18n('content.user.profile.lastName')} required/>
                <Basic.TextField ref="firstName" label={this.i18n('content.user.profile.firstName')}/>
                <Basic.TextField ref="titleBefore" label={this.i18n('entity.Identity.titleBefore')}/>
                <Basic.TextField ref="titleAfter" label={this.i18n('entity.Identity.titleAfter')}/>
                <Basic.TextField ref="email" label={this.i18n('content.user.profile.email.label')} placeholder={this.i18n('email.placeholder')} hidden={false} validation={Joi.string().email()}/>
                <Basic.TextField
                  ref="phone"
                  label={this.i18n('content.user.profile.phone.label')}
                  placeholder={this.i18n('phone.placeholder')} />
                <Basic.TextArea
                  ref="description"
                  label={this.i18n('content.user.profile.description.label')}
                  placeholder={this.i18n('description.placeholder')}
                  rows={4}/>
                <Basic.Checkbox
                  ref="disabled"
                  label={this.i18n('entity.Identity.disabled')}
                  readOnly={deactiveDisabled || !identity}
                  title={deactiveDisabled ? this.i18n('messages.deactiveDisabled') : ''}>
                  <Basic.Button
                    level="danger"
                    className="btn-xs"
                    style={{ marginTop: '-3px', marginLeft: '5px' }}
                    rendered={false && !deactiveDisabled && identity && !identity.disabled}
                    onClick={this.openDeactivateModal.bind(this)}
                    disabled={showLoading}
                    showLoading={showDeactivateLoading}
                    showLoadingIcon={true}
                    showLoadingText={this.i18n('button.deactivatePrepare')}>
                    {this.i18n('button.deactivate')}
                  </Basic.Button>
                </Basic.Checkbox>
              </Basic.AbstractForm>

              <Basic.PanelFooter>
                <Basic.Button type="button" level="link" onClick={this.context.router.goBack} showLoading={showLoading}>{this.i18n('button.back')}</Basic.Button>
                <Basic.Button type="submit" level="success" showLoading={showLoading} rendered={canEditMap.get('isSaveEnabled')}>{this.i18n('button.save')}</Basic.Button>
              </Basic.PanelFooter>
            </Basic.Panel>
          </Basic.Row>
        </form>

        <Basic.Modal
          show={showDeactivateModal}
          onHide={this.closeDeactivateModal.bind(this)}
          bsSize={subordinatesCount ? 'large' : 'default'} backdrop="static" keyboard={!deactivateLoading}>
          <Basic.Modal.Header closeButton={!deactivateLoading}>
            <h1>{deactivateLoading ? this.i18n('deactivate.proceed', { username: userID }) : subordinatesCount ? this.i18n('deactivate.selectIdmManager') : this.i18n('deactivate.confirm.header')}</h1>
          </Basic.Modal.Header>
          <Basic.Modal.Body>
            {
              deactivateLoading
              ?
              <Basic.ProgressBar min={0} max={subordinatesCount} now={deactivateCounter} label={deactivateLabel} active style={{ marginBottom: 0}}/>
              :
              !subordinatesCount
              ?
              <Basic.Alert
                text={this.i18n('deactivate.confirm.message', { username: userID, escape: false })}
                className="last"/>
              :
              <div>
                <Basic.Alert
                  level="warning"
                  text={this.i18n('messages.subordinatesCount', { username: userID, subordinatesCount: subordinatesCount, escape: false })}
                />

                <div className="form-horizontal">
                  <Basic.SelectBox
                    ref="newIdmManager"
                    service={identityManager.getService()}
                    searchInFields={['lastName', 'name','email']}
                    placeholder={this.i18n('deactivate.form.newIdmManager')}
                    componentSpan="col-sm-12"
                    required
                    validate={this._validateNewIdmManager.bind(this, userID)}
                  />
                </div>

                <Advanced.Table
                  ref="table"
                  uiKey="deactivate_subordinate_table"
                  manager={identitySubordinateManager}
                  rowClass={({rowIndex, data}) => { return data[rowIndex]['disabled'] ? 'disabled' : ''}}>
                  <Advanced.Column property="name" width="20%" sort={true} face="text"/>
                  <Advanced.Column property="lastName" sort={true} face="text" />
                  <Advanced.Column property="firstName" width="10%" face="text" />
                  <Advanced.Column property="email" width="15%" face="text" sort={true}/>
                  <Advanced.Column property="disabled" face="bool" sort={true} width="100px"/>
                </Advanced.Table>
              </div>
            }
          </Basic.Modal.Body>
          <Basic.Modal.Footer>
            <Basic.Button level="link" onClick={this.closeDeactivateModal.bind(this)} rendered={!deactivateLoading}>{this.i18n('button.cancel')}</Basic.Button>
            <Basic.Button level="danger" onClick={this.onDeactivate.bind(this)} rendered={!deactivateLoading}>{this.i18n('button.confirm')}</Basic.Button>
            <Basic.Button
              level="link"
              onClick={this.requestDeactivateBreak.bind(this)}
              rendered={deactivateLoading}
              title={this.i18n('deactivate.button.break.title')}
              disabled={deactivateBreak}>
              {!deactivateBreak ? this.i18n('deactivate.button.break.label') : this.i18n('deactivate.button.break.proceed')}
            </Basic.Button>
          </Basic.Modal.Footer>
        </Basic.Modal>
      </div>
    );
  }
}

Profile.propTypes = {
  userContext: React.PropTypes.object
}
Profile.defaultProps = {
  userContext: null
}

function select(state, component) {
  const { userID } = component.params;
  return {
    identity: identityManager.getEntity(state, userID),
    userContext: state.security.userContext
  }
}

export default connect(select)(Profile);
