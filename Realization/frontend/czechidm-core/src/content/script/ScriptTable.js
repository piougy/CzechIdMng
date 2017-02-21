import React, { PropTypes } from 'react';
import uuid from 'uuid';
//
import { SecurityManager } from '../../redux';
import ScriptCategoryEnum from '../../enums/ScriptCategoryEnum';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';

const MAX_DESCRIPTION_LENGTH = 60;

/**
 * Table with definitions of scripts
 */
export default class ScriptTable extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    // default filter status
    // true - open
    // false - close
    this.state = {
      filterOpened: false
    };
  }

  getContentKey() {
    return 'content.scripts';
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
    const { uiKey, scriptManager } = this.props;
    const selectedEntities = scriptManager.getEntitiesByIds(this.context.store.getState(), selectedRows);
    //
    // show confirm message for deleting entity or entities
    this.refs['confirm-' + bulkActionValue].show(
      this.i18n(`action.${bulkActionValue}.message`, { count: selectedEntities.length, record: scriptManager.getNiceLabel(selectedEntities[0]), records: scriptManager.getNiceLabels(selectedEntities).join(', ') }),
      this.i18n(`action.${bulkActionValue}.header`, { count: selectedEntities.length, records: scriptManager.getNiceLabels(selectedEntities).join(', ') })
    ).then(() => {
      // try delete
      this.context.store.dispatch(scriptManager.deleteEntities(selectedEntities, uiKey, (entity, error, successEntities) => {
        if (entity && error) {
          this.addErrorMessage({ title: this.i18n(`action.delete.error`, { record: scriptManager.getNiceLabel(entity) }) }, error);
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
   * Recive new form for create new type else show detail for existing org.
   */
  showDetail(entity, event) {
    if (event) {
      event.preventDefault();
    }
    // when create new script is generate non existing id
    // and set parameter new to 1 (true)
    // this is necessary for ScriptDetail
    if (entity.id === undefined) {
      const uuidId = uuid.v1();
      this.context.router.push(`/scripts/${uuidId}?new=1`);
    } else {
      this.context.router.push('/scripts/' + entity.id);
    }
  }

  render() {
    const { uiKey, scriptManager } = this.props;
    const { filterOpened } = this.state;

    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          manager={scriptManager}
          showRowSelection={SecurityManager.hasAuthority('SCRIPT_DELETE')}
          rowClass={({rowIndex, data}) => { return data[rowIndex].disabled ? 'disabled' : ''; }}
          filter={
            <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row>
                  <div className="col-lg-6">
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={this.i18n('filter.text')}/>
                  </div>
                  <div className="col-lg-6 text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                  </div>
                </Basic.Row>
                <Basic.Row className="last">
                  <div className="col-lg-6">
                    <Basic.EnumSelectBox
                      ref="category"
                      placeholder={this.i18n('entity.Script.category')}
                      enum={ScriptCategoryEnum}/>
                  </div>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
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
                      rendered={SecurityManager.hasAuthority('SCRIPT_WRITE')}>
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
          <Advanced.Column property="name" sort />
          <Advanced.Column property="category" sort face="enum" enumClass={ScriptCategoryEnum}/>
          <Advanced.Column property="description" cell={ ({ rowIndex, data }) => {
            if (data[rowIndex]) {
              const description = data[rowIndex].description.replace(/<(?:.|\n)*?>/gm, '').substr(0, MAX_DESCRIPTION_LENGTH);
              return description.substr(0, Math.min(description.length, description.lastIndexOf(' ')));
            }
            return '';
          }}/>
        </Advanced.Table>
      </div>
    );
  }
}

ScriptTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  scriptManager: PropTypes.object.isRequired
};

ScriptTable.defaultProps = {
};
