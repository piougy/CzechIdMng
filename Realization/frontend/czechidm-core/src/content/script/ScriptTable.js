import PropTypes from 'prop-types';
import React from 'react';
import uuid from 'uuid';
import { connect } from 'react-redux';
//
import * as Utils from '../../utils';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { SecurityManager } from '../../redux';
import ScriptCategoryEnum from '../../enums/ScriptCategoryEnum';

const MAX_DESCRIPTION_LENGTH = 60;

/**
 * Table with definitions of scripts
 *
 * @author Ondřej Kopr
 * @author Radek Tomiška
 */
export class ScriptTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    // default filter status
    // true - open
    // false - close
    this.state = {
      filterOpened: props.filterOpened
    };
  }

  componentDidMount() {
    super.componentDidMount();
    //
    this.refs.text.focus();
  }

  getManager() {
    return this.props.scriptManager;
  }

  getContentKey() {
    return 'content.scripts';
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
      this.context.history.push(`/scripts/${uuidId}/new?new=1`);
    } else {
      this.context.history.push(`/scripts/${entity.id}/detail`);
    }
  }

  /**
   * Dropzone component function called after select file
   * @param  {array} files Array of selected files
   */
  _onDrop(files) {
    if (this.refs.dropzone.state.isDragReject) {
      this.addMessage({
        message: this.i18n('filesRejected'),
        level: 'warning'
      });
      return;
    }
    files.forEach((file) => {
      const fileName = file.name.toLowerCase();
      if (!fileName.endsWith('.zip') && !fileName.endsWith('.xml')) {
        this.addMessage({
          message: this.i18n('fileRejected', {name: file.name}),
          level: 'warning'
        });
        return;
      }
      this.setState({
        showLoading: true
      }, () => {
        const formData = new FormData();
        formData.append('name', file.name);
        formData.append('fileName', file.name);
        formData.append('data', file);
        //
        this.getManager().getService().deploy(formData)
          .then(() => {
            this.setState({
              showLoading: false
            }, () => {
              this.addMessage({
                message: this.i18n('fileUploaded', { name: file.name })
              });
              this.refs.table.reload();
            });
          })
          .catch(error => {
            this.setState({
              showLoading: false
            });
            this.addError(error);
          });
      });
    });
  }

  render() {
    const { uiKey, scriptManager, disableAdd, forceSearchParameters, className } = this.props;
    const { filterOpened, showLoading } = this.state;
    //
    return (
      <Basic.Div>
        <Basic.Panel style={{ margin: '5px 5px 0 5px' }} rendered={ !disableAdd }>
          <Basic.Dropzone
            ref="dropzone"
            accept=".zip,text/xml"
            onDrop={ this._onDrop.bind(this) }>
            { this.i18n('dropzone.infoText') }
          </Basic.Dropzone>
        </Basic.Panel>
        <Advanced.Table
          ref="table"
          uiKey={ uiKey }
          manager={ scriptManager }
          showRowSelection
          showLoading={ showLoading }
          rowClass={({rowIndex, data}) => { return data[rowIndex].disabled ? 'disabled' : ''; }}
          forceSearchParameters={ forceSearchParameters }
          filter={
            <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row>
                  <Basic.Col lg={ 6 }>
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={this.i18n('filter.text')}/>
                  </Basic.Col>
                  <Basic.Col lg={ 6 } className="text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                  </Basic.Col>
                </Basic.Row>
                <Basic.Row className="last">
                  <Basic.Col lg={ 6 }>
                    <Advanced.Filter.TextField
                      ref="code"
                      placeholder={this.i18n('entity.Script.code')}/>
                  </Basic.Col>
                  <Basic.Col lg={ 6 }>
                    <Advanced.Filter.EnumSelectBox
                      ref="inCategory"
                      placeholder={ this.i18n('entity.Script.category') }
                      enum={ ScriptCategoryEnum }
                      multiSelect/>
                  </Basic.Col>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          filterOpened={ filterOpened }
          buttons={
            [
              <Basic.Button
                level="success"
                key="add_button"
                className="btn-xs"
                hidden={ disableAdd }
                showLoading={ showLoading }
                onClick={ this.showDetail.bind(this, {}) }
                rendered={ SecurityManager.hasAuthority('SCRIPT_CREATE') }
                icon="fa:plus">
                { this.i18n('button.add') }
              </Basic.Button>
            ]
          }
          _searchParameters={ this.getSearchParameters() }
          className={ className }>
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
            }
            sort={ false }/>
          <Advanced.ColumnLink to="/scripts/:id/detail" property="code" sort />
          <Advanced.Column property="name" sort />
          <Advanced.Column property="category" sort face="enum" enumClass={ ScriptCategoryEnum }/>
          <Advanced.Column
            property="description"
            cell={ ({ rowIndex, data }) => {
              if (data[rowIndex] && data[rowIndex].description !== null) {
                const description = data[rowIndex].description.replace(/<(?:.|\n)*?>/gm, '').substr(0, MAX_DESCRIPTION_LENGTH);
                return description.substr(0, Math.min(description.length, description.lastIndexOf(' ')));
              }
              return '';
            }}/>
        </Advanced.Table>
      </Basic.Div>
    );
  }
}

ScriptTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  scriptManager: PropTypes.object.isRequired,
  disableAdd: PropTypes.boolean,
  forceSearchParameters: PropTypes.object
};

ScriptTable.defaultProps = {
  disableAdd: false
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select)(ScriptTable);
