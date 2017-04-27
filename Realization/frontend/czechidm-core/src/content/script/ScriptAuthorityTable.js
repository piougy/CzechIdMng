import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import { SecurityManager, ScriptAuthorityManager, DataManager } from '../../redux';
import ScriptAuthorityTypeEnum from '../../enums/ScriptAuthorityTypeEnum';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';

const manager = new ScriptAuthorityManager();

const AVAILABLE_SERVICES_UIKEY = 'availableServicesUiKey';

/**
 * Table with authorities for scripts.
 * Allowed class and services for script
 */
class ScriptAuthorityTable extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    //
    const detail = {
      show: false,
      entity: null,
      isService: true
    };
    //
    this.state = {
      _showLoading: true,
      filterOpened: false,
      detail
    };
    //
    this.context.store.dispatch(manager.fetchAvailableServices(AVAILABLE_SERVICES_UIKEY));
  }

  getContentKey() {
    return 'content.scriptAuthorities';
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().useFilterForm(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  onDelete(bulkActionValue, selectedRows) {
    const { uiKey } = this.props;
    const selectedEntities = manager.getEntitiesByIds(this.context.store.getState(), selectedRows);
    //
    // show confirm message for deleting entity or entities
    this.refs['confirm-' + bulkActionValue].show(
      this.i18n(`action.${bulkActionValue}.message`, { count: selectedEntities.length, record: manager.getNiceLabel(selectedEntities[0]), records: manager.getNiceLabels(selectedEntities).join(', ') }),
      this.i18n(`action.${bulkActionValue}.header`, { count: selectedEntities.length, records: manager.getNiceLabels(selectedEntities).join(', ') })
    ).then(() => {
      // try delete
      this.context.store.dispatch(manager.deleteEntities(selectedEntities, uiKey, (entity, error, successEntities) => {
        if (entity && error) {
          this.addErrorMessage({ title: this.i18n(`action.delete.error`, { record: manager.getNiceLabel(entity) }) }, error);
        }
        if (!error && successEntities) {
          // refresh data in table
          this.refs.table.getWrappedInstance().reload();
        }
      }));
    }, () => {
      //
    });
  }

  /**
   * Save new or old script authority
   */
  save(values, event) {
    const { uiKey, scriptId } = this.props;
    const entity = this.refs.form.getData();
    //
    if (event) {
      event.preventDefault();
    }
    //
    if (!this.refs.form.isFormValid()) {
      return;
    }
    //
    this.setState({
      _showLoading: true
    }, this.refs.form.processStarted());

    // set script id into script authority
    entity.script = scriptId;

    //
    if (entity.id === undefined) {
      this.context.store.dispatch(manager.createEntity(entity, `${uiKey}-detail`, (createdEntity, error) => {
        this._afterSave(createdEntity, error);
      }));
    } else {
      this.context.store.dispatch(manager.updateEntity(entity, `${uiKey}-detail`, this._afterSave.bind(this)));
    }
  }

  /**
   * Method set _showLoading to false and if is'nt error then show success message
   */
  _afterSave(entity, error) {
    let { detail } = this.state;
    if (error) {
      this.setState({
        _showLoading: false
      }, this.refs.form.processEnded());
      this.addError(error);
      return;
    }
    this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
    this.refs.table.getWrappedInstance().reload();
    this.refs.form.processEnded();
    //
    detail = {
      ...detail,
      show: false,
      entity: null
    };
    //
    this.setState({
      detail,
      _showLoading: false
    });
  }

  getOptions(services) {
    if (!services) {
      return null;
    }
    const servicesOptions = [];
    if (services && services._embedded && services._embedded.availableServices) {
      for (let index = 0; index < services._embedded.availableServices.length; index++) {
        const service = services._embedded.availableServices[index];
        const row = {
          niceLabel: service.serviceName,
          value: service.serviceClass
        };
        servicesOptions.push(row);
      }
    }
    return servicesOptions;
  }

  /**
   * Change type, show dynamical text field with class name or service type
   */
  onChangeType(entity, event) {
    const { detail } = this.state;
    if (event) {
      event.preventDefault();
    }
    let isService = false;
    if (entity.value === ScriptAuthorityTypeEnum.findKeyBySymbol(ScriptAuthorityTypeEnum.SERVICE)) {
      isService = true;
    } else {
      this.refs.service.setValue('');
    }
    detail.isService = isService;
    this.setState({
      ...detail
    });
  }

  onChangeService(entity, event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.service.setValue(entity.niceLabel);
  }

  closeDetail(entity, event) {
    let { detail } = this.state;
    if (event) {
      event.preventDefault();
    }
    detail = {
      ...detail,
      show: false,
      entity: null
    };
    this.setState({
      detail
    });
  }

  /**
   * Show new form or detail for script authority, show in modal window
   */
  showDetail(entity, event) {
    let { detail } = this.state;
    if (event) {
      event.preventDefault();
    }
    if (Utils.Entity.isNew(entity)) {
      entity.type = ScriptAuthorityTypeEnum.findKeyBySymbol(ScriptAuthorityTypeEnum.SERVICE);
    }
    let isService = false;
    if (entity.type === ScriptAuthorityTypeEnum.findKeyBySymbol(ScriptAuthorityTypeEnum.SERVICE)) {
      isService = true;
    }
    detail.isService = isService;
    detail = {
      ...detail,
      entity,
      show: true
    };
    // set detail for modal window
    this.setState({
      detail,
      _showLoading: false
    });
  }

  render() {
    const { uiKey, rendered, scriptId, availableServices } = this.props;
    const { filterOpened, detail, _showLoading } = this.state;

    if (!rendered) {
      return null;
    }

    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          manager={manager}
          forceSearchParameters={manager.getDefaultSearchParameters().setFilter('scriptId', scriptId)}
          showRowSelection={SecurityManager.hasAuthority('SCRIPT_DELETE')}
          rowClass={({rowIndex, data}) => { return data[rowIndex].disabled ? 'disabled' : ''; }}
          _showLoading={_showLoading}
          filterOpened={!filterOpened}
          actions={
            [
              { value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }
            ]
          }
          buttons={
            [
              <Basic.Button level="success" key="add_button" className="btn-xs"
                      onClick={this.showDetail.bind(this, {})}
                      rendered={SecurityManager.hasAuthority('SCRIPT_CREATE')}>
                <Basic.Icon type="fa" icon="plus"/>
                {' '}
                {this.i18n('button.add')}
              </Basic.Button>
            ]
          }>
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
          <Advanced.Column property="type"
            header={ this.i18n('entity.ScriptAuthority.type.label') }
            sort face="enum" enumClass={ScriptAuthorityTypeEnum}/>
          <Advanced.Column property="className" sort />
          <Advanced.Column property="service" sort />
        </Advanced.Table>

        <Basic.Modal
          bsSize="medium"
          show={ detail.show }
          onHide={this.closeDetail.bind(this)}
          backdrop="static"
          keyboard={!_showLoading}>

          <form onSubmit={this.save.bind(this, {})}>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('create.header')} rendered={Utils.Entity.isNew(detail.entity)}/>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('edit.header', { name: detail.entity })} rendered={!Utils.Entity.isNew(detail.entity)}/>
            <Basic.Modal.Body>
              <Basic.AbstractForm
                ref="form"
                data={detail.entity}
                _showLoading={_showLoading}>
                <Basic.Row>
                  <div className="col-lg-12">
                    <Basic.EnumSelectBox
                      ref="type"
                      enum={ScriptAuthorityTypeEnum}
                      onChange={ this.onChangeType.bind(this) }
                      label={ this.i18n('entity.ScriptAuthority.type.label') }
                      palceholder={ this.i18n('entity.ScriptAuthority.type.placeholder') }
                      helpBlock={ this.i18n('entity.ScriptAuthority.type.help') }/>
                    {
                      detail.isService
                      ?
                      <Basic.EnumSelectBox
                        required
                        searchable
                        ref="className"
                        onChange={ this.onChangeService.bind(this) }
                        options={this.getOptions(availableServices)}
                        label={ this.i18n('entity.ScriptAuthority.type.label') }
                        palceholder={ this.i18n('entity.ScriptAuthority.type.placeholder') }
                        helpBlock={ this.i18n('entity.ScriptAuthority.type.help') }/>
                      :
                      <Basic.TextField
                        required
                        ref="className"
                        label={this.i18n('entity.ScriptAuthority.className')}
                        max={2000}/>
                    }
                    <Basic.TextField
                      hidden
                      ref="service"
                      max={2000}/>
                  </div>
                </Basic.Row>

              </Basic.AbstractForm>
            </Basic.Modal.Body>

            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={this.closeDetail.bind(this)}
                _showLoading={_showLoading}>
                {this.i18n('button.close')}
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                _showLoading={_showLoading}
                _showLoadingIcon
                _showLoadingText={this.i18n('button.saving')}
                rendered={SecurityManager.hasAuthority('SCRIPT_CREATE')}>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>
      </div>
    );
  }
}

ScriptAuthorityTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  scriptId: PropTypes.string.isRequired,
  rendered: PropTypes.bool.isRequired
};

ScriptAuthorityTable.defaultProps = {
  rendered: true
};

function select(state) {
  return {
    availableServices: DataManager.getData(state, AVAILABLE_SERVICES_UIKEY)
  };
}

export default connect(select)(ScriptAuthorityTable);
