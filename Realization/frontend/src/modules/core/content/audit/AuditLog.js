

import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../../../components/basic';
import * as Advanced from '../../../../components/advanced';
import { AuditLogManager, AuditLogForObjectManager, EmailLogManager } from '../../../../redux';
import OperationEnum from '../../enums/OperationEnum';
import OperationResultEnum from '../../enums/OperationResultEnum';

class AuditLog extends Basic.AbstractContent {

  constructor(props, context) {
     super(props, context);
     this.state = {
       filterOpened: true
     }
     this.auditLogManager = new AuditLogManager();
  }

  getManager() {
    return this.auditLogManager;
  }

  getContentKey() {
    return 'content.audit.log';
  }
 
  componentDidMount() {
    this.selectNavigationItem('audit-log');
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
          {this.i18n('header')}
        </Basic.PageHeader>

        <Basic.Panel>

          <Advanced.Table
            ref="table"
            uiKey="audit_log_table"
            manager={this.getManager()}
            filter={
              <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
                <Basic.AbstractForm ref="filterForm">
                  <Basic.Row>
                    <div className="col-lg-4">
                      <Advanced.Filter.DateTimePicker
                        mode="datetime"
                        ref="filterDateFrom"
                        field="date"
                        relation="GE"
                        placeholder={this.i18n('filter.dateFrom.placeholder')}
                        label={this.i18n('filter.dateFrom.label')}/>
                    </div>
                    <div className="col-lg-4">
                      <Advanced.Filter.DateTimePicker
                        mode="datetime"
                        ref="filterDateTill"
                        field="date"
                        relation="LE"
                        placeholder={this.i18n('filter.dateTill.placeholder')}
                        label={this.i18n('filter.dateTill.label')}/>
                    </div>
                    <div className="col-lg-4 text-right">
                      <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                    </div>
                  </Basic.Row>
                  <Basic.Row>
                    <div className="col-lg-4">
                      <Advanced.Filter.TextField
                        ref="executorName"
                        placeholder={this.i18n('entity.Identity.name')}
                        label={this.i18n('entity.AuditLog.executorName')}/>
                    </div>
                    <div className="col-lg-4">
                      <Advanced.Filter.TextField
                        ref="effectiveExecutorName"
                        placeholder={this.i18n('entity.Identity.name')}
                        label={this.i18n('entity.AuditLog.effectiveExecutorName')}/>
                    </div>
                    <div className="col-lg-4">

                    </div>
                  </Basic.Row>
                  <Basic.Row className="last">
                    <div className="col-lg-4">
                      <Advanced.Filter.TextField
                        ref="operationName"
                        label={this.i18n('entity.AuditLog.operationName')}
                        placeholder={this.i18n('entity.AuditLog.operationName')}/>
                    </div>
                    <div className="col-lg-4">
                      <Advanced.Filter.EnumSelectBox
                        ref="operationType"
                        label={this.i18n('entity.AuditLog.operationType')}
                        placeholder={this.i18n('filter.operationType.placeholder')}
                        multiSelect={true}
                        enum={OperationEnum}/>
                    </div>
                    <div className="col-lg-4">
                      <Advanced.Filter.EnumSelectBox
                        ref="operationResult"
                        label={this.i18n('entity.AuditLog.operationResult')}
                        placeholder={this.i18n('filter.operationResult.placeholder')}
                        multiSelect={false}
                        enum={OperationResultEnum}/>
                    </div>
                  </Basic.Row>
                </Basic.AbstractForm>
              </Advanced.Filter>
            }
            filterOpened={filterOpened}>
            <Advanced.Column property="date" face="datetime" sort={true}/>
            <Advanced.Column property="executorName"sort={true}/>
            <Advanced.Column property="effectiveExecutorName" sort={true}/>
            <Advanced.Column property="operationName" sort={true}/>
            <Advanced.Column property="operationType" sort={true} face="enum" enumClass={OperationEnum} />
            <Advanced.Column property="operationResult" sort={true} face="enum" enumClass={OperationResultEnum}/>
            <Advanced.Column property="detail" sort={true} rendered={true}/>
          </Advanced.Table>
        </Basic.Panel>

      </div>
    );
  }
}

AuditLog.propTypes = {
};
AuditLog.defaultProps = {
};

function select(state) {
  return {};
}

export default connect(select)(AuditLog);
