'use strict';

import React, { Component, PropTypes } from 'react';
import { Link }  from 'react-router';
import Joi from 'joi';
import merge from 'object-assign';
import { connect } from 'react-redux';
//
import { Confirm, Loading, ProgressBar, AbstractForm, Alert, Modal, Toolbar, Label, Icon, Button, AbstractContent, BasicForm, EnumSelectBox, SelectBox, Checkbox, TextArea, TextField, Panel, PanelHeader, PanelBody, Row, ContentHeader } from '../../../components/basic';
import { AuthenticateService, ConfigService } from '../../../modules/core/services';
import { CrtIdentityManager, CrtCertificateManager, CrtCertificateTaskManager } from '../redux/data';
import { AttachmentManager } from '../../../redux';
import { SecurityManager, FormManager, IdentityManager } from '../../../modules/core/redux';
import ApiOperationTypeEnum from '../../../modules/core/enums/ApiOperationTypeEnum';
import CrtIdentityRoleStateEnum from '../enums/CrtIdentityRoleStateEnum';
import CertificateTaskStateEnum from '../enums/CertificateTaskStateEnum';
import CertificateStateEnum from '../enums/CertificateStateEnum';
import CertificateTypeEnum from '../enums/CertificateTypeEnum';
import CertificateTaskTypeEnum from '../enums/CertificateTaskTypeEnum';
import * as Advanced from '../../../components/advanced';
import certificatesHelp from './CrtIdentityDetail_cs.md';

let crtIdentityManager = new CrtIdentityManager();
let identityManager = new IdentityManager();
let crtCertificateManager = new CrtCertificateManager();
let crtCertificateTaskManager = new CrtCertificateTaskManager();
let attachmentManager = new AttachmentManager();
let formManager = new FormManager();
let maxRefreshCount = 4;
let configService = new ConfigService();

class CrtIdentityDetail extends AbstractContent {

  constructor(props) {
    super(props);
    this.save = this.save.bind(this);
    this.transformData =  this.transformData.bind(this);
    this._initButton =  this._initButton.bind(this);
    this.state = {
      authenticateCertType: {state : CrtIdentityRoleStateEnum.ASK},
      signCertType: {state : CrtIdentityRoleStateEnum.ASK},
      encryptCertType: {state : CrtIdentityRoleStateEnum.ASK},
      vpnCertType: {state : CrtIdentityRoleStateEnum.ASK},
      progressBar: { showProcessing:false, max: maxRefreshCount, counter:0, label:'' },
      showModal: false
    };
  }

  getContentKey() {
    return 'crt:content.CrtIdentityDetail';
  }

  componentDidMount() {
    this.selectSidebarItem('certificates-info');
    const { userID } = this.props.params;
    let { query } = this.props.location;
    this.context.store.dispatch(crtIdentityManager.fetchEntity(userID, `ui-load-crt-identity-${userID}`, () => {
      // if user has a right to generate certificate, then modal will be shown
      if (this.state.showModal && !this._haveSomeRightForCrt(this._getCertTypeOptions())) {
        this._closeModal();
      }
    }));
    this.context.store.dispatch(identityManager.fetchEntityIfNeeded(userID, `ui-load-identity-${userID}`));
    if (query && query.open === '1') {
      this._addCertificate();
    }
  }

