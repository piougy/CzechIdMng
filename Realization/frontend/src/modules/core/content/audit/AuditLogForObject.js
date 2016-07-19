

import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../../../components/basic';
import * as Advanced from '../../../../components/advanced';
import { AuditLogManager, AuditLogForObjectManager, EmailLogManager } from '../../../../redux';
import ObjectClassEnum from '../../enums/ObjectClassEnum';
import OperationTargetEnum from '../../enums/OperationTargetEnum';
import OperationTypeEnum from '../../enums/OperationTypeEnum';


class AuditLogForObject extends Basic.AbstractContent {

  constructor(props, context) {
     super(props, context);
     this.state = {
       filterOpened: true,
       showEntityLogDetail: false,
       entityLogDetail: {}
     }
     this.auditLogForObjectManager = new AuditLogForObjectManager();
  }

  getManager() {
    return this.auditLogForObjectManager;
  }

  getContentKey() {
    return 'content.audit.object';
  }

  componentDidMount() {
    this.selectNavigationItem('audit-log-for-object');
  }

  openEntityLogDetail(entityLogDetail) {
    this.setState({
      showEntityLogDetail: true,
      entityLogDetail: entityLogDetail
    });
  }

  closeEntityLogDetail() {
    this.setState({ showEntityLogDetail: false });
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
    const { showEntityLogDetail, entityLogDetail, filterOpened } = this.state;

    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.PageHeader>
          {this.i18n('title')}
        </Basic.PageHeader>

        <Basic.Panel>
          <Advanced.Table
            ref="table"
            uiKey="audit_log_for_object_table"
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
                        ref="targetObject"
                        placeholder={this.i18n('entity.AuditLogForObject.targetObject')}
                        label={this.i18n('entity.AuditLogForObject.targetObject')}/>
                    </div>
                    <div className="col-lg-4">
                      <Advanced.Filter.TextField
                        ref="creator"
                        relation="EQ"
                        placeholder={'Uživatelské jméno'}
                        label={this.i18n('entity.AuditLogForObject.creator')}/>
                    </div>
                    <div className="col-lg-4">
                    </div>
                  </Basic.Row>
                  <Basic.Row>
                    <div className="col-lg-4">
                      <Advanced.Filter.EnumSelectBox
                        ref="objectClass"
                        label={this.i18n('entity.AuditLogForObject.objectClass')}
                        placeholder={this.i18n('filter.objectClass.placeholder')}
                        multiSelect={true}
                        enum={ObjectClassEnum}/>
                    </div>
                    <div className="col-lg-4">
                      <Advanced.Filter.EnumSelectBox
                        ref="operationTarget"
                        label={this.i18n('entity.AuditLogForObject.operationTarget')}
                        placeholder={this.i18n('filter.operationTarget.placeholder')}
                        multiSelect={true}
                        enum={OperationTargetEnum}/>
                    </div>
                    <div className="col-lg-4">
                      <Advanced.Filter.EnumSelectBox
                        ref="operationType"
                        label={this.i18n('entity.AuditLogForObject.operationType')}
                        placeholder={this.i18n('filter.operationType.placeholder')}
                        multiSelect={true}
                        enum={OperationTypeEnum}/>
                    </div>
                  </Basic.Row>
                </Basic.AbstractForm>
              </Advanced.Filter>
            }
            filterOpened={filterOpened}>

            <Advanced.Column property="date" face="datetime" sort={true}/>
            <Advanced.Column property="objectClass" sort={true}/>
            <Advanced.Column property="targetObject" sort={true}/>
            <Advanced.Column property="creator" sort={true}/>
            <Advanced.Column property="operationSubject" sort={true}/>
            <Advanced.Column property="operationTarget" sort={true}/>
            <Advanced.Column property="operationType" sort={true}/>
            <Advanced.Column property="oldValue" sort={true}/>
            <Advanced.Column property="newValue" sort={true}/>
            <Basic.Column
              header={this.i18n('label.action')}
              className="action"
              cell={
                ({rowIndex, data, property, ...props}) => {
                  if (data[rowIndex].detail != null) {
                      return <Basic.Button level="link" onClick={this.openEntityLogDetail.bind(this, data[rowIndex])}>{this.i18n('button.detail')}</Basic.Button>;
                  }

                }
              }/>
          </Advanced.Table>
        </Basic.Panel>

        <Basic.Modal
          show={showEntityLogDetail}
          onHide={this.closeEntityLogDetail.bind(this)}>
          <Basic.Modal.Header closeButton={true} text={this.i18n('entity.AuditLogForObject.detail')}/>
          <Basic.Modal.Body>
            {entityLogDetail.detail}
          </Basic.Modal.Body>
          <Basic.Modal.Footer>
            <Basic.Button level="link" onClick={this.closeEntityLogDetail.bind(this)} >{this.i18n('button.close')}</Basic.Button>
          </Basic.Modal.Footer>
        </Basic.Modal>
      </div>
    );
  }
}

AuditLogForObject.propTypes = {
}
AuditLogForObject.defaultProps = {
}

function select(state) {
  return {};
}

export default connect(select)(AuditLogForObject)
