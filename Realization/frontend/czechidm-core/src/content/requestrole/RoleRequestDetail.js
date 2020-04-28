import PropTypes from 'prop-types';
import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import _ from 'lodash';
import moment from 'moment';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { RoleRequestManager, IdentityRoleManager, DataManager, ConfigurationManager } from '../../redux';
import RoleRequestStateEnum from '../../enums/RoleRequestStateEnum';
import RequestIdentityRoleTable from './RequestIdentityRoleTable';
import IncompatibleRoleWarning from '../role/IncompatibleRoleWarning';
//
const uiKey = 'role-request';
const uiKeyIncompatibleRoles = 'request-incompatible-roles-';
const roleRequestManager = new RoleRequestManager();
const identityRoleManager = new IdentityRoleManager();

/**
 * Detail of the role request
 *
 * @author Vít Švanda
 */
class RoleRequestDetail extends Advanced.AbstractTableContent {

  getUiKey() {
    return uiKey;
  }

  getContentKey() {
    return 'content.roleRequestDetail';
  }

  getNavigationKey() {
    return 'role-requests';
  }

  // @Deprecated - since V10 ... replaced by dynamic key in Route
  // UNSAFE_componentWillReceiveProps(nextProps) {
  //   const entityId = nextProps.entityId ? nextProps.entityId : nextProps.match.params.entityId;
  //   const entityIdCurrent = this.props.entityId ? this.props.entityId : this.props.match.params.entityId;
  //   if (entityId && entityId !== entityIdCurrent) {
  //     this._initComponent(nextProps);
  //   }
  // }

  // Did mount only call initComponent method
  componentDidMount() {
    super.componentDidMount();
    //
    this._initComponent(this.props);
    if (this.refs.description) {
      this.refs.description.focus();
    }
  }

  /**
   * Method for init component from didMount method and from willReceiveProps method
   * @param  {properties of component} props For didmount call is this.props for call from willReceiveProps is nextProps.
   */
  _initComponent(props) {
    const { entityId } = props;
    const _entityId = entityId || props.match.params.entityId;
    if (this._getIsNew(props)) {
      this.setState({
        showLoading: false,
        request: {
          applicant: props.location.query.applicantId,
          state: RoleRequestStateEnum.findKeyBySymbol(RoleRequestStateEnum.CONCEPT),
          requestedByType: 'MANUALLY'
        }
      });
    } else {
      this.context.store.dispatch(roleRequestManager.fetchEntity(_entityId));
      this.context.store.dispatch(roleRequestManager.fetchIncompatibleRoles(_entityId, `${ uiKeyIncompatibleRoles }${ _entityId }`));
    }
  }

  /**
   * Its used for reload the component iner date, eq. refresh roleRequest.
   * Reload is used as callback from inner components
   */
  reloadComponent() {
    this._initComponent(this.props);
  }

  /**
   * Saves give entity
   */
  save(startRequest, event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const formEntity = this.refs.form.getData();
    this.setState({showLoading: true});
    delete formEntity.conceptRoles;

    if (formEntity.id === undefined) {
      this.context.store.dispatch(roleRequestManager.createEntity(formEntity, `${uiKey}-detail`, (createdEntity, error) => {
        if (startRequest) {
          this.afterSaveAndStartRequest(createdEntity, error);
        } else {
          this.afterSave(createdEntity, error);
        }
      }));
    } else if (startRequest) {
      this.context.store.dispatch(roleRequestManager.updateEntity(formEntity, `${uiKey}-detail`, this.afterSaveAndStartRequest.bind(this)));
    } else {
      // => save only
      this.context.store.dispatch(roleRequestManager.updateEntity(formEntity, `${uiKey}-detail`, (createdEntity, error) => {
        this.afterSave(createdEntity, error, true);
      }));
    }
  }

  afterSave(entity, error, showMessage = false) {
    this.setState({
      showLoading: false
    }, () => {
      if (!error) {
        if (showMessage) {
          this.addMessage({ message: this.i18n('save.success') });
        }
        if (this._getIsNew()) {
          this.replaceUrl(entity.id);
        }
      } else {
        this.addError(error);
      }
    });
  }

  replaceUrl(requestId) {
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const formEntity = this.refs.form.getData();
    // If request detail from form hasn't ID (is new) and has filled descripton, then we need to make a
    // update of new request (for prevent lost of this description).
    if (this._getIsNew() && formEntity.description) {
      // First we need to get fresh request.
      this.context.store.dispatch(roleRequestManager.fetchEntity(requestId, null, (request) => {
        // Set description from form.
        request.description = formEntity.description;
        this.setState({showLoading: true}, () => {
          // => save only
          this.context.store.dispatch(roleRequestManager.updateEntity(request, `${uiKey}-detail`, () => {
            this.context.history.replace(`/role-requests/${requestId}/detail`);
          }));
        });
      }));
    } else {
      this.context.history.replace(`/role-requests/${requestId}/detail`);
    }
  }