  componentDidUpdate() {
    this.transformData(this.props.data, null, ApiOperationTypeEnum.GET);
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.data){
      this.setState({
        authenticateCertType: {
          state : (CrtIdentityRoleStateEnum[nextProps.data.authenticateCertTypeState] ?  CrtIdentityRoleStateEnum[nextProps.data.authenticateCertTypeState] :CrtIdentityRoleStateEnum.ASK)
        },
        signCertType: {
          state : (CrtIdentityRoleStateEnum[nextProps.data.signCertTypeState] ? CrtIdentityRoleStateEnum[nextProps.data.signCertTypeState]:CrtIdentityRoleStateEnum.ASK)
        },
        encryptCertType: {
          state : (CrtIdentityRoleStateEnum[nextProps.data.encryptCertTypeState] ? CrtIdentityRoleStateEnum[nextProps.data.encryptCertTypeState]:CrtIdentityRoleStateEnum.ASK)
        },
        vpnCertType: {
          state : (CrtIdentityRoleStateEnum[nextProps.data.vpnCertTypeState] ? CrtIdentityRoleStateEnum[nextProps.data.vpnCertTypeState]:CrtIdentityRoleStateEnum.ASK)}
        }
      );
    }
  }

  validateAndSave() {
    let form = this.refs.identityCertForm;
    if (!form.isFormValid()){
      return;
    }
    form.processStarted();
    this.save(form.getData());
  }

  save(json) {
    const { userID } = this.props.params;
    let result = merge({},json);
    merge(result, {authenticateCertTypeState:CrtIdentityRoleStateEnum.findKeyBySymbol(this.state.authenticateCertType.state)});
    merge(result, {signCertTypeState:CrtIdentityRoleStateEnum.findKeyBySymbol(this.state.signCertType.state)});
    merge(result, {encryptCertTypeState:CrtIdentityRoleStateEnum.findKeyBySymbol(this.state.encryptCertType.state)});
    merge(result, {vpnCertTypeState:CrtIdentityRoleStateEnum.findKeyBySymbol(this.state.vpnCertType.state)});

    let dataFunc = this.transformData;
    crtIdentityManager.getService().updateById(userID, result).then(response => {
      return response.json();
    }).then(json => {
      if (json){
        if (!json.error) {
          // TODO: only signCertTypeState is supported now
          if (json.signCertTypeState === CrtIdentityRoleStateEnum.findKeyBySymbol(CrtIdentityRoleStateEnum.APPROVED)) {
            this.addMessage({
              title: this.i18n('request.message.added.title'),
              message: this.i18n('request.message.added.message', { certificateType: CertificateTypeEnum.getNiceLabel(CertificateTypeEnum.findKeyBySymbol(CertificateTypeEnum.SIGNING)) })
            });
          } else {
            this.addMessage({
              title: this.i18n('request.message.requested.title'),
              message: this.i18n('request.message.requested.message', { certificateType: CertificateTypeEnum.getNiceLabel(CertificateTypeEnum.findKeyBySymbol(CertificateTypeEnum.SIGNING)) })
            });
          }
          this.context.store.dispatch(crtIdentityManager.fetchEntities({'filter':{'filters': [{'field':'name', 'value':userID}]}}));
        } else {
          dataFunc(null, json.error, ApiOperationTypeEnum.UPDATE);
        }
      }
    }).catch(ex => {
      dataFunc(null, ex, ApiOperationTypeEnum.UPDATE);
    })
  }

  transformData(json, error, operationType){
    let result;
    if (json){
      result =  merge({},json);
    }
    this.refs.identityCertForm.setData(result, error, operationType);
    //cancle edit form
    this.context.store.dispatch(formManager.cancelForm('UserRoles'));
  }

  _roleRequestButton(key){
    this.setState({[key]:{state : CrtIdentityRoleStateEnum.WILL_ASK}}, this.validateAndSave);
  }

  _initButton(key, rendered = true, enabled = true, title = null) {
    if (!rendered) {
      return null;
    }
    let data = this.props.data;
    let hasRole = false;
    let state;
    let hiddenBtn = false;

    if (data){
      hasRole = data[key];
      state = data[key+'State'];
    }
    if (hasRole === true || CrtIdentityRoleStateEnum.WAIT_TO_APPROVE === this.state[key].state){
      hiddenBtn = true;
    }
    let hiddenLabel = hiddenBtn && hasRole;

    let btnLevel = 'success';
    if (CrtIdentityRoleStateEnum.WILL_ASK === this.state[key].state){
      btnLevel = 'danger';
    }
    let btnLabel = this.i18n('request_btn.'+(CrtIdentityRoleStateEnum.findKeyBySymbol(this.state[key].state)));
    let stateLabel = this.i18n('current_state') + ' ' + CrtIdentityRoleStateEnum.getNiceLabel(CrtIdentityRoleStateEnum.findKeyBySymbol(this.state[key].state));

    let btn = (
      <Button title={title || stateLabel} hidden={hiddenBtn} level={btnLevel} className="btn-xs" disabled={!enabled} onClick={this._roleRequestButton.bind(this, key)}>
        {btnLabel}
      </Button>
    );

    let label = (<Label level="warning" text = {CrtIdentityRoleStateEnum.getNiceLabel(CrtIdentityRoleStateEnum.findKeyBySymbol(this.state[key].state))} className="label-form"/>);

    let result = hiddenBtn === true ? (hiddenLabel === true ? <div/> :label) : btn;

    return result;

  }

  downloadHref(rowIndex, data, property, label) {
    if (data[rowIndex][property]){
      return (
        <a
          key={`crt-${property}-download-${data[rowIndex]['id']}`}
          href={crtCertificateManager.getService().getDownloadUrl(data[rowIndex]['id'], property + 'Id')}
          title="Download">{label}
        </a>
      );
    } else {
      return null;
    }
  }

  _downloadCell({rowIndex, data, property, ...props}) {
    const { userID } = this.props.params;
    let hrefPfx = this.downloadHref(rowIndex, data, 'certPfx', this.i18n('crt.certPfx'));
    let hrefPem = this.downloadHref(rowIndex, data, 'certPem', this.i18n('crt.certPem'));
    let result = [];
    if (SecurityManager.equalsAuthenticated(userID)){
      result.push(hrefPfx);
      result.push(' ')
    }
    result.push(hrefPem);

    return <div>{result}</div>
  }

  _actionsTaskCell({rowIndex, data, property, ...props}) {
    if (data[rowIndex]['state'] === CertificateTaskStateEnum.findKeyBySymbol(CertificateTaskStateEnum.IN_PROGRESS)){
      let result = [];
      let haveRight = this._haveRightForRevocateCrt();
      let title = this.i18n('task.taskRefresh');
      if (!haveRight){
        title = this.i18n('task.notRightForRefresh');
      }
      result.push(
        <Button
          key={`cert-task-btn-refresh-${data[rowIndex]['id']}`} type="button" level="primary" disabled={!haveRight} title={title} titlePlacement="bottom"
          onClick={this._onTaskRefresh.bind(this, data[rowIndex])} className="btn-xs" aria-label="Left Align">
          <Icon icon="refresh"/>
        </Button>
      );
      return <div>{result}</div>
    }
  }

  _actionsCertCell({rowIndex, data, property, ...props}) {
    if (data[rowIndex]['state'] === CertificateStateEnum.findKeyBySymbol(CertificateStateEnum.VALID)){
      const { userID } = this.props.params;
      let result = [];
      let haveRevocationRight = this._haveRightForRevocateCrt();
      result.push(
        <Button
          key={`cert-btn-download-${data[rowIndex]['id']}`}
          type="button" level="success" disabled={!SecurityManager.equalsAuthenticated(userID)}
          title={haveRevocationRight ? this.i18n('crt.downloadPfx') : this.i18n('crt.notRightForDownloadPfx')} titlePlacement="bottom"
          onClick={this._downloadPfx.bind(this, data[rowIndex]['id'])}
          className="btn-xs">
          <Icon icon="download-alt"/>
        </Button>
      );
      result.push(' ');
      result.push(
        <Button
          key={`cert-btn-revoke-${data[rowIndex]['id']}`}
          type="button" level="danger" disabled={!haveRevocationRight}
          title={haveRevocationRight ? this.i18n('crt.revocationBtn') : this.i18n('crt.notRightForRevocation')} titlePlacement="bottom"
          onClick={this._revocateCrt.bind(this, data[rowIndex])} className="btn-xs">
          <Icon icon="ban-circle"/>
        </Button>
      );
      return <span>{result}</span>
    }
  }

  _downloadPfx(crtId){
    window.location.href= crtCertificateManager.getService().getDownloadUrl(crtId, 'certPfxId');
  }

  /**
  * Action for certificate task refresh
  * @param  {json} task
  */
  _onTaskRefresh(task){
    //Load pregress bar info
    let progressBar = this.state.progressBar;
    let progressBarRuning = progressBar == null ? false : progressBar.showProcessing;
    //show loading
    this.setState({tables_showLoading:true});
    //call refreshProcessState api
    crtCertificateTaskManager.getService().refreshProcessState(task['id']).then(response => {
      return response.json();
    }).then(json => {

      const { userID } = this.props.params;
      //hide loading
      this.setState({tables_showLoading:false});

      if (json){
        if (!json.error){
          if (!progressBarRuning){
            //refresh entities in table
            this._refreshTables();
          }

          if (json.state === CertificateTaskStateEnum.findKeyBySymbol(CertificateTaskStateEnum.IN_PROGRESS)){
            if (progressBarRuning){
              //if we not exceeded the max number of trials, then we try refresh again
              if (progressBar.max > progressBar.counter){
                progressBar.counter ++;
                this.setState({progressBar:progressBar},this._onTaskRefresh.bind(this, task))
              }else {
                //Max number of trails exceeded ... show warning
                this.addMessage({message:this.i18n('task.refreshInProgress'), level: 'warning'});
                this._refreshTables();
                this._closeModal();
              }
            }else {
              this.addMessage({message:this.i18n('task.refreshInProgress'), level: 'warning'});
            }
          } else if (json.state === CertificateTaskStateEnum.findKeyBySymbol(CertificateTaskStateEnum.ERROR)) {
            this.addMessage({message:this.i18n('task.createTaskNotSuccess', json.description), level: 'error'});
          } else {
            if (progressBarRuning){
              this._refreshTables();
              this._closeModal();
            }
            this.addMessage({message:this.i18n('task.refreshSuccess', { type: CertificateTypeEnum.getNiceLabel(task['type']) }), level: 'success'});
          }
        }else {
          this.addError(json.error);
        }
      }
    }).catch(ex => {
      //hide loading
      this.setState({tables_showLoading:false});
      this.addError(ex);
    })
  }

  _revocateCrt(crt){
    let confirmPromise = this.refs.confirm.show(this.i18n('crt.revocationConfirm'));
    confirmPromise.then(result => {
      //show loading
      this.setState({tables_showLoading:true});
      //call revocation api
      crtCertificateManager.getService().revocation(crt['id']).then(response => {
        return response.json();
      }).then(json => {

        const { userID } = this.props.params;
        //hide loading
        this.setState({tables_showLoading:false});

        if (json){
          //refresh entities in table
          this._refreshTables();
          if (!json.error){

            if (json.state === CertificateTaskStateEnum.findKeyBySymbol(CertificateTaskStateEnum.IN_PROGRESS)){
              this.addMessage({message:this.i18n('task.refreshInProgress'), level: 'warning'});
            } else if (json.state === CertificateTaskStateEnum.findKeyBySymbol(CertificateTaskStateEnum.ERROR)) {
              this.addMessage({message:this.i18n('task.createTaskNotSuccess', json.description), level: 'error'});
            } else {
              this.addMessage({message:this.i18n('task.revocateSuccess', { type: CertificateTypeEnum.getNiceLabel(crt['type']) }), level: 'success'});
            }
          }else {
            this.addError(json.error);
          }
        }
      }).catch(ex => {
        //hide loading
        this.setState({tables_showLoading:false});
        this.addError(ex);
      })
    }, function(err) {
      return;
    });
  }

  _refreshTables(){
    //refresh entities in table
    this.refs.certificatesTaskTable.getWrappedInstance().reload();
    this.refs.certificatesTable.getWrappedInstance().reload();
  }

  _addCertificate(){
    this.setState({showModal:true}, function () {
      //Default selected crt type is signing (is first in options array)
      this.refs['addCertificateForm'].setData({'crtTypeSelect':this._getCertTypeOptions()[0].value});
    }.bind(this));
  }

  _addCertificateFormDisabled(disabled){
    this.refs['addCertificateForm'].setState({showLoading:disabled});
  }

  _approveCertificate(){
    if (this.refs['addCertificateForm'].isFormValid()){
      let crtFormResult = this.refs['addCertificateForm'].getData();
      const { userID } = this.props.params;
      //We check same valid certificates on exist
      const sameValidCrtFilter = {filter: { operation: 'AND', filters: [{
        field: 'owner.name',
        value: userID,
      },{
        field: 'type',
        value: crtFormResult.crtTypeSelect
      },{
        field: 'state',
        value: 'VALID'
      }
    ]}, range : { size: 10 }};
    crtCertificateManager.getService().search(sameValidCrtFilter).then(result =>{
      result.json().then(json => {
        if (json.total === 0){
          //Same valid certificate not exist
          this._callCreateCrtTask(userID, crtFormResult);
        }else {
          //Same valid certificates exists. We will ask user if they should be revoked
          this.refs.confirm.show(this.i18n('crt.sameCrtExistRevoked'),this.i18n('crt.sameCrtExist')).then(result => {
            this._callCreateCrtTask(userID, crtFormResult);
          }, function(err) {
            return;
          });
        }
      });
    });
  }
}

