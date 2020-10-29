import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import uuid from 'uuid';
//
import { SecurityManager } from '../../../redux';
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import * as Utils from '../../../utils';

/**
 * Table with notification templates.
 *
 * @author Ondřej Kopr
 * @author Radek Tomiška
 */
export class TemplateTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: true
    };
  }

  componentDidMount() {
    super.componentDidMount();
    //
    this.refs.text.focus();
  }

  getContentKey() {
    return 'content.notificationTemplate';
  }

  getManager() {
    return this.props.manager;
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }

    this.refs.table.useFilterData(this.refs.filterForm.getData());
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.cancelFilter(this.refs.filterForm);
  }

  /**
   * Recive new form for create new type else show detail for existing notification templates
   */
  showDetail(entity, event) {
    if (event) {
      event.preventDefault();
    }
    if (entity.id === undefined) {
      const uuidId = uuid.v1();
      this.context.history.push(`/notification/templates/${ uuidId }?new=1`);
    } else {
      this.context.history.push(`/notification/templates/${ entity.id }`);
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
    const { uiKey, manager } = this.props;
    const { filterOpened, showLoading } = this.state;
    //
    return (
      <Basic.Div>
        <Basic.Panel style={{ margin: '5px 5px 0 5px' }}>
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
          manager={ manager }
          showRowSelection
          showLoading={ showLoading }
          rowClass={({ rowIndex, data }) => { return data[rowIndex].disabled ? 'disabled' : ''; }}
          filter={
            <Advanced.Filter onSubmit={ this.useFilter.bind(this) }>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row className="last">
                  <Basic.Col lg={ 6 }>
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={ this.i18n('filter.text') }/>
                  </Basic.Col>
                  <Basic.Col lg={ 6 } className="text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                  </Basic.Col>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          filterOpened={filterOpened}
          buttons={
            [
              <Basic.Button
                level="success"
                key="add_button"
                className="btn-xs"
                showLoading={ showLoading }
                onClick={ this.showDetail.bind(this, {}) }
                rendered={ SecurityManager.hasAuthority('NOTIFICATIONTEMPLATE_CREATE') }
                icon="fa:plus">
                { this.i18n('button.add') }
              </Basic.Button>
            ]
          }
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
            }
            sort={false}/>
          <Advanced.Column property="code" width={ 125 } sort/>
          <Advanced.Column property="name" sort/>
          <Advanced.Column property="module" width={ 125 } sort/>
          <Advanced.Column property="subject" sort/>
          <Advanced.Column
            property="unmodifiable"
            header={ this.i18n('entity.NotificationTemplate.unmodifiable.name') }
            face="bool"
            sort />
        </Advanced.Table>
      </Basic.Div>
    );
  }
}

TemplateTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  passwordPolicyManager: PropTypes.object.isRequired
};

TemplateTable.defaultProps = {
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select)(TemplateTable);