  afterSaveAndStartRequest(entity, error) {
    this.setState({showLoading: false});
    if (!error) {
      this._startRequest(entity.id);
    } else {
      this.addError(error);
    }
  }

  closeDetail() {
    this.refs.form.processEnded();
  }

  _getIsNew(nextProps) {
    if ((nextProps && nextProps.location) || this.props.location) {
      const { query } = nextProps ? nextProps.location : this.props.location;
      return (query) ? query.new : null;
    }
    return false;
  }

  _startRequest(idRequest, event) {
    if (event) {
      event.preventDefault();
    }
    this.setState({
      showLoading: true
    }, () => {
      const promise = roleRequestManager.getService().startRequest(idRequest);
      promise.then((json) => {
        this.setState({
          showLoading: false
        }, () => {
          if (json && json.id) {
            this.context.store.dispatch(roleRequestManager.fetchEntity(json.id));
          }
          if (json.state === RoleRequestStateEnum.findKeyBySymbol(RoleRequestStateEnum.DUPLICATED)) {
            this.addMessage({ message: this.i18n('content.roleRequests.action.startRequest.duplicated',
              { created: moment(json._embedded.duplicatedToRequest.created).format(this.i18n('format.datetime'))}),
            level: 'warning'});
            return;
          }
          if (json.state === RoleRequestStateEnum.findKeyBySymbol(RoleRequestStateEnum.EXCEPTION)) {
            this.addMessage({ message: this.i18n('content.roleRequests.action.startRequest.exception'), level: 'warning' });
            return;
          }
          if (json.state === RoleRequestStateEnum.findKeyBySymbol(RoleRequestStateEnum.EXECUTED)) {
            this.addMessage({ message: this.i18n('content.roleRequests.action.startRequest.executed')});
            this.context.history.goBack();
            return;
          }
          this.addMessage({ message: this.i18n('content.roleRequests.action.startRequest.started'), level: 'info' });
          this.context.history.goBack();
        });
      }).catch(ex => {
        this.addError(ex);
        this.setState({
          showLoading: false
        });
      });
    });
  }

  _getWfProcessCell({ rowIndex, data}) {
    const entity = data[rowIndex];
    if (!entity || !entity.wfProcessId) {
      return '';
    }
    return (
      <Advanced.WorkflowProcessInfo entityIdentifier={entity.wfProcessId}/>
    );
  }

  _confirmIncompatibleRoles(incompatibleRoles) {
    if (!incompatibleRoles || incompatibleRoles.length === 0) {
      this.save(true);
    } else {
      this.refs['confirm-incompatible-role'].show(
        null,
        this.i18n(`confirm-incompatible-role.header`)
      ).then(() => {
        this.save(true);
      });
    }
  }

  _getApplicantAndImplementer(request) {
    return (
      <div>
        <Basic.LabelWrapper
          rendered={ request !== null && !_.isNil(request.applicant) }
          readOnly
          ref="applicant"
          label={this.i18n('entity.RoleRequest.applicant')}>
          <Advanced.IdentityInfo
            entityIdentifier={ request && request.applicant }
            showLoading={!request}/>
        </Basic.LabelWrapper>

        <Basic.LabelWrapper
          rendered={ request !== null && !_.isNil(request.creatorId) }
          readOnly
          ref="implementer"
          label={this.i18n('entity.RoleRequest.implementer')}>
          <Advanced.IdentityInfo
            face="popover"
            entityIdentifier={request && request.creatorId}
            showLoading={!request}/>
        </Basic.LabelWrapper>
      </div>
    );
  }

  _getIdentityId(props) {
    const { _request, location} = props;
    const applicantFromUrl = location && location.query ? location.query.applicantId : null;

    return _request ? _request.applicant : applicantFromUrl;
  }

