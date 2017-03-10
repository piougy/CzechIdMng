import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { RoleRequestManager, ConceptRoleRequestManager, IdentityRoleManager, SecurityManager} from '../../redux';
import RoleRequestStateEnum from '../../enums/RoleRequestStateEnum';
import ConceptRoleRequestOperationEnum from '../../enums/ConceptRoleRequestOperationEnum';
import SearchParameters from '../../domain/SearchParameters';
import EntityUtils from '../../utils/EntityUtils';
import UiUtils from '../../utils/UiUtils';
import RoleConceptTable from './RoleConceptTable';

const uiKey = 'role-request';
const uiKeyAttributes = 'concept-role-requests-table';
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
    const {entityId} = nextProps.params;
    if (entityId && entityId !== this.props.params.entityId) {
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
  }

  /**
   * Method for init component from didMount method and from willReceiveProps method
   * @param  {properties of component} props For didmount call is this.props for call from willReceiveProps is nextProps.
   */
  _initComponent(props) {
    const { entityId} = props.params;
    if (this._getIsNew(props)) {
      this.setState({request: {
        applicant: props.location.query.applicantId,
        state: RoleRequestStateEnum.findKeyBySymbol(RoleRequestStateEnum.CONCEPT),
        requestedByType: 'MANUALLY'
      }}, () => {
        this.save(null);
      });
    } else {
      this.context.store.dispatch(roleRequestManager.fetchEntity(entityId));
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
  save(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }

    const formEntity = this.refs.form.getData();

    if (formEntity.id === undefined) {
      this.context.store.dispatch(roleRequestManager.createEntity(formEntity, `${uiKey}-detail`, (createdEntity, error) => {
        this.afterSave(createdEntity, error);
      }));
    } else {
      this.context.store.dispatch(roleRequestManager.updateEntity(formEntity, `${uiKey}-detail`, this.afterSave.bind(this)));
    }
  }

  afterSave(entity, error) {
    if (!error) {
      this.addMessage({ message: this.i18n('save.success') });
      if (this._getIsNew()) {
        this.context.router.replace(`/role-requests/${entity.id}/detail`, {entityId: entity.id});
      }
    } else {
      this.addError(error);
    }
  }

  closeDetail() {
    this.refs.form.processEnded();
  }

  _getIsNew(nextProps) {
    const { query } = nextProps ? nextProps.location : this.props.location;
    return (query) ? query.new : null;
  }

  _removeConcept(data, type) {
    const {_request} = this.props;
    let concept = data;
    if (type === ConceptRoleRequestOperationEnum.findKeyBySymbol(ConceptRoleRequestOperationEnum.REMOVE)
      || (type === ConceptRoleRequestOperationEnum.findKeyBySymbol(ConceptRoleRequestOperationEnum.UPDATE))) {
      for (const conceptRole of _request.conceptRoles) {
        if (conceptRole.identityRole === data) {
          concept = conceptRole;
        }
      }
    }

    this.context.store.dispatch(conceptRoleRequestManager.deleteEntity(concept, `${uiKeyAttributes}-detail`, (deletedEntity, error) => {
      if (!error) {
        for (const conceptRole of _request.conceptRoles) {
          if (conceptRole.id === concept.id) {
            _request.conceptRoles.splice(_request.conceptRoles.indexOf(conceptRole), 1);
          }
        }
        this.refs.table.getWrappedInstance().reload();
      } else {
        this.addError(error);
      }
    }));
  }

  _updateConcept(data, type) {
    const {_request} = this.props;
    console.log("update concept", data);

    const concept = {
      'id': data.id,
      'operation': type,
      'roleRequest': _request.id,
      'identityContract': data.identityContract,
      'role': data.role,
      'identityRole': data.identityRole,
      'validFrom': data.validFrom,
      'validTill': data.validTill,
    };
    this.context.store.dispatch(conceptRoleRequestManager.updateEntity(concept, `${uiKeyAttributes}-detail`, (updatedEntity, error) => {
      if (!error) {
        for (let i = 0; i < _request.conceptRoles.length; i++) {
          if (_request.conceptRoles[i].id === updatedEntity.id) {
            _request.conceptRoles[i] = updatedEntity;
          }
        }
        this.refs.table.getWrappedInstance().reload();
      } else {
        this.addError(error);
      }
    }));
  }

  _createConcept(data, type) {
    const {_request} = this.props;
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
      _request.conceptRoles.push(json);
      this.refs.table.getWrappedInstance().reload();
    })
    .catch(error => {
      this.addError(error);
    });

    // this.context.store.dispatch(conceptRoleRequestManager.createEntity(concept, `${uiKeyAttributes}-detail`, (createdEntity, error) => {
    //   if (!error) {
    //     _request.conceptRoles.push(createdEntity);
    //     this.refs.table.getWrappedInstance().reload();
    //   } else {
    //     this.addError(error);
    //   }
    // }));
  }

  render() {
    const { _showLoading, _request, _currentIdentityRoles} = this.props;
    const forceSearchParameters = new SearchParameters().setFilter('roleRequestId', _request ? _request.id : SearchParameters.BLANK_UUID);
    const isNew = this._getIsNew();
    const request = isNew ? this.state.request : _request;
    const showLoading = !request || _showLoading;

    const addedIdentityRoles = [];
    const changedIdentityRoles = [];
    const removedIdentityRoles = [];
    if (request && request.conceptRoles) {
      const conceptRoles = request.conceptRoles;
      for (const concept of conceptRoles) {
        if (concept.operation === ConceptRoleRequestOperationEnum.findKeyBySymbol(ConceptRoleRequestOperationEnum.ADD)) {
          addedIdentityRoles.push(concept);
        }
        if (concept.operation === ConceptRoleRequestOperationEnum.findKeyBySymbol(ConceptRoleRequestOperationEnum.UPDATE)) {
          changedIdentityRoles.push(concept);
        }
        if (concept.operation === ConceptRoleRequestOperationEnum.findKeyBySymbol(ConceptRoleRequestOperationEnum.REMOVE)) {
          removedIdentityRoles.push(concept.identityRole);
        }
      }
    }

    return (
      <div>
        <form onSubmit={this.save.bind(this)}>
          <Helmet title={this.i18n('title')} />
          <Basic.Confirm ref="confirm-delete" level="danger"/>
          <Basic.ContentHeader>
            <Basic.Icon value="compressed"/>
            {' '}
            <span dangerouslySetInnerHTML={{ __html: this.i18n('header') }}/>
          </Basic.ContentHeader>
          <Basic.Panel>
            <Basic.AbstractForm ref="form" data={request} showLoading={showLoading} style={{ padding: '15px 15px 0 15px' }}>
              <Basic.LabelWrapper rendered={request && request.applicant} readOnly ref="applicant" label={this.i18n('entity.RoleRequest.applicant')}>
                <Advanced.IdentityInfo username={request && request.applicant} showLoading={!request} className="no-margin" style={{ width: '60%' }}/>
              </Basic.LabelWrapper>
              <Basic.EnumSelectBox
                ref="state"
                readOnly
                label={this.i18n('entity.RoleRequest.state')}
                enum={RoleRequestStateEnum}
                style={{ maxWidth: '60%' }}
                required/>
              <Basic.Checkbox
                ref="executeImmediately"
                label={this.i18n('entity.RoleRequest.executeImmediately')}/>
              <Basic.TextArea
                ref="log"
                rows="8"
                readOnly
                label={this.i18n('entity.RoleRequest.log')}/>
            </Basic.AbstractForm>
            <Basic.PanelFooter>
              <Basic.Button type="button" level="link"
                onClick={this.context.router.goBack}
                showLoading={showLoading}>
                {this.i18n('button.back')}
              </Basic.Button>
              <Basic.Button
                onClick={this.save.bind(this)}
                level="success"
                type="submit"
                showLoading={showLoading}>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.PanelFooter>
          </Basic.Panel>
        </form>
        <Basic.ContentHeader rendered={request && !isNew}>
          <Basic.Icon value="list"/>
          {' '}
          <span dangerouslySetInnerHTML={{ __html: this.i18n('conceptHeader') }}/>
        </Basic.ContentHeader>
        <Basic.Panel rendered={request}>
          <RoleConceptTable
            ref="identityRoleConceptTable"
            uiKey="identity-role-concept-table"
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
            <Advanced.Column
              property=""
              header=""
              className="detail-button"
              cell={
                ({ rowIndex, data }) => {
                  return (
                    <Advanced.DetailButton
                      title={this.i18n('button.detail')}
                      onClick={this.showDetail.bind(this, data[rowIndex], false)}/>
                  );
                }
              }/>
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
}

RoleRequestDetail.propTypes = {
  _showLoading: PropTypes.bool,
};
RoleRequestDetail.defaultProps = {
  _showLoading: false,
};

function select(state, component) {
  const entity = EntityUtils.getEntity(state, roleRequestManager.getEntityType(), component.params.entityId);
  let _currentIdentityRoles = null;
  const applicantFromUrl = component.location.query.applicantId;
  if (entity) {
    _currentIdentityRoles = identityRoleManager.getEntities(state, `${uiKey}-${entity.applicant}`);
  } else if (applicantFromUrl) {
    _currentIdentityRoles = identityRoleManager.getEntities(state, `${uiKey}-${applicantFromUrl}`);
  }
  return {
    _request: entity,
    _showLoading: UiUtils.isShowLoading(state, `${uiKey}-detail`),
    _currentIdentityRoles
  };
}

export default connect(select)(RoleRequestDetail);
