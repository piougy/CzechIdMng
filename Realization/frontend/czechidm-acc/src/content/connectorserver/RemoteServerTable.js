import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import { Advanced, Basic, Utils } from 'czechidm-core';
import uuid from 'uuid';
import { RemoteServerManager } from '../../redux';

const manager = new RemoteServerManager();

/**
* Remote server with connectors.
*
* @author Radek Tomiška
* @since 10.8.0
*/
export class RemoteServerTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: this.props.filterOpened,
    };
  }

  getContentKey() {
    return 'acc:content.remote-servers';
  }

  getManager() {
    return manager;
  }

  componentDidMount() {
    super.componentDidMount();
    //
    if (this.refs.text) {
      this.refs.text.focus();
    }
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.useFilterForm(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.cancelFilter(this.refs.filterForm);
  }

  showDetail(entity) {
    if (Utils.Entity.isNew(entity)) {
      const uuidId = uuid.v1();
      this.context.store.dispatch(this.getManager().receiveEntity(uuidId, entity));
      this.context.history.push(`/remote-servers/${ uuidId }/new?new=1`);
    } else {
      this.context.history.push(`/remote-servers/${ entity.id }/detail`);
    }
  }

  render() {
    const { filterOpened } = this.state;
    //
    return (
      <Basic.Div>
        <Advanced.Table
          ref="table"
          uiKey={ this.getUiKey() }
          manager={ this.getManager() }
          showRowSelection
          filter={
            <Advanced.Filter onSubmit={ this.useFilter.bind(this) }>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row className="last">
                  <Basic.Col lg={ 6 }>
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={ this.i18n('filter.text.placeholder') }
                      help={ Advanced.Filter.getTextHelp() }/>
                  </Basic.Col>
                  <Basic.Col lg={ 6 } className="text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={ this.cancelFilter.bind(this) }/>
                  </Basic.Col>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          buttons={
            [
              <Basic.Button
                level="success"
                key="add_button"
                className="btn-xs"
                onClick={this.showDetail.bind(this, { useSsl: true })}
                rendered={ this.getManager().canSave() }
                icon="fa:plus">
                { this.i18n('button.add') }
              </Basic.Button>
            ]
          }
          filterOpened={ filterOpened }
          _searchParameters={ this.getSearchParameters() }>
          <Advanced.Column
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={ this.i18n('button.detail') }
                    onClick={ this.showDetail.bind(this, data[rowIndex]) }/>
                );
              }
            }/>
          <Advanced.Column property="host" sort/>
          <Advanced.Column property="port" sort/>
          <Advanced.Column property="useSsl" face="bool" sort/>
          <Advanced.Column property="timeout" sort/>
          <Advanced.Column
            property="defaultRemoteServer"
            header={ this.i18n('acc:entity.RemoteServer.defaultRemoteServer.short') }
            face="bool"
            rendered={ false } />
          <Advanced.Column
            property="description"
            header={ this.i18n('entity.description.label') }
            sort/>
        </Advanced.Table>
      </Basic.Div>
    );
  }
}

RemoteServerTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  filterOpened: PropTypes.bool
};

RemoteServerTable.defaultProps = {
  filterOpened: true
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select, null, null, { forwardRef: true })(RemoteServerTable);
