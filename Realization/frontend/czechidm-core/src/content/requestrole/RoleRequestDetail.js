import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import _ from 'lodash';
import moment from 'moment';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { RoleRequestManager, ConceptRoleRequestManager, IdentityRoleManager} from '../../redux';
import RoleRequestStateEnum from '../../enums/RoleRequestStateEnum';
import ConceptRoleRequestOperationEnum from '../../enums/ConceptRoleRequestOperationEnum';
import SearchParameters from '../../domain/SearchParameters';
import EntityUtils from '../../utils/EntityUtils';
import RoleConceptTable from './RoleConceptTable';

const uiKey = 'role-request';
const uiKeyAttributes = 'concept-role-requests';
const conceptRoleRequestManager = new ConceptRoleRequestManager();
const roleRequestManager = new RoleRequestManager();
const identityRoleManager = new IdentityRoleManager();

class RoleRequestDetail extends Basic.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
  }

  getManager() {
    return conceptRoleRequestManager;
  }

  getUiKey() {
    return uiKey;
  }

  getContentKey() {
    return 'content.roleRequestDetail';
  }

  componentWillReceiveProps(nextProps) {
    const {_request} = nextProps;
    const entityId = nextProps.entityId ? nextProps.entityId : nextProps.params.entityId;
    const entityIdCurrent = this.props.entityId ? this.props.entityId : this.props.params.entityId;
    if (entityId && entityId !== entityIdCurrent) {
      this._initComponent(nextProps);
    }
    if (_request && _request !== this.props._request) {
      this._initComponentCurrentRoles(nextProps);
    }
  }

  // Did mount only call initComponent method
  componentDidMount() {
    this._initComponent(this.props);
    this._initComponentCurrentRoles(this.props);
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
    const adminMode = props.adminMode !== undefined ? props.adminMode : props.location.state.adminMode;
    if (this._getIsNew(props)) {
      this.setState({
        adminMode,
        request: {
          applicant: props.location.query.applicantId,
          state: RoleRequestStateEnum.findKeyBySymbol(RoleRequestStateEnum.CONCEPT),
          requestedByType: 'MANUALLY'
        }}, () => {
        this.save(null, false);
      });
    } else {
      this.context.store.dispatch(roleRequestManager.fetchEntity(_entityId));
      this.setState({
        adminMode
      });
    }
  }

  _initComponentCurrentRoles(props) {
    const {_request} = props;
    if (this._getIsNew(props)) {
      const applicant = props.location.query.applicantId;
      this.context.store.dispatch(identityRoleManager.fetchRoles(applicant, `${uiKey}-${applicant}`));
    } else {
      if (_request) {
        this.context.store.dispatch(identityRoleManager.fetchRoles(_request.applicant, `${uiKey}-${_request.applicant}`));
      }
    }
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

    this.setState({showLoading: true});
    const formEntity = this.refs.form.getData();

    if (formEntity.id === undefined) {
      this.context.store.dispatch(roleRequestManager.createEntity(formEntity, `${uiKey}-detail`, (createdEntity, error) => {
        this.afterSave(createdEntity, error);
      }));
    } else {
      if (startRequest) {
        this.context.store.dispatch(roleRequestManager.updateEntity(formEntity, `${uiKey}-detail`, this.afterSaveAndStartRequest.bind(this)));
      } else {
        this.context.store.dispatch(roleRequestManager.updateEntity(formEntity, `${uiKey}-detail`, this.afterSave.bind(this)));
      }
    }
  }

  afterSave(entity, error) {
    this.setState({showLoading: false});
    if (!error) {
      const {adminMode} = this.state;
      this.addMessage({ message: this.i18n('save.success') });
      if (this._getIsNew()) {
        this.context.router.replace({pathname: `/role-requests/${entity.id}/detail`, param: {entityId: entity.id}, state: {adminMode}});
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

  _removeConcept(data, type) {
    const {_request} = this.props;
    this.setState({showLoading: true});

    let concept = data;
    if (type === ConceptRoleRequestOperationEnum.findKeyBySymbol(ConceptRoleRequestOperationEnum.REMOVE)
      || (type === ConceptRoleRequestOperationEnum.findKeyBySymbol(ConceptRoleRequestOperationEnum.UPDATE))) {
      for (const conceptRole of _request.conceptRoles) {
        if (conceptRole.identityRole === data) {
          concept = conceptRole;
        }
      }
    }

    conceptRoleRequestManager.getService().deleteById(concept.id)
    .then(() => {
      for (const conceptRole of _request.conceptRoles) {
        if (conceptRole.id === concept.id) {
          _request.conceptRoles.splice(_request.conceptRoles.indexOf(conceptRole), 1);
        }
      }
      this.refs.table.getWrappedInstance().reload();
      this.setState({showLoading: false});
    })
    .catch(error => {
      this.addError(error);
      this.setState({showLoading: false});
    });

    // this.context.store.dispatch(conceptRoleRequestManager.deleteEntity(concept, `${uiKeyAttributes}-deleteConcept-${_request.applicant}`, (deletedEntity, error) => {
    //   if (!error) {
    //     for (const conceptRole of _request.conceptRoles) {
    //       if (conceptRole.id === concept.id) {
    //         _request.conceptRoles.splice(_request.conceptRoles.indexOf(conceptRole), 1);
    //       }
    //     }
    //     this.refs.table.getWrappedInstance().reload();
    //   } else {
    //     this.addError(error);
    //   }
    // }));
  }

  _updateConcept(data, type) {
    const {_request} = this.props;
    this.setState({showLoading: true});

    let concept;
    if (type === ConceptRoleRequestOperationEnum.findKeyBySymbol(ConceptRoleRequestOperationEnum.UPDATE)) {
      concept = {
        'id': data.id,
        'operation': type,
        'roleRequest': _request.id,
        'identityContract': data.identityContract,
        'role': data.role,
        'identityRole': data.identityRole,
        'validFrom': data.validFrom,
        'validTill': data.validTill,
      };
    }
    if (type === ConceptRoleRequestOperationEnum.findKeyBySymbol(ConceptRoleRequestOperationEnum.ADD)) {
      concept = {
        'id': data.id,
        'operation': type,
        'roleRequest': _request.id,
        'identityContract': data.identityContract.id,
        'role': data.role,
        'identityRole': data.identityRole,
        'validFrom': data.validFrom,
        'validTill': data.validTill,
      };
    }
    this.context.store.dispatch(conceptRoleRequestManager.updateEntity(concept, `${uiKeyAttributes}-detail`, (updatedEntity, error) => {
      if (!error) {
        for (let i = 0; i < _request.conceptRoles.length; i++) {
          if (_request.conceptRoles[i].id === updatedEntity.id) {
            _request.conceptRoles[i] = updatedEntity;
          }
        }
        this.refs.table.getWrappedInstance().reload();
        this.setState({showLoading: false});
      } else {
        this.setState({showLoading: false});
        this.addError(error);
      }
    }));
  }

  _createConcept(data, type) {
    const {_request} = this.props;
    this.setState({showLoading: true});
    const concept = {
      'operation': type,
      'roleRequest': _request.id,
      'identityContract': data.identityContract.id,
      'role': data._embedded.role.id,
      'identityRole': data.id,
      'validFrom': data.validFrom,
      'validTill': data.validTill,
    };

    conceptRoleRequestManager.getService().create(concept)
    .then(json => {
      this.setState({showLoading: false});
      _request.conceptRoles.push(json);
      this.refs.table.getWrappedInstance().reload();
    })
    .catch(error => {
      this.setState({showLoading: false});
      this.addError(error);
    });

    // this.context.store.dispatch(conceptRoleRequestManager.createEntity(concept, `${uiKeyAttributes}-createConcept-${_request.applicant}`, (createdEntity, error) => {
    //   if (!error) {
    //     _request.conceptRoles.push(createdEntity);
    //     this.refs.table.getWrappedInstance().reload();
    //   } else {
    //     this.addError(error);
    //   }
    // }));
  }

  _startRequest(idRequest, event) {
    if (event) {
      event.preventDefault();
    }
    this.refs[`confirm-delete`].show(
      this.i18n(`content.roleRequests.action.startRequest.message`),
      this.i18n(`content.roleRequests.action.startRequest.header`)
    ).then(() => {
      this.setState({
        showLoading: true
      });
      const promise = roleRequestManager.getService().startRequest(idRequest);
      promise.then((json) => {
        this.setState({
          showLoading: false
        });
        this.context.router.goBack();
        if (json.state === RoleRequestStateEnum.findKeyBySymbol(RoleRequestStateEnum.DUPLICATED)) {
          this.addMessage({ message: this.i18n('content.roleRequests.action.startRequest.duplicated', { created: moment(json._embedded.duplicatedToRequest.created).format(this.i18n('format.datetime'))}), level: 'warning'});
          return;
        }
        if (json.state === RoleRequestStateEnum.findKeyBySymbol(RoleRequestStateEnum.EXCEPTION)) {
          this.addMessage({ message: this.i18n('content.roleRequests.action.startRequest.exception'), level: 'error' });
          return;
        }
        this.addMessage({ message: this.i18n('content.roleRequests.action.startRequest.started') });
      }).catch(ex => {
        this.setState({
          showLoading: false
        });
        this.addError(ex);
        if (this.refs.table) {
          this.refs.table.getWrappedInstance().reload();
        }
      });
    }, () => {
      // Rejected
    });
    return;
  }

  _renderRoleConceptChangesTable(request, forceSearchParameters, rendered) {
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
            uiKey={uiKeyAttributes}
            manager={conceptRoleRequestManager}
            forceSearchParameters={forceSearchParameters}
            actions={
              [{ value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }]
            }
            >
            <Advanced.Column property="_embedded.identityContract.position" header={this.i18n('entity.ConceptRoleRequest.identityContract')} sort/>
            <Advanced.Column property="_embedded.role.name" header={this.i18n('entity.ConceptRoleRequest.role')} sort/>
            <Advanced.Column property="operation" face="enum" enumClass={ConceptRoleRequestOperationEnum} header={this.i18n('entity.ConceptRoleRequest.operation')} sort/>
            <Advanced.Column property="state" face="enum" enumClass={RoleRequestStateEnum} header={this.i18n('entity.ConceptRoleRequest.state')} sort/>
            <Advanced.Column property="validFrom" face="date" header={this.i18n('entity.ConceptRoleRequest.validFrom')} sort/>
            <Advanced.Column property="validTill" face="date" header={this.i18n('entity.ConceptRoleRequest.validTill')} sort/>
            <Advanced.Column property="wfProcessId" face="text" header={this.i18n('entity.ConceptRoleRequest.wfProcessId')} sort/>
          </Advanced.Table>
        </Basic.Panel>
      </div>
    );
  }

  render() {
    const { _showLoading, _request, _currentIdentityRoles, adminMode, editableInStates, showRequestDetail} = this.props;

    const _adminMode = this.state.adminMode !== null ? this.state.adminMode : adminMode;
    const forceSearchParameters = new SearchParameters().setFilter('roleRequestId', _request ? _request.id : SearchParameters.BLANK_UUID);
    const isNew = this._getIsNew();
    const request = isNew ? this.state.request : _request;
    const showLoading = !request || _showLoading || this.state.showLoading;
    const isEditable = request && _.includes(editableInStates, request.state);

    const addedIdentityRoles = [];
    const changedIdentityRoles = [];
    const removedIdentityRoles = [];
    if (request && request.conceptRoles) {
      const conceptRoles = request.conceptRoles;
      for (const concept of conceptRoles) {
        if (concept.operation === ConceptRoleRequestOperationEnum.findKeyBySymbol(ConceptRoleRequestOperationEnum.ADD)
          && concept.state !== RoleRequestStateEnum.findKeyBySymbol(RoleRequestStateEnum.EXECUTED)) {
          addedIdentityRoles.push(concept);
        }
        if (concept.operation === ConceptRoleRequestOperationEnum.findKeyBySymbol(ConceptRoleRequestOperationEnum.UPDATE)
          && concept.state !== RoleRequestStateEnum.findKeyBySymbol(RoleRequestStateEnum.EXECUTED)) {
          changedIdentityRoles.push(concept);
        }
        if (concept.operation === ConceptRoleRequestOperationEnum.findKeyBySymbol(ConceptRoleRequestOperationEnum.REMOVE)
          && concept.state !== RoleRequestStateEnum.findKeyBySymbol(RoleRequestStateEnum.EXECUTED)) {
          removedIdentityRoles.push(concept.identityRole);
        }
      }
    }

    return (
      <div>
        <form onSubmit={this.save.bind(this, false)}>
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
                  <Basic.LabelWrapper
                    rendered={request && request.applicant}
                    readOnly
                    ref="applicant"
                    label={this.i18n('entity.RoleRequest.applicant')}>
                    <Advanced.IdentityInfo
                      username={request && request.applicant}
                      showLoading={!request}/>
                  </Basic.LabelWrapper>
                </div>
              </Basic.Row>
              <Basic.EnumLabel
                ref="state"
                enum={RoleRequestStateEnum}
                label={this.i18n('entity.RoleRequest.state')}/>
              <Basic.Checkbox
                ref="executeImmediately"
                hidden={!_adminMode}
                label={this.i18n('entity.RoleRequest.executeImmediately')}/>
              <Basic.TextField
                ref="wfProcessId"
                hidden={!_adminMode}
                readOnly
                label={this.i18n('entity.RoleRequest.wfProcessId')}/>
              <Basic.TextField
                ref="currentActivity"
                hidden={!_adminMode}
                readOnly
                label={this.i18n('entity.RoleRequest.currentActivity')}/>
              <Basic.TextField
                ref="candicateUsers"
                hidden={!_adminMode}
                readOnly
                label={this.i18n('entity.RoleRequest.candicateUsers')}/>
              <Basic.TextArea
                ref="log"
                rows="8"
                hidden={!_adminMode}
                readOnly
                label={this.i18n('entity.RoleRequest.log')}/>
              <Basic.TextArea
                ref="description"
                rows="3"
                placeholder={this.i18n('entity.RoleRequest.description.placeholder')}
                label={this.i18n('entity.RoleRequest.description.label')}/>
              {this._renderRoleConceptChangesTable(request, forceSearchParameters, true)}
            </Basic.AbstractForm>
            <Basic.PanelFooter>
              <Basic.Button type="button" level="link"
                onClick={this.context.router.goBack}
                showLoading={showLoading}>
                {this.i18n('button.back')}
              </Basic.Button>
              <Basic.Button
                onClick={this.save.bind(this, false)}
                disabled={!isEditable}
                rendered={_adminMode}
                level="success"
                type="submit"
                showLoading={showLoading}>
                {this.i18n('button.save')}
              </Basic.Button>
              <Basic.Button
                level="success"
                disabled={!isEditable}
                showLoading={showLoading}
                onClick={this.save.bind(this, true)}
                rendered={request && !_adminMode}
                titlePlacement="bottom"
                title={this.i18n('button.createRequest.tooltip')}>
                <Basic.Icon type="fa" icon="object-group"/>
                {' '}
                { this.i18n('button.createRequest.label') }
              </Basic.Button>
            </Basic.PanelFooter>
          </Basic.Panel>
        </form>
        {this._renderRoleConceptChangesTable(request, forceSearchParameters, !showRequestDetail)}
        <Basic.ContentHeader rendered={request && !isNew}>
          <Basic.Icon value="list"/>
          {' '}
          <span dangerouslySetInnerHTML={{ __html: this.i18n('conceptWithCurrentRoleHeader') }}/>
        </Basic.ContentHeader>
        <Basic.Panel rendered={request && _currentIdentityRoles}>
          <RoleConceptTable
            ref="identityRoleConceptTable"
            uiKey="identity-role-concept-table"
            showLoading={showLoading}
            readOnly={!isEditable}
            identityUsername={request && request.applicant}
            identityRoles={_currentIdentityRoles}
            addedIdentityRoles={addedIdentityRoles}
            changedIdentityRoles={changedIdentityRoles}
            removedIdentityRoles={removedIdentityRoles}
            removeConceptFunc={this._removeConcept.bind(this)}
            createConceptFunc={this._createConcept.bind(this)}
            updateConceptFunc={this._updateConcept.bind(this)}
            />
        </Basic.Panel>
      </div>
    );
  }
}

RoleRequestDetail.propTypes = {
  _showLoading: PropTypes.bool,
  adminMode: PropTypes.bool, // If true, then will be show advanced fields as log and etc.
  editableInStates: PropTypes.arrayOf(PropTypes.string),
  showRequestDetail: PropTypes.bool
};
RoleRequestDetail.defaultProps = {
  editableInStates: ['CONCEPT', 'EXCEPTION', 'DUPLICATED'],
  showRequestDetail: true
};

function select(state, component) {
  const entityId = component.entityId ? component.entityId : component.params.entityId;
  const entity = EntityUtils.getEntity(state, roleRequestManager.getEntityType(), entityId);
  let _currentIdentityRoles = null;
  const applicantFromUrl = component.location ? component.location.query.applicantId : null;
  if (entity) {
    _currentIdentityRoles = identityRoleManager.getEntities(state, `${uiKey}-${entity.applicant}`);
  } else if (applicantFromUrl) {
    _currentIdentityRoles = identityRoleManager.getEntities(state, `${uiKey}-${applicantFromUrl}`);
  }
  if (entity && entity._embedded && entity._embedded.wfProcessId) {
    entity.currentActivity = entity._embedded.wfProcessId.name;
    entity.candicateUsers = entity._embedded.wfProcessId.candicateUsers;
  }
  return {
    _request: entity,
    _showLoading: entity ? false : true,
    _currentIdentityRoles
  };
}

export default connect(select)(RoleRequestDetail);
