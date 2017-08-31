import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
import uuid from 'uuid';
//
import { Basic, Advanced, Utils, Domain, Managers} from 'czechidm-core';
import { VsRequestManager} from '../../redux';
import VsOperationType from '../../enums/VsOperationType';
import VsRequestState from '../../enums/VsRequestState';

const accManagers = require('czechidm-acc').Managers;
const manager = new VsRequestManager();
const systemManager = new accManagers.SystemManager();

/**
* Table of virtula system requests
*
* @author Vít Švanda
*
*/
export class VsRequestTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: this.props.filterOpened,
    };
  }

  /**
   * "Shorcut" for localization
   */
  getContentKey() {
    return 'vs:content.vs-requests';
  }

  /**
   * Base manager for this agenda (used in `AbstractTableContent`)
   */
  getManager() {
    return manager;
  }

  /**
   * Submit filter action
   */
  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().useFilterForm(this.refs.filterForm);
  }

  /**
   * Cancel filter action
   */
  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  /**
   * Link to detail / create
   */
  showDetail(entity) {
    if (Utils.Entity.isNew(entity)) {
      const uuidId = uuid.v1();
      this.context.router.push(`/vs/request/${uuidId}/new?new=1`);
    } else {
      this.context.router.push(`/vs/request/${entity.id}/detail`);
    }
  }

  _cancelSynchronization(id, event) {
    if (event) {
      event.preventDefault();
    }
    this.refs[`confirm-delete`].show(
      this.i18n(`action.cancelSynchronization.message`),
      this.i18n(`action.cancelSynchronization.header`)
    ).then(() => {
      this.setState({
        showLoading: true
      });
      const promise = this.getManager().getService().cancelSynchronization(id);
      promise.then((json) => {
        this.setState({
          showLoading: false
        });
        if (this.refs.table) {
          this.refs.table.getWrappedInstance().reload();
        }
        this.addMessage({ message: this.i18n('action.cancelSynchronization.canceled', { name: json.name }) });
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


  /**
  * Mark virtual system request as realized (changes will be propagated to VsAccount)
  */
  realize(bulkActionValue, ids, event) {
    if (event) {
      event.preventDefault();
    }
    const selectedEntities = manager.getEntitiesByIds(this.context.store.getState(), ids);
    this.refs[`confirm-delete`].show(
      this.i18n(`action.realize.message`, { count: selectedEntities.length, record: manager.getNiceLabel(selectedEntities[0]), records: manager.getNiceLabels(selectedEntities).join(', ') }),
      this.i18n(`action.realize.header`, { count: selectedEntities.length, records: manager.getNiceLabels(selectedEntities).join(', ') })
    ).then(() => {
      const cb = (realizedEntity, newError) => {
        if (!newError) {
          this.setState({
            showLoading: false
          });
          if (this.refs.table) {
            this.refs.table.getWrappedInstance().reload();
          }
          this.addMessage({ message: this.i18n('action.realize.success', { record: realizedEntity.uid }) });
        } else {
          this.setState({
            showLoading: false
          });
          this.addError(newError);
          if (this.refs.table) {
            this.refs.table.getWrappedInstance().reload();
          }
        }
      };

      for (const id of ids) {
        this.setState({
          showLoading: true
        });
        this.context.store.dispatch(manager.realize(id, null, cb));
      }
    }, () => {
      // Rejected
    });
    return;
  }

  /**
  * Cancel virtual system request
  */
  cancel(bulkActionValue, ids, event) {
    if (event) {
      event.preventDefault();
    }
    const selectedEntities = manager.getEntitiesByIds(this.context.store.getState(), ids);
    this.refs[`confirm-cancel`].show(
      this.i18n(`action.cancel.message`, { count: selectedEntities.length, record: manager.getNiceLabel(selectedEntities[0]), records: manager.getNiceLabels(selectedEntities).join(', ') }),
      this.i18n(`action.cancel.header`, { count: selectedEntities.length, records: manager.getNiceLabels(selectedEntities).join(', ') }),
      this._validateCancelDialog.bind(this)
    ).then(() => {
      const cb = (realizedEntity, newError) => {
        if (!newError) {
          this.setState({
            showLoading: false
          });
          if (this.refs.table) {
            this.refs.table.getWrappedInstance().reload();
          }
          this.addMessage({ message: this.i18n('action.cancel.success', { record: realizedEntity.uid }) });
        } else {
          this.setState({
            showLoading: false
          });
          this.addError(newError);
          if (this.refs.table) {
            this.refs.table.getWrappedInstance().reload();
          }
        }
      };

      for (const id of ids) {
        this.setState({
          showLoading: true
        });
        const reason = this.refs['cancel-form'].getData()['cancel-reason'];
        this.context.store.dispatch(manager.cancel(id, reason, null, cb));
      }
    }, () => {
      // Rejected
    });
    return;
  }

  _validateCancelDialog(result) {
    if (result === 'reject') {
      return true;
    }
    if (result === 'confirm' && this.refs['cancel-form'].isFormValid()) {
      return true;
    }
    return false;
  }

  _getSystemCell({ rowIndex, data }) {
    return (
      <Advanced.EntityInfo
        entityType="system"
        entityIdentifier={ data[rowIndex]._embedded.systemId.id }
        face="popover" />
    );
  }

  _getImplementersCell({ rowIndex, data}) {
    const entity = data[rowIndex];
    if (!entity || !entity.implementers) {
      return '';
    }
    const identities = [];
    for (const implementer of entity.implementers) {
      identities.push(implementer.id);
    }

    return (
      <Advanced.IdentitiesInfo identities={identities}/>
    );
  }

  _getButtonsCell({ rowIndex, data}) {
    return (
      <span>
        <Basic.Button
          ref="realizeButton"
          type="button"
          level="success"
          rendered={Managers.SecurityManager.hasAnyAuthority(['VSREQUEST_UPDATE'])}
          style={{marginRight: '2px'}}
          title={this.i18n('vs:content.vs-request.detail.button.request.realize')}
          titlePlacement="bottom"
          onClick={this.realize.bind(this, 'realize', [data[rowIndex].id])}
          disabled={ data[rowIndex].state !== 'IN_PROGRESS' }
          className="btn-xs">
          <Basic.Icon type="fa" icon="play"/>
        </Basic.Button>
        <Basic.Button
          ref="cancelButton"
          type="button"
          level="danger"
          rendered={Managers.SecurityManager.hasAnyAuthority(['VSREQUEST_UPDATE'])}
          style={{marginRight: '2px'}}
          title={this.i18n('vs:content.vs-request.detail.button.request.cancel')}
          titlePlacement="bottom"
          onClick={this.cancel.bind(this, 'cancel', [data[rowIndex].id])}
          disabled={ data[rowIndex].state !== 'IN_PROGRESS' }
          className="btn-xs">
          <Basic.Icon type="fa" icon="remove"/>
        </Basic.Button>
      </span>
    );
  }

  render() {
    const { uiKey, columns, forceSearchParameters, showRowSelection } = this.props;
    const { filterOpened, showLoading} = this.state;

    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Basic.Confirm ref="confirm-cancel" level="danger">
          <Basic.AbstractForm ref="cancel-form" uiKey="confirm-cancel" >
            <Basic.TextArea
              ref="cancel-reason"
              label=" "
              placeholder={this.i18n('vs:content.vs-requests.cancel-reason.placeholder')}
              required/>
          </Basic.AbstractForm>
        </Basic.Confirm>

        <Advanced.Table
          ref="table"
          uiKey={ uiKey }
          manager={ manager }
          filterOpened={filterOpened}
          showLoading={showLoading}
          forceSearchParameters={ forceSearchParameters }
          showRowSelection={ Managers.SecurityManager.hasAuthority('VSREQUEST_UPDATE') && showRowSelection }
          filter={
            <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row className="last">
                  <div className="col-lg-4">
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={this.i18n('filter.text.placeholder')}/>
                  </div>
                  <div className="col-lg-4">
                    <Advanced.Filter.SelectBox
                      ref="systemId"
                      placeholder={this.i18n('acc:entity.System._type')}
                      multiSelect={false}
                      forceSearchParameters={new Domain.SearchParameters().setFilter('virtual', true)}
                      manager={systemManager}/>
                  </div>
                  <div className="col-lg-4 text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                  </div>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          actions={
            [
              { value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false },
              { value: 'realize', niceLabel: this.i18n('action.realize.action'), action: this.realize.bind(this), disabled: false },
              { value: 'cancel', niceLabel: this.i18n('action.cancel.action'), action: this.cancel.bind(this), disabled: false }
            ]
          }
          _searchParameters={ this.getSearchParameters() }
          >

          <Advanced.Column
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={this.i18n('button.detail')}
                    onClick={this.showDetail.bind(this, data[rowIndex])}/>
                );
              }
            }
            sort={false}/>
          <Advanced.Column property="uid" width="15%" sort face="text" rendered={_.includes(columns, 'uid')}/>
          <Advanced.Column
              header={this.i18n('acc:entity.System.name')}
              rendered={_.includes(columns, 'systemId')}
              cell={this._getSystemCell.bind(this)}/>
          <Advanced.Column property="operationType" width="15%" sort face="enum" enumClass={VsOperationType} rendered={_.includes(columns, 'operationType')}/>
          <Advanced.Column property="state" width="15%" sort face="enum" enumClass={VsRequestState} rendered={_.includes(columns, 'state')}/>
          <Advanced.Column property="executeImmediately" width="5%" sort face="boolean" rendered={_.includes(columns, 'executeImmediately')}/>
          <Advanced.Column
              header={this.i18n('vs:entity.VsRequest.implementers.label')}
              rendered={_.includes(columns, 'implementers')}
              cell={this._getImplementersCell.bind(this)}/>
          <Advanced.Column property="created" width="10%" sort face="datetime" rendered={_.includes(columns, 'created')}/>
          <Advanced.Column
            property=""
            header=""
            width="55px"
            className="detail-button"
            cell={this._getButtonsCell.bind(this)}/>
          </Advanced.Table>
      </div>
    );
  }
}

VsRequestTable.propTypes = {
  /**
   * Entities, permissions etc. fro this content are stored in redux under given key
   */
  uiKey: PropTypes.string.isRequired,
  /**
   * Rendered columns (all by default)
   */
  columns: PropTypes.arrayOf(PropTypes.string),
  /**
   * Show filter or collapse
   */
  filterOpened: PropTypes.bool,
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  /**
   * Show row selection for bulk actions
   */
  showRowSelection: PropTypes.bool
};

VsRequestTable.defaultProps = {
  columns: ['uid', 'state', 'systemId', 'operationType', 'executeImmediately', 'implementers', 'created'],
  filterOpened: false,
  forceSearchParameters: new Domain.SearchParameters(),
  showAddButton: true,
  showRowSelection: true
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey), // persisted filter state in redux
  };
}

export default connect(select, null, null, { withRef: true })(VsRequestTable);