_callCreateCrtTask(userID, crtFormResult){

  let task = {
    type: crtFormResult.crtTypeSelect,
    owner: userID,
    password: crtFormResult.newPassword,
    taskType:'CREATE_CRT'
  };

  //shg and progress bar
  this._addCertificateFormDisabled(true);
  let progressBarDefault = {showProcessing: false, counter: 0, max: maxRefreshCount, label: ''}
  let progressBar = {showProcessing: true, counter: 1, max: maxRefreshCount, label: this.i18n('newCrtProgressBarTask')};
  this.setState({progressBar:progressBar});

  //call create task api
  crtCertificateTaskManager.getService().create(task).then(response => {
    return response.json();
  }).then(json => {

    if (json){
      if (!json.error){
        if (json.state === CertificateTaskStateEnum.findKeyBySymbol(CertificateTaskStateEnum.IN_PROGRESS)){
          let progressBarNext = {showProcessing: true, counter: 2, max: maxRefreshCount, label: this.i18n('newCrtProgressBarRefresh')};
          //we try refresh 4x
          this.setState({progressBar:progressBarNext}, function () {
            this._onTaskRefresh(json);
          }.bind(this));
        } else if (json.state === CertificateTaskStateEnum.findKeyBySymbol(CertificateTaskStateEnum.ERROR)) {
          this.addMessage({message:this.i18n('task.createTaskNotSuccess', {error:json.description}), level: 'error'});
          this.setState({progressBar:progressBarDefault});
          //hide loading
          this._addCertificateFormDisabled(false);
          this._refreshTables();
        } else {
          this.addMessage({message:this.i18n('task.createTaskSuccess'), level: 'success'});
          this.setState({showModal:false, progressBar:progressBarDefault});
          //hide loading
          this._addCertificateFormDisabled(false);
          this._refreshTables();
        }
      }else {
        this._addCertificateFormDisabled(false);
        this.setState({progressBar:progressBarDefault});
        this.addError(json.error);
        this._refreshTables();
      }
    }}).catch(ex => {
      //hide loading
      this._addCertificateFormDisabled(false);
      this.setState({progressBar:progressBarDefault});
      this.addError(ex);
      this._refreshTables();
    })
  }

  _closeModal(){
    let progressBarDefault = {showProcessing: false, counter: 0, max: maxRefreshCount, label: ''}
    this.setState({showModal:false, progressBar:progressBarDefault});
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

  _haveRightFroCreateCrt(certTypeEnum) {
    let userContext = AuthenticateService.getUserContext();
    //Check right for signing crt
    if (certTypeEnum === CertificateTypeEnum.SIGNING && this.props.data && this.props.data.signCertType === true){
      return true;
    }
    //Check right for encryption crt
    if (certTypeEnum === CertificateTypeEnum.ENCRYPTION && this.props.data && this.props.data.encryptCertType === true){
      return true;
    }
    return false;
  }

  _haveRightForRevocateCrt(){
    let userContext = AuthenticateService.getUserContext();
    //I am Admin
    if (SecurityManager.isAdmin(userContext)){
      return true;
    }
    //I am Garant
    if (this.props.identity && this.props.identity.idmManager === userContext.username){
      return true;
    }
    //Check right my self
    if (this.props.identity && this.props.identity.name === userContext.username){
      return true;
    }

    return false;
  }

  _getCertTypeOptions(){
    let  options = [];
    if (configService.getConfig('crtSigningEnable')){
      options.push(this._getCertTypeOptionsItem(CertificateTypeEnum.SIGNING));
    }
    if (configService.getConfig('crtEncrytpEnable')){
      options.push(this._getCertTypeOptionsItem(CertificateTypeEnum.ENCRYPTION));
    }
    return options;
  }
  _getCertTypeOptionsItem(certificateTypeEnum){
    let key = CertificateTypeEnum.findKeyBySymbol(certificateTypeEnum);
    let item = {value: key, niceLabel: CertificateTypeEnum.getNiceLabel(key),  disabled: !this._haveRightFroCreateCrt(certificateTypeEnum)};
    return item;
  }

  /**
  * Have user least one right for generate certificate
  * @param  {array} options
  * @return {boolean}
  */
  _haveSomeRightForCrt(options){
    let allDisabled = true;
    for (let o in options){
      if (options[o].disabled === false){
        allDisabled = false;
      }
    }
    if (allDisabled === true){
      return false;
    }
    return true;
  }

  render() {
    const { userID } = this.props.params;
    const { identity, _identityShowLoading, data, _dataShowLoading } = this.props;
    const isExterne = identity && identityManager.isExterne(identity);
    const forceFilters = [{
      field: 'owner.name',
      value: userID,
    }];

    let crtTypeOptions = this._getCertTypeOptions();
    let haveSomeRightForGenerate = this._haveSomeRightForCrt(crtTypeOptions);

    return (
      <div>
        <Confirm ref="confirm"/>
        <Loading className="global" showLoading={this.state.tables_showLoading}/>

        <ContentHeader help={certificatesHelp}>
          {this.i18n('title')}
        </ContentHeader>
        {
          !identity || !data
          ?
          null
          :
          !haveSomeRightForGenerate
          ?
          <Alert level="warning" className="no-margin" text={this.i18n('noRightsForCrtGenerate')}/>
          :
          null
        }

        <AbstractForm ref="identityCertForm" style={{ paddingTop: 0}}>
          <Row>
            <div className="col-lg-10">
              <div className="row" style={{ marginBottom: '15px' }}>
                <div className="col-sm-6">
                  <Advanced.IdentityInfo identity={identity} showLoading={!identity} className="no-margin"/>
                </div>
                <div className="col-sm-6">
                  <label>{this.i18n('permissions_for_certs')}:</label>
                  {configService.getConfig('crtSigningEnable') ?
                    <Row>
                      <div className="col-sm-6">
                        <Checkbox
                          ref="signCertType"
                          disabled
                          label={this.i18n('sign_cert_type')}
                          labelSpan=""
                          componentSpan="col-sm-12"/>
                      </div>
                      <div className="col-sm-6">
                        {this._initButton('signCertType', data)}
                      </div>
                    </Row>
                  : ''}
                  {configService.getConfig('crtEncrytpEnable') ?
                    <Row>
                      <div className="col-sm-6">
                        <Checkbox
                          ref="encryptCertType"
                          disabled
                          label={this.i18n('encrypt_cert_type')}
                          labelSpan=""
                          componentSpan="col-sm-12"/>
                      </div>
                      <div className="col-sm-6">
                        {
                          this._initButton('encryptCertType', data, !isExterne, isExterne ? this.i18n('message.certificateTypeIsDisabled.externe') : null)
                        }
                      </div>
                    </Row>
                  : ''}
                </div>
              </div>
            </div>
          </Row>
        </AbstractForm>

        <Panel ref="certificatesContainer">
          <PanelHeader text={this.i18n('crt.title')}/>
          <Toolbar viewportOffsetTop={0} container={this.refs.certificatesContainer} rendered={SecurityManager.equalsAuthenticated(userID)}>
            <div className="pull-right">
              <Button
                className="btn"
                level="success"
                onClick={this._addCertificate.bind(this)}
                disabled={!identity || identity.disabled || !haveSomeRightForGenerate}
                title={!identity || identity.disabled ? this.i18n('button.addCertificate.disabled') : ''}>
                <Icon icon="certificate"/>
                {' '+this.i18n('button.addCertificate.label')}
              </Button>
            </div>
            <div className="clearfix"></div>
          </Toolbar>
          <Advanced.Table
            ref="certificatesTable"
            manager={crtCertificateManager}
            pagination={true}
            forceSearchParameters={{ filter: {filters: forceFilters}}}
            rowClass={({rowIndex, data}) => { return data[rowIndex]['state'] === CertificateStateEnum.findKeyBySymbol(CertificateStateEnum.VALID) ? '' : 'disabled'}}>
            <Advanced.Column
              header={this.i18n('crt.type')}
              property="type" face="enum" enumClass={CertificateTypeEnum} sort={true}/>
            <Advanced.Column
              header={this.i18n('crt.publisher')}
              property="publisher" face="text" sort={true}/>
            <Advanced.Column
              header={this.i18n('crt.validFrom')}
              property="validFrom" face="date" sort={true}/>
            <Advanced.Column
              header={this.i18n('crt.validTill')}
              property="validTill" face="date" sort={true}/>
            <Advanced.Column
              header={this.i18n('crt.pem')}
              property="certPem" cell={this._downloadCell.bind(this)}/>
            <Advanced.Column
              header={this.i18n('crt.state')}
              property="state" face="enum" enumClass={CertificateStateEnum} sort={true}/>
            <Advanced.Column
              header={this.i18n('label.action')}
              property="actions"
              className="action"
              cell={this._actionsCertCell.bind(this)}
              sort={false}/>
          </Advanced.Table>
        </Panel>

        <Panel>
          <PanelHeader text={this.i18n('task.title')}/>
          <Advanced.Table
            ref="certificatesTaskTable"
            manager={crtCertificateTaskManager}
            pagination={true}
            forceSearchParameters={{ filter: {filters: forceFilters}}}>
            <Advanced.Column
              header={this.i18n('task.taskType')}
              property="taskType" sort={true} face="enum" enumClass={CertificateTaskTypeEnum}/>
            <Advanced.Column
              header={this.i18n('task.type')}
              property="type" sort={true} face="enum" enumClass={CertificateTypeEnum}/>
            <Advanced.Column
              header={this.i18n('task.submission')}
              property="submission" face="datetime" sort={true}/>
            <Advanced.Column
              header={this.i18n('task.state')}
              property="state" sort={true} face="enum" enumClass={CertificateTaskStateEnum}/>
            <Advanced.Column
              header={this.i18n('label.action')}
              property="actions"
              className="action"
              cell={this._actionsTaskCell.bind(this)}
              sort={false}/>
          </Advanced.Table>
        </Panel>

        <Modal show={this.state.showModal} onHide={this._closeModal.bind(this)} bsSize='lg' showLoading={!identity || !data || !haveSomeRightForGenerate} backdrop="static" keyboard={!this.state.progressBar.showProcessing}>
          <Modal.Header text={this.i18n('addCertificate')} help={certificatesHelp}/>
          <Modal.Body>
            {
              this.state.progressBar.showProcessing
              ?
              <ProgressBar min={0} max={this.state.progressBar.max} now={this.state.progressBar.counter} label={this.state.progressBar.label} active/>
              :
              !haveSomeRightForGenerate
              ?
              <Alert level="danger" className="last" text={this.i18n('noRightsForCrtGenerateModal')}/>
              :
              <span>
                <Alert level="info" text={this.i18n('newCrtMsg')}/>
                <Alert level="warning" text={this.i18n('newCrtUseDiffPassword')}/>
              </span>
            }

            <AbstractForm ref="addCertificateForm" className={haveSomeRightForGenerate ? '' : 'hidden'}>
              <EnumSelectBox
                ref="crtTypeSelect"
                componentSpan= 'col-sm-5'
                required
                disabled={crtTypeOptions && crtTypeOptions.length === 1}
                options={crtTypeOptions}
                label={this.i18n('certificateType')}/>
              <TextField type="password" ref="newPassword"
                componentSpan= 'col-sm-5'
                validate={this._validatePassword.bind(this, 'newPasswordAgain', true)}
                validation={Joi.string().min(8)}
                label={this.i18n('newCrtPass')}
                required/>
              <TextField type="password" ref="newPasswordAgain"
                componentSpan= 'col-sm-5'
                validate={this._validatePassword.bind(this, 'newPassword', false)}
                label={this.i18n('newCrtPassAgain')}
                required/>
              <Checkbox ref="policy"
                required
                label={this.i18n('newCrtIreadPolicy')}/>
            </AbstractForm>
          </Modal.Body>
          <Modal.Footer>
            <Button level="link" disabled={this.state.progressBar.showProcessing} onClick={this._closeModal.bind(this)}>{this.i18n('button.close')}</Button>
            <Button ref="createCrtBtn" disabled={this.state.progressBar.showProcessing} level="success" onClick={this._approveCertificate.bind(this)} rendered={haveSomeRightForGenerate}>{this.i18n('newCrtApprove')}</Button>
          </Modal.Footer>
        </Modal>
      </div>
    );
  }
}

function select(state, component) {
  const { userID } = component.params;
  return {
    data: crtIdentityManager.getEntity(state, userID),
    _dataShowLoading: crtIdentityManager.isShowLoading(state, `ui-load-crt-identity-${userID}`),
    identity: identityManager.getEntity(state, userID),
    _identityShowLoading: identityManager.isShowLoading(state, `ui-load-identity-${userID}`),
  }
}

export default connect(select)(CrtIdentityDetail);