  render() {
    const {
      _showLoading,
      _request,
      editableInStates,
      showRequestDetail,
      _permissions,
      _incompatibleRoles,
      _incompatibleRolesLoading,
      canExecute,
      showEnvironment
    } = this.props;
    //
    const isNew = this._getIsNew();
    let request = isNew ? this.state.request : _request;
    if (!request) {
      request = this.state.request;
    }
    const requestForForm = _.merge({}, request);
    // Form is rendered if data are changed, but we don't want rerenderd the form if only some
    // concept was changed (prevent lost other changes in form ... filled description for example).
    requestForForm.conceptRoles = null;

    // We want show audit fields only for Admin, but not in concept state.
    const hasAdminRights = Utils.Permission.hasPermission(_permissions, 'ADMIN');
    const _adminMode = hasAdminRights && request.state !== RoleRequestStateEnum.findKeyBySymbol(RoleRequestStateEnum.CONCEPT);
    const showLoading = !request || (_showLoading && !isNew) || this.state.showLoading || this.props.showLoading;
    const isEditable = request && _.includes(editableInStates, request.state);
    const canExecuteTheRequest = isEditable && _.includes(['CONCEPT', 'EXCEPTION'], request.state);
    const systemStateLog = request && request.systemState ? request.systemState.stackTrace : null;

    if (this.state.showLoading || !request) {
      return (
        <div>
          <Basic.ContentHeader rendered={ showRequestDetail }>
            <Basic.Icon value="component:role-request"/>
            {' '}
            <span dangerouslySetInnerHTML={{ __html: this.i18n('header') }}/>
          </Basic.ContentHeader>
          <Basic.PanelBody>
            <Basic.Loading show isStatic style={{marginTop: '300px', marginBottom: '300px'}} />
          </Basic.PanelBody>
        </div>);
    }

    const approvers = request.approvers;

    return (
      <div>
        <Basic.Confirm ref="confirm-incompatible-role" level="warning">
          <IncompatibleRoleWarning incompatibleRoles={ _.uniqWith(_incompatibleRoles, (irOne, irTwo) => irOne.id === irTwo.id) } face="full"/>
          <Basic.Alert level="warning" text={ this.i18n(`confirm-incompatible-role.message`, { escape: false }) }/>
        </Basic.Confirm>
        <form onSubmit={this.save.bind(this, false)}>
          <Helmet title={this.i18n('title')} />
          <Basic.Confirm ref="confirm-delete" level="danger"/>
          <Basic.ContentHeader rendered={ showRequestDetail }>
            <Basic.Icon value="component:role-request"/>
            {' '}
            <span dangerouslySetInnerHTML={{ __html: this.i18n('header') }}/>
          </Basic.ContentHeader>
          <Basic.Panel>
            <Basic.AbstractForm
              readOnly={!isEditable}
              hidden={ !showRequestDetail }
              ref="form"
              data={requestForForm}
              showLoading={showLoading && showRequestDetail }
              style={{ padding: '15px 15px 0 15px', display: (showRequestDetail ? null : 'none') }}>
              <Basic.Row>
                <Basic.Col lg={ 6 }>
                  { this._getApplicantAndImplementer(request) }
                </Basic.Col>
              </Basic.Row>
              <Basic.LabelWrapper
                label={ this.i18n('entity.RoleRequest.states') }>
                <div style={{ display: 'flex', alignItems: 'center' }}>
                  <Basic.Div>
                    <Basic.EnumValue
                      value={ request.state }
                      enum={ RoleRequestStateEnum }/>
                  </Basic.Div>
                  <Basic.Div
                    rendered={ !!request.systemState }
                    style={{ margin: '0 7px' }}>
                    /
                  </Basic.Div>
                  <Basic.Div>
                    <Advanced.OperationResult
                      value={ request.systemState }
                      stateLabel={ request.systemState
                        && request.systemState.state === 'CREATED'
                        ? this.i18n('enums.RoleRequestStateEnum.CONCEPT')
                        : null}
                    />
                  </Basic.Div>
                </div>
              </Basic.LabelWrapper>
              <Basic.Checkbox
                ref="executeImmediately"
                hidden={!hasAdminRights}
                label={ this.i18n('entity.RoleRequest.executeImmediately.label') }
                helpBlock={ this.i18n('entity.RoleRequest.executeImmediately.help') }/>
              <Basic.LabelWrapper
                rendered={ _adminMode && request.wfProcessId }
                label={this.i18n('entity.RoleRequest.wfProcessId')}>
                <Advanced.WorkflowProcessInfo
                  entityIdentifier={ request.wfProcessId }
                  maxLength={100}
                  entity={ request._embedded ? request._embedded.wfProcessId : null }/>
              </Basic.LabelWrapper>
              <Basic.TextField
                ref="currentActivity"
                hidden={!_adminMode}
                readOnly
                label={this.i18n('entity.RoleRequest.currentActivity')}/>
              <Basic.LabelWrapper
                label={ this.i18n('entity.RoleRequest.candicateUsers') }
                rendered={ ((approvers && approvers.length > 0) === true) }>
                <Advanced.IdentitiesInfo identities={ approvers } isUsedIdentifier={ false } maxEntry={ 5 } />
              </Basic.LabelWrapper>
              <Basic.TextArea
                ref="description"
                rows={ 3 }
                placeholder={this.i18n('entity.RoleRequest.description.placeholder')}
                label={this.i18n('entity.RoleRequest.description.label')}/>

            </Basic.AbstractForm>
            <div style={{ padding: '15px 15px 0 15px' }}>
              <Basic.ContentHeader>
                <Basic.Icon value="list"/>
                {' '}
                <span dangerouslySetInnerHTML={{ __html: this.i18n('conceptWithCurrentRoleHeader') }}/>
              </Basic.ContentHeader>
              <RequestIdentityRoleTable
                request={request}
                showLoading={showLoading}
                showEnvironment={showEnvironment}
                incompatibleRoles={ _incompatibleRoles }
                readOnly={!isEditable || !roleRequestManager.canSave(request, _permissions) || !canExecute}
                identityId={this._getIdentityId(this.props)}
                replaceUrl={this.replaceUrl.bind(this)}
              />
              {
                // Table with concepts was hidden, is replaced by new request-identity-role-table
                // this._renderRoleConceptChangesTable(request, forceSearchParameters, _adminMode)
              }
              <Basic.AbstractForm
                readOnly
                ref="formLog"
                data={request}>
                <Basic.ScriptArea
                  ref="log"
                  showLoading={showLoading}
                  hidden={!_adminMode}
                  mode="sqlserver"
                  height="15em"
                  readOnly
                  label={this.i18n('entity.RoleRequest.log')}/>
              </Basic.AbstractForm>
              <Basic.AbstractForm
                readOnly
                ref="formSystemStateLog"
                data={{systemStateLog}}>
                <Basic.ScriptArea
                  ref="systemStateLog"
                  showLoading={showLoading}
                  hidden={!_adminMode || !systemStateLog}
                  mode="sqlserver"
                  height="15em"
                  readOnly
                  label={this.i18n('entity.RoleRequest.systemStateLog')}/>
              </Basic.AbstractForm>
            </div>
            <Basic.PanelFooter>
              <Basic.Button
                type="button"
                level="link"
                onClick={this.context.history.goBack}
                showLoading={showLoading}>
                {this.i18n('button.back')}
              </Basic.Button>
              <Basic.Button
                onClick={this.save.bind(this, false)}
                disabled={!isEditable}
                rendered={ request && roleRequestManager.canSave(request, _permissions)}
                level="default"
                type="submit"
                showLoading={ showLoading || _incompatibleRolesLoading }
                title={this.i18n('button.saveConcept.tooltip')}
                style={{ marginRight: 3 }}>
                {this.i18n('button.saveConcept.label')}
              </Basic.Button>
              <Basic.Button
                level="success"
                icon="fa:object-group"
                disabled={!canExecuteTheRequest}
                showLoading={ showLoading || _incompatibleRolesLoading }
                onClick={ this._confirmIncompatibleRoles.bind(this, _incompatibleRoles) }
                rendered={ request && roleRequestManager.canSave(request, _permissions)}
                titlePlacement="bottom"
                title={ this.i18n('button.createRequest.tooltip') }>
                { this.i18n('button.createRequest.label') }
              </Basic.Button>
            </Basic.PanelFooter>
          </Basic.Panel>
        </form>
      </div>
    );
  }
}

