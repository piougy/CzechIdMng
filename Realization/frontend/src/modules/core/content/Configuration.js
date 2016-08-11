import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from 'app/components/basic';
import * as Advanced from 'app/components/advanced';
import { ConfigurationManager } from 'core/redux';
import * as Utils from 'core/utils';

const uiKey = 'configuration_table';

class Configuration extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: false,
      detail: {
        show: false,
        entity: {}
      }
    };
    this.configurationManager = new ConfigurationManager();
  }

  componentDidMount() {
    this.selectNavigationItem('system-configuration');
  }

  getManager() {
    return this.configurationManager;
  }

  getContentKey() {
    return 'content.configuration';
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().useFilter(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  showDetail(entity) {
    this.setState({
      detail: {
        show: true,
        showLoading: false,
        entity
      }
    }, () => {
      this.refs.form.setData(entity);
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
      this.context.store.dispatch(this.getManager().patchEntity(entity, `${uiKey}-detail`, this._afterSave.bind(this)));
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
  }

  onDelete(bulkActionValue, selectedRows) {
    const selectedEntities = this.getManager().getEntitiesByIds(this.context.store.getState(), selectedRows);
    //
    this.refs['confirm-' + bulkActionValue].show(
      this.i18n(`action.${bulkActionValue}.message`, { count: selectedEntities.length, record: this.getManager().getNiceLabel(selectedEntities[0]), records: this.getManager().getNiceLabels(selectedEntities).join(', ') }),
      this.i18n(`action.${bulkActionValue}.header`, { count: selectedEntities.length, records: this.getManager().getNiceLabels(selectedEntities).join(', ') })
    ).then(() => {
      this.context.store.dispatch(this.getManager().deleteEntities(selectedEntities, uiKey, () => {
        this.refs.table.getWrappedInstance().reload();
      }));
    }, () => {
      // nothing
    });
  }

  render() {
    const { _showLoading } = this.props;
    const { filterOpened, detail } = this.state;

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
            uiKey={uiKey}
            manager={this.getManager()}
            showRowSelection
            filter={
              <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
                <Basic.AbstractForm ref="filterForm" className="form-horizontal">
                  <Basic.Row className="last">
                    <div className="col-lg-4">
                      <Advanced.Filter.TextField
                        ref="text"
                        placeholder={this.i18n('entity.Configuration.name')}
                        label={this.i18n('entity.Configuration.name')}/>
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
            filterOpened={filterOpened}
            actions={
              [
                { value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }
              ]
            }
            buttons={
              [
                <Basic.Button level="success" key="add_button" className="btn-xs" onClick={this.showDetail.bind(this, {})}>
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
              }/>
            <Advanced.Column property="name" sort/>
            <Advanced.Column property="value" sort/>
            <Advanced.Column property="secured" sort face="bool"/>
          </Advanced.Table>
        </Basic.Panel>

        <Basic.Modal
          bsSize="default"
          show={detail.show}
          onHide={this.closeDetail.bind(this)}
          backdrop="static"
          keyboard={!_showLoading}>

          <form onSubmit={this.save.bind(this)}>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('create.header')} rendered={detail.entity.id === undefined}/>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('edit.header', { name: detail.entity.name })} rendered={detail.entity.id !== undefined}/>
            <Basic.Modal.Body>
              <Basic.AbstractForm ref="form" showLoading={_showLoading} className="form-horizontal">
                <Basic.TextField
                  ref="name"
                  label={this.i18n('entity.Configuration.name')}
                  required/>
                <Basic.TextArea
                  ref="value"
                  label={this.i18n('entity.Configuration.value')}/>
                <Basic.Checkbox
                  ref="secured"
                  label={this.i18n('entity.Configuration.secured')}/>
              </Basic.AbstractForm>
            </Basic.Modal.Body>

            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={this.closeDetail.bind(this)}
                showLoading={_showLoading}>
                {this.i18n('button.close')}
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoading={_showLoading}
                showLoadingIcon
                showLoadingText={this.i18n('button.saving')}>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>
      </div>
    );
  }
}

Configuration.propTypes = {
};

Configuration.defaultProps = {
  _showLoading: false
};

function select(state) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, uiKey),
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`)
  };
}

export default connect(select)(Configuration);
