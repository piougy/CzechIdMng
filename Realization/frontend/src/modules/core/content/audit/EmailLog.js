

import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../../../components/basic';
import * as Advanced from '../../../../components/advanced';
import { AuditLogManager, AuditLogForObjectManager, EmailLogManager } from '../../../../redux';

class EmailLog extends Basic.AbstractContent {

  constructor(props, context) {
     super(props, context);
     this.state = {
       filterOpened: true,
       showEmailDetail: false,
       emailDetail: {}
     }
     this.emailLogManager = new EmailLogManager();
  }

  getManager() {
    return this.emailLogManager;
  }

  getContentKey() {
    return 'content.audit.emailLog';
  }

  componentDidMount() {
    this.selectNavigationItem('email-log');
  }

  openEmailDetail(emailDetail) {
    this.setState({
      showEmailDetail: true,
      emailDetail: emailDetail
    });
  }

  closeEmailDetail() {
    this.setState({ showEmailDetail: false });
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

  render() {
    const { showEmailDetail, emailDetail, filterOpened } = this.state;

    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.PageHeader>
          {this.i18n('title')}
        </Basic.PageHeader>

        <Basic.Panel>
          <Advanced.Table
            ref="table"
            uiKey="email_log_table"
            manager={this.getManager()}
            filter={
              <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
                <Basic.AbstractForm ref="filterForm">
                  <Basic.Row>
                    <div className="col-lg-4">
                      <Advanced.Filter.DateTimePicker
                        mode="datetime"
                        ref="filterCreatedAtFrom"
                        field="createdAt"
                        relation="GE"
                        placeholder={this.i18n('filter.createdAtFrom.placeholder')}
                        label={this.i18n('filter.createdAtFrom.label')}/>
                    </div>
                    <div className="col-lg-4">
                      <Advanced.Filter.DateTimePicker
                        mode="datetime"
                        ref="filterCreatedAtTill"
                        field="createdAt"
                        relation="LE"
                        placeholder={this.i18n('filter.createdAtTill.placeholder')}
                        label={this.i18n('filter.createdAtTill.label')}/>
                    </div>
                    <div className="col-lg-4 text-right">
                      <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                    </div>
                  </Basic.Row>
                  <Basic.Row>
                    <div className="col-lg-4">
                      <Advanced.Filter.TextField
                        ref="email"
                        placeholder={this.i18n('entity.Identity.email')}
                        label={this.i18n('entity.EmailLog.email')}/>
                    </div>
                    <div className="col-lg-4">
                      <Advanced.Filter.TextField
                        ref="subject"
                        placeholder={this.i18n('entity.EmailLog.subject')}
                        label={this.i18n('entity.EmailLog.subject')}/>
                    </div>
                    <div className="col-lg-4">
                    </div>
                  </Basic.Row>
                  <Basic.Row className="last">
                    <div className="col-lg-4">
                      <Advanced.Filter.BooleanSelectBox
                        ref="success"
                        label={this.i18n('filter.success.label')}
                        placeholder={this.i18n('filter.success.placeholder')}/>
                    </div>
                    <div className="col-lg-4">
                    </div>
                    <div className="col-lg-4">
                    </div>
                  </Basic.Row>
                </Basic.AbstractForm>
              </Advanced.Filter>
            }
            filterOpened={filterOpened}>

            <Basic.Column
              className="detail-button"
              cell={
                ({rowIndex, data, property, ...props}) => {
                  return (
                    <Advanced.DetailButton onClick={this.openEmailDetail.bind(this, data[rowIndex])}/>
                  );
                }
              }/>
            <Advanced.Column property="createdAt" face="datetime" sort={true}/>
            <Advanced.Column property="email" sort={true}/>
            <Advanced.Column property="subject" sort={true}/>
            <Advanced.Column property="success" face="bool" sort={true} width="150px"/>
          </Advanced.Table>
        </Basic.Panel>

        <Basic.Modal
          bsSize="large"
          show={showEmailDetail}
          onHide={this.closeEmailDetail.bind(this)}>
          <Basic.Modal.Header closeButton={true} text={<span>{emailDetail.subject} <small>{this.i18n('detail.header')}</small></span>}/>
          <Basic.Modal.Body  style={{ padding: 0 }}>
            <div className="basic-table">
              <table className="table">
                <tbody>
                  <tr>
                    <td width="175px" style={{ borderTop: 0 }}>{this.i18n('entity.EmailLog.email')}</td>
                    <th style={{ borderTop: 0 }}>
                      {
                        emailDetail.email
                        ||
                        '-'
                      }
                    </th>
                  </tr>
                  <tr>
                    <td>{this.i18n('entity.EmailLog.createdAt')}</td>
                    <th> <Advanced.DateValue value={emailDetail.createdAt}/></th>
                  </tr>
                  <tr>
                    <td>
                      {this.i18n('entity.EmailLog.sent')}
                    </td>
                    <th>
                      {
                        emailDetail.success
                        ?
                        <span className="label label-success">{this.i18n('entity.EmailLog.success')}</span>
                        :
                        <span className="label label-danger">{this.i18n('entity.EmailLog.failed')}</span>
                      }
                    </th>
                  </tr>
                </tbody>
              </table>
            </div>
            <div style={{ margin: 15 }}>
              <h3>{ this.i18n('entity.EmailLog.text') }</h3>
              <textarea disabled style={{ width: '100%', minHeight: '250px', border: '1px solid #ddd', resize: 'vertical' }}>{emailDetail.text}</textarea>
            </div>
          </Basic.Modal.Body>
          <Basic.Modal.Footer>
            <Basic.Button level="link" onClick={this.closeEmailDetail.bind(this)} >{this.i18n('button.close')}</Basic.Button>
          </Basic.Modal.Footer>
        </Basic.Modal>

      </div>
    );
  }
}

EmailLog.propTypes = {
}
EmailLog.defaultProps = {
}

function select(state) {
  return {};
}

export default connect(select)(EmailLog)
