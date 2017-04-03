import React, { PropTypes } from 'react';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
//
import { SecurityManager, FormAttributeManager } from '../../redux';
import uuid from 'uuid';
import SearchParameters from '../../domain/SearchParameters';

const attributeManager = new FormAttributeManager();
/**
* Table of forms attributes
*/
export default class FormAttributeTable extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: props.filterOpened,
      showLoading: true
    };
  }

  componentDidMount() {
  }

  getContentKey() {
    return 'content.formAttributes';
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().useFilterForm(this.refs.filterForm);
  }

  onDelete(bulkActionValue, selectedRows) {
    const { uiKey } = this.props;
    const selectedEntities = attributeManager.getEntitiesByIds(this.context.store.getState(), selectedRows);
    //
    // show confirm message for deleting entity or entities
    this.refs['confirm-' + bulkActionValue].show(
      this.i18n(`action.${bulkActionValue}.message`, { count: selectedEntities.length, record: attributeManager.getNiceLabel(selectedEntities[0]), records: attributeManager.getNiceLabels(selectedEntities).join(', ') }),
      this.i18n(`action.${bulkActionValue}.header`, { count: selectedEntities.length, records: attributeManager.getNiceLabels(selectedEntities).join(', ') })
    ).then(() => {
      // try delete
      this.context.store.dispatch(attributeManager.deleteEntities(selectedEntities, uiKey, (entity, error, successEntities) => {
        if (entity && error) {
          this.addErrorMessage({ title: this.i18n(`action.delete.error`, { record: attributeManager.getNiceLabel(entity) }) }, error);
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

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  showDetail(entity) {
    const { formDefinitionId } = this.props;
    if (entity.id === undefined) {
      const uuidId = uuid.v1();
      this.context.router.push(`/forms/attribute/${uuidId}?new=1&formDefinition=${formDefinitionId}`);
    } else {
      this.context.router.push('/forms/attribute/' + entity.id);
    }
  }

  render() {
    const { uiKey, formDefinitionId } = this.props;
    const { filterOpened } = this.state;

    return (
      <Basic.Row>
        <div className="col-lg-12">
          <Basic.Confirm ref="confirm-delete" level="danger"/>
          <Advanced.Table
            ref="table"
            uiKey={uiKey}
            showRowSelection={SecurityManager.hasAuthority('EAVFORMATTRIBUTES_DELETE')}
            manager={attributeManager}
            forceSearchParameters={
              new SearchParameters()
              .setFilter('formDefinitionId', formDefinitionId)
              .setSort('seq', 'ASC')}
            rowClass={({rowIndex, data}) => { return data[rowIndex].disabled ? 'disabled' : ''; }}
            filter={
              <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
                <Basic.AbstractForm ref="filterForm">
                  <Basic.Row>
                    <div className="col-lg-6">
                      <Advanced.Filter.TextField
                        ref="name"
                        placeholder={this.i18n('filter.name')}/>
                    </div>
                    <div className="col-lg-6 text-right">
                      <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                    </div>
                  </Basic.Row>
                  <Basic.Row>
                    <div className="col-lg-6">

                    </div>
                  </Basic.Row>
                </Basic.AbstractForm>
              </Advanced.Filter>
            }
            actions={
              [
                { value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }
              ]
            }
            buttons={
              [
                <Basic.Button
                  level="success"
                  key="add_button"
                  className="btn-xs"
                  onClick={this.showDetail.bind(this, { })}
                  rendered={SecurityManager.hasAuthority('EAVFORMATTRIBUTES_CREATE')}>
                  <Basic.Icon type="fa" icon="plus"/>
                  {' '}
                  {this.i18n('button.add')}
                </Basic.Button>
              ]
            }
            filterOpened={!filterOpened}>
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
            <Advanced.Column property="seq" sort width="5%"/>
            <Advanced.Column property="displayName" sort/>
            <Advanced.Column property="persistentType" sort />
            <Advanced.Column property="unmodifiable" header={this.i18n('entity.FormAttribute.unmodifiable.label')} face="bool" sort />
          </Advanced.Table>
        </div>
      </Basic.Row>
      );
  }
}

FormAttributeTable.propTypes = {
  filterOpened: PropTypes.bool,
  uiKey: PropTypes.string,
  formDefinitionId: PropTypes.string.isRequired
};

FormAttributeTable.defaultProps = {
  filterOpened: true,
  uiKey: 'form-attributes-table'
};
