import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import OperationStateEnum from '../../enums/OperationStateEnum';
import {SecurityManager, RequestManager, RequestItemManager, WorkflowTaskInstanceManager } from '../../redux';
import RoleRequestStateEnum from '../../enums/RoleRequestStateEnum';
import ConceptRoleRequestOperationEnum from '../../enums/ConceptRoleRequestOperationEnum';
import SearchParameters from '../../domain/SearchParameters';
import RequestItemChangesTable from './RequestItemChangesTable';

const uiKey = 'universal-request';
const uiKeyRequestItems = 'request-items';
const requestItemManager = new RequestItemManager();
const requestManager = new RequestManager();
const workflowTaskInstanceManager = new WorkflowTaskInstanceManager();

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
    if (!this._getIsNew(props)) {
      this.context.store.dispatch(requestManager.fetchEntity(_entityId));
      this._reloadRequestItems({id: _entityId});
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
    this.setState({itemDetail: {show: false}});
  }

  previewDetailByRequest(entity) {
    const urlType = this._getUrlType(entity.ownerType);
    this.context.router.push(`/requests/${entity.id}/${urlType}/${entity.ownerId}/detail`);
  }

  showItemChanges(entity) {
    this.getManager().getService().getChanges(entity.id)
    .then(json => {
      this.setState({itemDetail: {changes: json, show: true, item: entity}});
    })
    .catch(error => {
      this.addError(error);
    });
  }

  _getNameOfDTO(ownerType) {
    const types = ownerType.split('.');
    return types[types.length - 1];
  }

  _getUrlType(ownerType) {
    const dtoType = this._getNameOfDTO(ownerType);
    const words = dtoType.split(/(?=[A-Z])/);
    words.splice(0, 1);
    words.splice(words.length - 1, 1);
    return words.map(w => w.toLowerCase()).join('-');
  }

  _getIsNew(nextProps) {
    if ((nextProps && nextProps.location) || this.props.location) {
      const { query } = nextProps ? nextProps.location : this.props.location;
      return (query) ? query.new : null;
    }
    return false;
  }

  _reloadRequestItems(_request) {
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
      // this.context.router.goBack();
      if (json.result.state === RoleRequestStateEnum.findKeyBySymbol(RoleRequestStateEnum.EXCEPTION)) {
        this.addMessage({ message: this.i18n('content.requests.action.startRequest.exception'), level: 'error' });
        this._initComponent(this.props);
        return;
      }
      this.addMessage({ message: this.i18n('content.requests.action.startRequest.started') });
      this._initComponent(this.props);
    }).catch(ex => {
      this.setState({
        showLoading: false
      });
      this.addError(ex);
      this._initComponent(this.props);
    });
    return;
  }

  _renderDetailCell({ rowIndex, data }) {
    return (
      <Basic.Button
        type="button"
        level="warning"
        title={ this.i18n('button.showChanges.tooltip')}
        titlePlacement="bottom"
        onClick={this.showItemChanges.bind(this, data[rowIndex])}
        className="btn-xs">
        <Basic.Icon type="fa" icon="exchange"/>
      </Basic.Button>
    );
  }

  _renderOriginalOwnerCell({ rowIndex, data }) {
    const entity = data[rowIndex];
    const entityType = this._getNameOfDTO(entity.ownerType);
    return (
      <Advanced.EntityInfo
        entityType={ entityType }
        entityIdentifier={ entity.ownerId }
        face="popover"/>
    );
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
            showRowSelection={SecurityManager.hasAuthority('REQUEST_UPDATE')}
            actions={
              [{ value: 'delete', niceLabel: this.i18n('action.delete.action'),
                 action: this.onDelete.bind(this), disabled: false }]
            }
            >
            <Advanced.Column
              property=""
              header=""
              className="detail-button"
              cell={this._renderDetailCell.bind(this)}/>
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
              property="ownerId"
              header={ this.i18n('entity.RequestItem.originalOwnerId') }
              face="text"
              cell={this._renderOriginalOwnerCell.bind(this)}/>
            <Advanced.Column
              property="candicateUsers"
              rendered={false}
              face="text"
              cell={this._getCandidatesCell}
              />
            <Advanced.Column
              property="currentActivity"
              face="text"
              cell={this._getCurrentActivitiCell}
              />
            <Advanced.Column
              property="wfProcessId"
              cell={this._getWfProcessCell}
              sort
              face="text"/>
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

  _getCandidatesCell({ rowIndex, data, property}) {
    const entity = data[rowIndex];
    if (!entity || !entity._embedded || !entity._embedded.wfProcessId) {
      return '';
    }
    return (
      <Advanced.IdentitiesInfo identities={entity._embedded.wfProcessId[property]} maxEntry={5} />
    );
  }

  _getCurrentActivitiCell({ rowIndex, data}) {
    const entity = data[rowIndex];
    if (!entity || !entity._embedded || !entity._embedded.wfProcessId) {
      return '';
    }
    const task = {taskName: entity._embedded.wfProcessId.currentActivityName,
                  processDefinitionKey: entity._embedded.wfProcessId.processDefinitionKey,
                  definition: {id: entity._embedded.wfProcessId.activityId}
                };
    return (
      workflowTaskInstanceManager.localize(task, 'name')
    );
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

  _getApplicantAndImplementer(request) {
    const entityType = this._getNameOfDTO(request.ownerType);
    return (
      <div>
        <Basic.LabelWrapper
          rendered={(request && request.ownerId) ? true : false}
          readOnly
          ref="entity"
          label={this.i18n('entity.Request.entity')}>
          <Advanced.EntityInfo
            entityType={entityType}
            entityIdentifier={ request.ownerId }
            showLink
            face="full"/>
        </Basic.LabelWrapper>
        <Basic.LabelWrapper
          rendered={(request && request.creatorId) ? true : false}
          readOnly
          ref="implementer"
          label={this.i18n('entity.Request.implementer')}>
          <Advanced.IdentityInfo
            face="popover"
            entityIdentifier={request && request.creatorId}
            showLoading={!request}/>
        </Basic.LabelWrapper>
      </div>
    );
  }

  _onChangeRequestType(requestType) {
    this.setState({requestType: requestType ? requestType.value : null});
  }

  /**
   * Create value (highlights changes) cell for attributes table
   */
  _getWishValueCell( old = false, showChanges = true, { rowIndex, data}) {
    const entity = data[rowIndex];
    if (!entity || (!entity.value && !entity.values)) {
      return '';
    }
    if (entity.multivalue) {
      const listResult = [];
      if (!entity.values) {
        return '';
      }
      for (const item of entity.values) {
        const value = old ? item.oldValue : item.value;
        if (!old && item.change && showChanges) {
          listResult.push(<Basic.Label
            key={value}
            level={ConceptRoleRequestOperationEnum.getLevel(item.change)}
            title={item.change ? this.i18n(`attribute.diff.${item.change}`) : null}
            style={item.change === 'REMOVE' ? {textDecoration: 'line-through'} : null}
            text={value}/>);
        } else {
          listResult.push(value ? (item.value + ' ') : '');
        }
        listResult.push(' ');
      }
      return listResult;
    }

    if (!entity.value) {
      return '';
    }
    const value = old ? entity.value.oldValue : entity.value.value;
    if (!old && entity.value.change && showChanges) {
      return (<Basic.Label
        title={entity.value.change ? this.i18n(`attribute.diff.${entity.value.change}`) : null}
        level={ConceptRoleRequestOperationEnum.getLevel(entity.value.change)}
        text={value !== null ? value + '' : '' }/>);
    }
    return value !== null ? value + '' : '';
  }

  render() {
    const {
      _showLoading,
      _request,
      editableInStates,
      showRequestDetail,
      _permissions} = this.props;
    const {itemDetail} = this.state;
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
    let operation = 'update';
    if (itemDetail && itemDetail.changes && itemDetail.changes.requestItem) {
      operation = itemDetail.changes.requestItem.operation.toLowerCase();
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
                rendered={false}
                label={this.i18n('entity.Request.name')}/>
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
                level="primary"
                disabled={!isEditable}
                showLoading={showLoading}
                onClick={this.previewDetailByRequest.bind(this, request)}
                rendered={ request && requestManager.canSave(request, _permissions)}
                titlePlacement="bottom"
                title={this.i18n('button.previewDetailByRequest.tooltip')}>
                <Basic.Icon type="fa" icon="object-group"/>
                {' '}
                { this.i18n('button.previewDetailByRequest.label') }
              </Basic.Button>
                {' '}
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
        <Basic.Modal
          show={itemDetail && itemDetail.show}
          onHide={this.closeDetail.bind(this)}
          backdrop="static"
          keyboard={!_showLoading}>
            <Basic.Modal.Header closeButton={ !_showLoading } text={this.i18n(`itemDetail.title.${operation}`)}/>
            <Basic.Modal.Body>
              <RequestItemChangesTable
                itemData={itemDetail ? itemDetail.changes : null}/>
            </Basic.Modal.Body>
            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={ this.closeDetail.bind(this) }
                showLoading={ _showLoading }>
                { this.i18n('button.close') }
              </Basic.Button>
            </Basic.Modal.Footer>
        </Basic.Modal>
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
