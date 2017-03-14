import React, { PropTypes } from 'react';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';


/**
* Table of forms definitions (attributes is show in detail)
*/
export default class FormTable extends Basic.AbstractContent {

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
    return 'content.formDefinitions';
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
    // form definitions can't be created
    this.context.router.push('/forms/' + entity.id);
  }

  /**
  * Method get last string of split string by dot.
  * Used for get niceLabel for type entity.
  */
  _getType(name) {
    const type = name.split('.');
    return type[type.length - 1];
  }

  render() {
    const { uiKey, definitionManager } = this.props;
    const { filterOpened } = this.state;
    return (
      <Basic.Row>
        <div className="col-lg-12">
          <Advanced.Table
            ref="table"
            uiKey={uiKey}
            manager={definitionManager}
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
                  <Basic.Row>
                    <div className="col-lg-6">

                    </div>
                  </Basic.Row>
                </Basic.AbstractForm>
              </Advanced.Filter>
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
            <Advanced.Column property="type" sort
              face="text" width="75px"
              cell={
                ({ rowIndex, data, property }) => {
                  return this._getType(data[rowIndex][property]);
                }}/>
            <Advanced.Column property="name" sort/>
          </Advanced.Table>
        </div>
      </Basic.Row>
      );
  }
}

FormTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  definitionManager: PropTypes.object.isRequired,
  filterOpened: PropTypes.bool
};

FormTable.defaultProps = {
  filterOpened: true,
};