RoleRequestDetail.propTypes = {
  _showLoading: PropTypes.bool,
  editableInStates: PropTypes.arrayOf(PropTypes.string),
  showRequestDetail: PropTypes.bool,
  canExecute: PropTypes.bool,
};
RoleRequestDetail.defaultProps = {
  editableInStates: ['CONCEPT', 'EXCEPTION', 'DUPLICATED'],
  showRequestDetail: true,
  canExecute: true
};

function select(state, component) {
  const result = {
    showEnvironment: ConfigurationManager.getPublicValueAsBoolean(state, 'idm.pub.app.show.environment', true)
  };
  const entityId = component.entityId ? component.entityId : component.match.params.entityId;
  const entity = roleRequestManager.getEntity(state, entityId);
  const applicantFromUrl = component.location && component.location.query ? component.location.query.applicantId : null;
  const identityId = entity ? entity.applicant : applicantFromUrl;
  if (entity && entity._embedded && entity._embedded.wfProcessId) {
    entity.currentActivity = entity._embedded.wfProcessId.name;
    entity.candicateUsers = entity._embedded.wfProcessId.candicateUsers;
  }
  return {
    ...result,
    _request: entity,
    _showLoading: !entity,
    showLoadingRoles: identityRoleManager.isShowLoading(state, `${uiKey}-${identityId}`),
    _permissions: roleRequestManager.getPermissions(state, null, entity),
    _incompatibleRoles: entity ? DataManager.getData(state, `${ uiKeyIncompatibleRoles }${entity.id}`) : null,
    _incompatibleRolesLoading: entity ? DataManager.isShowLoading(state, `${ uiKeyIncompatibleRoles }${entity.id}`) : false
  };
}

export default connect(select)(RoleRequestDetail);
