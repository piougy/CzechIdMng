import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import _ from 'lodash';
import moment from 'moment';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import OperationStateEnum from '../../enums/OperationStateEnum';
import { RequestManager, RequestItemManager } from '../../redux';
import RoleRequestStateEnum from '../../enums/RoleRequestStateEnum';
import ConceptRoleRequestOperationEnum from '../../enums/ConceptRoleRequestOperationEnum';
import SearchParameters from '../../domain/SearchParameters';

const uiKey = 'universal-request';
const uiKeyRequestItems = 'request-items';
const requestItemManager = new RequestItemManager();
const requestManager = new RequestManager();

/**
 * Detail for universal request
 *
 * @author Vít Švanda
 */
class RequestDetail extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
  }

  getManager() {
    return requestItemManager;
  }

  getUiKey() {
    return uiKey;
  }

  getContentKey() {
    return 'content.requestDetail';
  }

  componentWillReceiveProps(nextProps) {
    const entityId = nextProps.entityId ? nextProps.entityId : nextProps.params.entityId;
    const entityIdCurrent = this.props.entityId ? this.props.entityId : this.props.params.entityId;
    if (entityId && entityId !== entityIdCurrent) {
      this._initComponent(nextProps);
    }
  }

  // Did mount only call initComponent method
  componentDidMount() {
    super.componentDidMount();
    this.selectNavigationItems(['audit', 'requests']);
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
    const { entityId} = props;
    const _entityId = entityId ? entityId : props.params.entityId;
    if (this._getIsNew(props)) {
      // const _automaticRoleId = props.location.query.automaticRoleId;
      // this.setState({
      //   showLoading: false,
      //   request: {
      //     role: props.location.query.roleId,
      //     automaticRole: _automaticRoleId,
      //     state: RoleRequestStateEnum.findKeyBySymbol(RoleRequestStateEnum.CONCEPT),
      //     operation: _automaticRoleId ?
      //               ConceptRoleRequestOperationEnum.findKeyBySymbol(ConceptRoleRequestOperationEnum.UPDATE)
      //               : ConceptRoleRequestOperationEnum.findKeyBySymbol(ConceptRoleRequestOperationEnum.ADD),
      //     requestedByType: 'MANUALLY'
      //   }}, () => {
      //   this.save(this, false, false);
      // });
    } else {
      this.context.store.dispatch(requestManager.fetchEntity(_entityId));
      this._reloadRuleRequests({id: _entityId});
    }
  }

  /**
   * Saves give entity
   */
  save(component, startRequest, validate, event) {
    if (event) {
      event.preventDefault();
    }
    if (validate && !this.refs.form.isFormValid()) {
      return;
    }

    this.setState({showLoading: true});
    const formEntity = this.refs.form.getData();

    if (formEntity.id === undefined) {
      this.context.store.dispatch(requestManager.createEntity(formEntity, `${uiKey}-detail`, (createdEntity, error) => {
        this.afterSave(createdEntity, error);
      }));
    } else {
      if (startRequest) {
        this.context.store.dispatch(requestManager.updateEntity(formEntity, `${uiKey}-detail`, this.afterSaveAndStartRequest.bind(this)));
      } else {
        this.context.store.dispatch(requestManager.updateEntity(formEntity, `${uiKey}-detail`, this.afterSave.bind(this)));
      }
    }
  }

  afterSave(entity, error) {
    this.setState({showLoading: false});
    if (!error) {
      if (this._getIsNew()) {
        this.context.router.replace(`/requests/${entity.id}/detail`);
      }
    } else {
      this.addError(error);
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

  _reloadRuleRequests(_request) {
    let forceSearchParameters = requestItemManager.getDefaultSearchParameters();
    if (_request.id) {
      forceSearchParameters = forceSearchParameters.setFilter('requestId', _request.id);
    } else {
      forceSearchParameters = forceSearchParameters.setFilter('requestId', SearchParameters.BLANK_UUID);
    }
    this.context.store.dispatch(requestItemManager.fetchEntities(forceSearchParameters, `${uiKeyRequestItems}-${_request.id}`));
  }

  _startRequest(idRequest, event) {
    if (event) {
      event.preventDefault();
    }
    this.setState({
      showLoading: true
    });
    const promise = requestManager.getService().startRequest(idRequest);
    promise.then((json) => {
      this.setState({
        showLoading: false
      });
      this.context.router.goBack();
      if (json.state === RoleRequestStateEnum.findKeyBySymbol(RoleRequestStateEnum.DUPLICATED)) {
        this.addMessage({ message: this.i18n('content.requests.action.startRequest.duplicated', { created: moment(json._embedded.duplicatedToRequest.created).format(this.i18n('format.datetime'))}), level: 'warning'});
        return;
      }
      if (json.state === RoleRequestStateEnum.findKeyBySymbol(RoleRequestStateEnum.EXCEPTION)) {
        this.addMessage({ message: this.i18n('content.requests.action.startRequest.exception'), level: 'error' });
        return;
      }
      this.addMessage({ message: this.i18n('content.requests.action.startRequest.started') });
    }).catch(ex => {
      this.setState({
        showLoading: false
      });
      this.addError(ex);
      if (this.refs.table) {
        this.refs.table.getWrappedInstance().reload();
      }
    });
    return;
  }

  _renderRequestItemsTable(request, forceSearchParameters, rendered) {
    if (!rendered) {
      return null;
    }
    return (
      <div>
        <Basic.ContentHeader rendered={request}>
          <Basic.Icon value="list"/>
          {' '}
          <span dangerouslySetInnerHTML={{ __html: this.i18n('conceptHeader') }}/>
        </Basic.ContentHeader>
        <Basic.Panel rendered={request}>
          <Advanced.Table
            ref="table"
            uiKey={uiKeyRequestItems}
            manager={requestItemManager}
            forceSearchParameters={forceSearchParameters}
            >
            <Advanced.Column
              property="operation"
              face="enum"
              enumClass={ConceptRoleRequestOperationEnum}
              header={this.i18n('entity.RequestItem.operation')}
              sort/>
            <Advanced.Column
              property="result"
              header={this.i18n('entity.RequestItem.result')}
              face="text"
              cell={
                ({ rowIndex, data }) => {
                  const entity = data[rowIndex];
                  return (
                    <Advanced.OperationResult value={ entity.result }/>
                  );
                }
              }/>
            <Advanced.Column
              property="originalOwnerId"
              header={ this.i18n('entity.RequestItem.originalOwnerId') }
              face="text"
              cell={
                /* eslint-disable react/no-multi-comp */
                ({ rowIndex, data }) => {
                  const entity = data[rowIndex];
                  const types = entity.ownerType.split('.');
                  const entityType = types[types.length - 1];
                  return (
                    <Advanced.EntityInfo
                      entityType={ entityType }
                      entityIdentifier={ entity.originalOwnerId }
                      /* entity={ entity._embedded.entity } */
                      face="popover"/>
                  );
                }
              }/>
            <Advanced.Column property="created" header={this.i18n('entity.created')} sort face="datetime"/>
          </Advanced.Table>
        </Basic.Panel>
      </div>
    );
  }

  _operationResultComponent(request) {
    if (!request.result || Utils.Entity.isNew(request)) {
      return null;
    }
    return (
      <div>
        <Basic.ContentHeader text={ this.i18n('content.scheduler.all-tasks.detail.result') }/>
        <div style={{ marginBottom: 15 }}>
          <Basic.EnumValue value={ request.result.state } enum={ OperationStateEnum }/>
          {
            (!request.result.code)
            ||
            <span style={{ marginLeft: 15 }}>
              {this.i18n('content.scheduler.all-tasks.detail.resultCode')}: { request.result.code }
            </span>
          }
          <Basic.FlashMessage message={this.getFlashManager().convertFromResultModel(request.result.model)} style={{ marginTop: 15 }}/>
        </div>
        {
          (!request.result.stackTrace)
          ||
          <div>
            <textArea
              rows="10"
              value={ request.result.stackTrace }
              readOnly
              style={{ width: '100%', marginBottom: 15 }}/>
          </div>
        }
      </div>
    );
  }

  _getApplicantAndImplementer() {
    return <div/>;
    // return (
    //   <div>
    //     <Basic.LabelWrapper
    //       rendered={(request && request.role) ? true : false}
    //       readOnly
    //       ref="role"
    //       label={this.i18n('entity.Request.role')}>
    //       <Advanced.RoleInfo
    //         entityIdentifier={request && request.role}
    //         showLoading={!request}
    //         face="full"
    //         showLink/>
    //     </Basic.LabelWrapper>
    //
    //     <Basic.LabelWrapper
    //       rendered={(request && request.creatorId) ? true : false}
    //       readOnly
    //       ref="implementer"
    //       label={this.i18n('entity.Request.implementer')}>
    //       <Advanced.IdentityInfo
    //         face="popover"
    //         entityIdentifier={request && request.creatorId}
    //         showLoading={!request}/>
    //     </Basic.LabelWrapper>
    //   </div>
    // );
  }

  _onChangeRequestType(requestType) {
    this.setState({requestType: requestType ? requestType.value : null});
  }

  render() {
    const {
      _showLoading,
      _request,
      editableInStates,
      showRequestDetail,
      _permissions} = this.props;
    //
    const forceSearchParameters = new SearchParameters().setFilter('requestId', _request ? _request.id : SearchParameters.BLANK_UUID);
    const isNew = this._getIsNew();
    const request = isNew ? this.state.request : _request;
    // We want show audit fields only for Admin, but not in concept state.
    const _adminMode = Utils.Permission.hasPermission(_permissions, 'ADMIN');
    const showLoading = !request || _showLoading || this.state.showLoading || this.props.showLoading;
    const isEditable = request && _.includes(editableInStates, request.state);
    let requestType = request ? request.requestType : null;
    requestType = this.state.requestType ? this.state.requestType : requestType;

    if (this.state.showLoading || !request) {
      return (<div>
        <Basic.ContentHeader rendered={showRequestDetail}>
          <Basic.Icon value="compressed"/>
          {' '}
          <span dangerouslySetInnerHTML={{ __html: this.i18n('header') }}/>
        </Basic.ContentHeader>
        <Basic.PanelBody>
          <Basic.Loading show isStatic />
        </Basic.PanelBody>
      </div>);
    }

    let isDeleteRequest = false;
    if (request && request.operation === ConceptRoleRequestOperationEnum.findKeyBySymbol(ConceptRoleRequestOperationEnum.REMOVE)) {
      isDeleteRequest = true;
    }

    return (
      <div>
        <form onSubmit={this.save.bind(this, this, false, true)}>
          <Helmet title={this.i18n('title')} />
          <Basic.Confirm ref="confirm-delete" level="danger"/>
          <Basic.ContentHeader rendered={showRequestDetail}>
            <Basic.Icon value="compressed"/>
            {' '}
            <span dangerouslySetInnerHTML={{ __html: this.i18n('header') }}/>
          </Basic.ContentHeader>
          <Basic.Panel rendered={showRequestDetail}>
            <Basic.AbstractForm readOnly={!isEditable} ref="form" data={request} showLoading={showLoading} style={{ padding: '15px 15px 0 15px' }}>
              <Basic.Row>
                <div className="col-lg-6">
                  {this._getApplicantAndImplementer(request)}
                </div>
              </Basic.Row>
              <Basic.TextField
                ref="name"
                label={this.i18n('entity.Request.name')}/>
              <Basic.EnumLabel
                ref="operation"
                enum={ConceptRoleRequestOperationEnum}
                label={this.i18n('entity.Request.operation')}/>
              <Basic.EnumLabel
                ref="state"
                enum={RoleRequestStateEnum}
                label={this.i18n('entity.Request.state')}/>
              <Basic.Checkbox
                ref="executeImmediately"
                hidden={!_adminMode}
                label={this.i18n('entity.Request.executeImmediately')}/>
              <Basic.TextArea
                ref="description"
                rows="3"
                placeholder={this.i18n('entity.Request.description.placeholder')}
                label={this.i18n('entity.Request.description.label')}/>
            </Basic.AbstractForm>
            <div style={{ padding: '15px 15px 0 15px' }}>
              {
                this._renderRequestItemsTable(request, forceSearchParameters, !isDeleteRequest)
              }
              <Basic.AbstractForm
                readOnly
                ref="form-wf"
                rendered={request.wfProcessId ? true : false}
                data={request}
                showLoading={showLoading}>
                <Basic.LabelWrapper
                  readOnly
                  hidden={!_adminMode}
                  ref="wfProcessId"
                  label={this.i18n('entity.Request.wfProcessId')}>
                  <Advanced.WorkflowProcessInfo
                    entityIdentifier={request && request.wfProcessId}
                    showLoading={!request}
                    face="full"
                    showLink/>
                </Basic.LabelWrapper>
                <Basic.TextField
                  ref="currentActivity"
                  hidden
                  readOnly
                  label={this.i18n('entity.Request.currentActivity')}/>
                <Basic.TextField
                  ref="candicateUsers"
                  hidden
                  readOnly
                  label={this.i18n('entity.Request.candicateUsers')}/>
              </Basic.AbstractForm>
              { // Renders result of operation (includes stack trace)
                this._operationResultComponent(request)
              }
            </div>
            <Basic.PanelFooter>
              <Basic.Button type="button" level="link"
                onClick={this.context.router.goBack}
                showLoading={showLoading}>
                {this.i18n('button.back')}
              </Basic.Button>
              <Basic.Button
                onClick={this.save.bind(this, this, false, true)}
                disabled={!isEditable}
                rendered={_adminMode}
                level="success"
                type="submit"
                showLoading={showLoading}>
                {this.i18n('button.save')}
              </Basic.Button>
                {' '}
              <Basic.Button
                level="success"
                disabled={!isEditable}
                showLoading={showLoading}
                onClick={this.save.bind(this, this, true, true)}
                rendered={ request && requestManager.canSave(request, _permissions)}
                titlePlacement="bottom"
                title={this.i18n('button.createRequest.tooltip')}>
                <Basic.Icon type="fa" icon="object-group"/>
                {' '}
                { this.i18n('button.createRequest.label') }
              </Basic.Button>
            </Basic.PanelFooter>
          </Basic.Panel>
        </form>
      </div>
    );
  }
}

RequestDetail.propTypes = {
  _showLoading: PropTypes.bool,
  editableInStates: PropTypes.arrayOf(PropTypes.string),
  showRequestDetail: PropTypes.bool,
  showCurrentRules: PropTypes.bool,
  canExecute: PropTypes.bool,
};
RequestDetail.defaultProps = {
  editableInStates: ['CONCEPT', 'EXCEPTION', 'DUPLICATED'],
  showRequestDetail: true,
  showCurrentRules: true,
  canExecute: true
};

function select(state, component) {
  const entityId = component.entityId ? component.entityId : component.params.entityId;
  const entity = requestManager.getEntity(state, entityId);
  let _requestItems = null;
  if (entityId) {
    _requestItems = requestItemManager.getEntities(state, `${uiKeyRequestItems}-${entityId}`);
  }

  if (entity && entity._embedded && entity._embedded.wfProcessId) {
    entity.currentActivity = entity._embedded.wfProcessId.name;
    entity.candicateUsers = entity._embedded.wfProcessId.candicateUsers;
  }
  return {
    _request: entity,
    _showLoading: entity ? false : true,
    _requestItems,
    _permissions: requestManager.getPermissions(state, null, entity)
  };
}

export default connect(select)(RequestDetail);
