import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../components/basic';
import * as Advanced from '../components/advanced';
import { ConfigurationManager, DataManager, SecurityManager } from '../redux';
import * as Utils from '../utils';

const uiKey = 'configuration_table';
const manager = new ConfigurationManager();

/**
 * Application configurations
 *
 * @author Radek Tomiška
 */
class Configurations extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: false,
      detail: {
        show: false,
        entity: {}
      },
      isGuarded: false,
      isSecured: false
    };
  }

  componentDidMount() {
    this.selectNavigationItem('system-configuration');
    this.context.store.dispatch(manager.fetchAllConfigurationsFromFile());
    if (SecurityManager.hasAuthority('CONFIGURATION_ADMIN')) {
      this.context.store.dispatch(manager.fetchAllConfigurationsFromEnvironment());
    }
  }

  getManager() {
    return manager;
  }

  getUiKey() {
    return uiKey;
  }

  getContentKey() {
    return 'content.configuration';
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

  showDetail(entity) {
    const isGuarded = this._getGuarded(entity.confidential, entity.name);
    const isSecured = isGuarded || manager.shouldBeSecured(entity.name);
    //
    if (!Utils.Entity.isNew(entity)) {
      this.context.store.dispatch(this.getManager().fetchPermissions(entity.id, `${this.getUiKey()}-detail`));
    }
    //
    this.setState({
      detail: {
        show: true,
        showLoading: false,
        entity
      },
      isGuarded,
      isSecured
    }, () => {
      this.refs.name.focus();
    });
  }

  closeDetail() {
    this.setState({
      detail: {
        show: false,
        entity: {}
      }
    });
  }

  save(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const entity = this.refs.form.getData();
    //
    if (entity.id === undefined) {
      this.context.store.dispatch(this.getManager().createEntity(entity, `${uiKey}-detail`, (createdEntity, error) => {
        this._afterSave(createdEntity, error);
        if (!error) {
          this.refs.table.getWrappedInstance().reload();
        }
      }));
    } else {
      this.context.store.dispatch(this.getManager().updateEntity(entity, `${uiKey}-detail`, this._afterSave.bind(this)));
    }
  }

  _afterSave(entity, error) {
    if (error) {
      this.refs.form.processEnded();
      this.addError(error);
      return;
    }
    this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
    this.closeDetail();
    // reload public configurations
    this.context.store.dispatch(this.getManager().fetchPublicConfigurations());
  }

  onDelete(bulkActionValue, selectedRows) {
    const selectedEntities = this.getManager().getEntitiesByIds(this.context.store.getState(), selectedRows);
    //
    this.refs['confirm-' + bulkActionValue].show(
      this.i18n(`action.${bulkActionValue}.message`, { count: selectedEntities.length, record: this.getManager().getNiceLabel(selectedEntities[0]), records: this.getManager().getNiceLabels(selectedEntities).join(', ') }),
      this.i18n(`action.${bulkActionValue}.header`, { count: selectedEntities.length, records: this.getManager().getNiceLabels(selectedEntities).join(', ') })
    ).then(() => {
      this.context.store.dispatch(this.getManager().deleteEntities(selectedEntities, uiKey, (entity, error) => {
        if (entity && error) {
          this.addErrorMessage({ title: this.i18n(`action.delete.error`, { record: this.getManager().getNiceLabel(entity) }) }, error);
        } else {
          this.refs.table.getWrappedInstance().reload();
          // reload public configurations
          this.context.store.dispatch(this.getManager().fetchPublicConfigurations());
        }
      }));
    }, () => {
      // nothing
    });
  }

  _changeName(event) {
    // check guarded depents on new entity name
    const confidential = this.refs.confidential.getValue();
    const name = event.currentTarget.value;
    //
    this._setForceProperties(confidential, name);
  }

  _changeConfidential(event) {
    const confidential = event.currentTarget.checked;
    const name = this.refs.name.getValue();
    //
    this._setForceProperties(confidential, name);
  }

  _setForceProperties(confidential, entityName) {
    const prevGuarded = this.state.isGuarded;
    const isGuarded = this._getGuarded(confidential, entityName);
    const isSecured = isGuarded || manager.shouldBeSecured(entityName);
    this.setState({
      isGuarded,
      isSecured
    }, () => {
      if (isGuarded) {
        this.refs.confidential.setValue(true);
      } else if (prevGuarded) {
        this.refs.value.setValue(null);
      }
      //
      if (isSecured) {
        this.refs.public.setValue(false);
      }
    });
  }

  _getGuarded(confidential, entityName) {
    if (manager.shouldBeGuarded(entityName)) {
      return 'by_name'; // has higher priority
    }

    if (confidential) {
      return 'by_confidential';
    }
    return false;
  }

  render() {
    const {
      _showLoading,
      fileConfigurations,
      _fileConfigurationsShowLoading,
      environmentConfigurations,
      _environmentConfigurationsShowLoading,
      _permissions
    } = this.props;
    const { filterOpened, detail, isGuarded, isSecured } = this.state;

    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.PageHeader>
          <Basic.Icon value="cog"/>
          {' '}
          {this.i18n('header')}
        </Basic.PageHeader>

        <Basic.Panel>
          <Advanced.Table
            ref="table"
            uiKey={ uiKey }
            manager={ this.getManager()}
            showRowSelection={ manager.canDelete() }
            filter={
              <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
                <Basic.AbstractForm ref="filterForm">
                  <Basic.Row className="last">
                    <div className="col-lg-4">
                      <Advanced.Filter.TextField
                        ref="text"
                        placeholder={this.i18n('entity.Configuration.name')}/>
                    </div>
                    <div className="col-lg-4">
                    </div>
                    <div className="col-lg-4 text-right">
                      <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                    </div>
                  </Basic.Row>
                </Basic.AbstractForm>
              </Advanced.Filter>
            }
            filterOpened={ filterOpened }
            actions={
              [{ value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }]
            }
            buttons={
              [
                <Basic.Button
                  level="success"
                  key="add_button"
                  className="btn-xs"
                  onClick={this.showDetail.bind(this, { public: true })}
                  rendered={ manager.canSave() }>
                  <Basic.Icon type="fa" icon="plus"/>
                  {' '}
                  {this.i18n('button.add')}
                </Basic.Button>
              ]
            }>
            <Advanced.Column
              property=""
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
              }/>
            <Advanced.Column property="name" sort width="250px"/>
            <Advanced.Column property="value" sort/>
            <Advanced.Column property="confidential" sort face="bool" width="150px"/>
            <Advanced.Column property="public" face="bool" width="150px"/>
          </Advanced.Table>
        </Basic.Panel>

        <Basic.Modal
          bsSize="large"
          show={detail.show}
          onHide={this.closeDetail.bind(this)}
          backdrop="static"
          keyboard={!_showLoading}>

          <form onSubmit={this.save.bind(this)}>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('create.header')} rendered={detail.entity.id === undefined}/>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('edit.header', { name: detail.entity.name })} rendered={detail.entity.id !== undefined}/>
            <Basic.Modal.Body>
              <Basic.AbstractForm
                ref="form"
                data={ detail.entity }
                showLoading={ _showLoading }
                readOnly={ !manager.canSave(detail.entity, _permissions) }>
                <Basic.TextField
                  ref="name"
                  label={this.i18n('entity.Configuration.name')}
                  onChange={this._changeName.bind(this)}
                  required
                  helpBlock={<span dangerouslySetInnerHTML={{ __html: this.i18n('guarded', { guarded: ConfigurationManager.GUARDED_PROPERTY_NAMES.join(', ') }) }}/>}/>
                <Basic.TextField
                  type={isGuarded ? 'password' : 'text'}
                  ref="value"
                  label={this.i18n('entity.Configuration.value')}
                  confidential={isGuarded !== false}/>
                <Basic.Checkbox
                  ref="confidential"
                  label={this.i18n('entity.Configuration.confidential')}
                  helpBlock={this.i18n('confidential.help')}
                  onChange={this._changeConfidential.bind(this)}
                  readOnly={isGuarded === 'by_name'}>
                </Basic.Checkbox>
                <Basic.Checkbox
                  ref="public"
                  label={this.i18n('entity.Configuration.public')}
                  readOnly={ isSecured }
                  helpBlock={this.i18n('secured.help')}>
                </Basic.Checkbox>
              </Basic.AbstractForm>
            </Basic.Modal.Body>

            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={ this.closeDetail.bind(this) }
                showLoading={ _showLoading }>
                {this.i18n('button.close')}
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoading={ _showLoading }
                showLoadingIcon
                showLoadingText={ this.i18n('button.saving') }
                rendered={ manager.canSave(detail.entity, _permissions) }>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>

        <Basic.ContentHeader>
          <Basic.Icon value="cog"/>
          {' '}
          <span dangerouslySetInnerHTML={{ __html: this.i18n('fromFile') }}/>
        </Basic.ContentHeader>

        <Basic.Panel>
          <Basic.Table
            data={fileConfigurations}
            showLoading={_fileConfigurationsShowLoading}
            noData={this.i18n('component.basic.Table.noData')}>
            <Basic.Column property="name" header={this.i18n('entity.Configuration.name')} width="250px"/>
            <Basic.Column property="value" header={this.i18n('entity.Configuration.value')} />
            <Basic.Column
              property="confidential"
              header={<Basic.Cell className="column-face-bool">{this.i18n('entity.Configuration.confidential')}</Basic.Cell>}
              cell={<Basic.BooleanCell className="column-face-bool"/>}
              width="150px"/>
            <Basic.Column
              property="public"
              header={<Basic.Cell className="column-face-bool">{this.i18n('entity.Configuration.public')}</Basic.Cell>}
              cell={<Basic.BooleanCell className="column-face-bool"/>}
              width="150px"/>
          </Basic.Table>
        </Basic.Panel>

        {
          !SecurityManager.hasAuthority('CONFIGURATION_ADMIN')
          ||
          <div>
            <Basic.ContentHeader>
              <Basic.Icon value="cog"/>
              {' '}
              <span dangerouslySetInnerHTML={{ __html: this.i18n('fromEnvironment') }}/>
            </Basic.ContentHeader>

            <Basic.Panel>
              <Basic.Table
                data={environmentConfigurations}
                showLoading={_environmentConfigurationsShowLoading}
                noData={this.i18n('component.basic.Table.noData')}>
                <Basic.Column property="name" header={this.i18n('entity.Configuration.name')} width="150px"/>
                <Basic.Column property="value" header={this.i18n('entity.Configuration.value')}/>
              </Basic.Table>
            </Basic.Panel>
          </div>
        }
      </div>
    );
  }
}

Configurations.propTypes = {
  fileConfigurations: PropTypes.arrayOf(PropTypes.object),
  environmentConfigurations: PropTypes.arrayOf(PropTypes.object),
  _permissions: PropTypes.arrayOf(PropTypes.string)
};

Configurations.defaultProps = {
  fileConfigurations: [],
  _showLoading: false,
  _fileConfigurationsShowLoading: false,
  _environmentConfigurationsShowLoading: false,
  _permissions: null
};

function select(state) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, uiKey),
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
    fileConfigurations: DataManager.getData(state, ConfigurationManager.FILE_CONFIGURATIONS),
    _fileConfigurationsShowLoading: Utils.Ui.isShowLoading(state, ConfigurationManager.FILE_CONFIGURATIONS),
    environmentConfigurations: DataManager.getData(state, ConfigurationManager.ENVIRONMENT_CONFIGURATIONS),
    _environmentConfigurationsShowLoading: Utils.Ui.isShowLoading(state, ConfigurationManager.ENVIRONMENT_CONFIGURATIONS),
    _permissions: Utils.Permission.getPermissions(state, `${uiKey}-detail`)
  };
}

export default connect(select)(Configurations);
