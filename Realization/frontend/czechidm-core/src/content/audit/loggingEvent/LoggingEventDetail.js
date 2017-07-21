import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
//
import * as Basic from '../../../components/basic';
import { LoggingEventManager } from '../../../redux';
import LogTypeEnum from '../../../enums/LogTypeEnum';
import LoggingEventExceptionDetail from './LoggingEventExceptionDetail';

/**
* Basic detail for template detail,
* this detail is also used for create entity.
*
* @author Ondřej Kopr
*/

const manager = new LoggingEventManager();

class LoggingEventDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      showLoading: false
    };
  }

  getContentKey() {
    return 'content.audit.logging-event';
  }

  componentDidMount() {
    const { entityId } = this.props.params;
    //
    super.componentDidMount();
    this.selectNavigationItems(['audit', 'audits']);
    this.context.store.dispatch(manager.fetchEntity(entityId, 'UI_KEY_' + entityId));
  }

  render() {
    const { uiKey, entity } = this.props;
    const { showLoading } = this.state;
    return (
      <Basic.Row>
        <Helmet title={this.i18n('title')} />

        <Basic.PageHeader>
          <Basic.Icon value="eye-open"/>
          {' '}
          {this.i18n('header-detail')}
          {' '}
          <small>{this.i18n('detail')}</small>
        </Basic.PageHeader>

        <Basic.Panel>
          <Basic.AbstractForm
            data={entity} readOnly
            ref="form"
            uiKey={uiKey}
            style={{ padding: '15px 15px 0 15px' }}>
            <Basic.Row>
              <div className="col-lg-3">
                <Basic.TextField
                  ref="id"
                  label={this.i18n('entity.LoggingEvent.id')}/>
              </div>
              <div className="col-lg-9">
                <Basic.TextField
                  ref="loggerName"
                  label={this.i18n('entity.LoggingEvent.loggerName')}/>
              </div>
            </Basic.Row>
            <Basic.Row>
              <div className="col-lg-3">
                <Basic.DateTimePicker
                  ref="timestmp"
                  label={this.i18n('entity.LoggingEvent.timestmp')}
                  timeFormat={ this.i18n('format.times') }/>
              </div>
              <div className="col-lg-4">
                <Basic.EnumSelectBox
                  ref="levelString" enum={LogTypeEnum}
                  label={this.i18n('entity.LoggingEvent.levelString')}/>
              </div>
              <div className="col-lg-5">
                <Basic.TextField
                  ref="threadName"
                  label={this.i18n('entity.LoggingEvent.threadName')}/>
              </div>
            </Basic.Row>
            <Basic.TextField
              ref="callerFilename"
              label={this.i18n('entity.LoggingEvent.callerFilename')}/>
            <Basic.TextField
              ref="callerClass"
              label={this.i18n('entity.LoggingEvent.callerClass')}/>
            <Basic.Row>
              <div className="col-lg-3">
                <Basic.TextField
                  ref="callerLine"
                  label={this.i18n('entity.LoggingEvent.callerLine')}/>
              </div>
              <div className="col-lg-9">
                <Basic.TextField
                  ref="callerMethod"
                  label={this.i18n('entity.LoggingEvent.callerMethod')}/>
              </div>
            </Basic.Row>
            <Basic.Row>
              <div className="col-lg-3">
                <Basic.TextField
                  ref="arg0" hidden={entity && entity.arg0 === null}
                  label={this.i18n('entity.LoggingEvent.arg0')}/>
              </div>
              <div className="col-lg-3">
                <Basic.TextField
                  ref="arg1" hidden={entity && entity.arg1 === null}
                  label={this.i18n('entity.LoggingEvent.arg1')}/>
              </div>
              <div className="col-lg-3">
                <Basic.TextField
                  ref="arg2" hidden={entity && entity.arg2 === null}
                  label={this.i18n('entity.LoggingEvent.arg2')}/>
              </div>
              <div className="col-lg-3">
                <Basic.TextField
                  ref="arg3" hidden={entity && entity.arg3 === null}
                  label={this.i18n('entity.LoggingEvent.arg3')}/>
              </div>
            </Basic.Row>
            <Basic.TextArea ref="formattedMessage"
              label={this.i18n('entity.LoggingEvent.formattedMessage')}/>

            <div>
              <Basic.ContentHeader>
                <Basic.Icon value="warning-sign"/>
                {' '}
                <span dangerouslySetInnerHTML={{ __html: this.i18n('exceptions') }}/>
              </Basic.ContentHeader>
              <Basic.Panel >
                {
                  entity
                  ?
                  <LoggingEventExceptionDetail eventId={entity.id} />
                  :
                  <Basic.Loading />
                }
              </Basic.Panel>
            </div>
          </Basic.AbstractForm>

          <Basic.PanelFooter showLoading={showLoading} >
            <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
          </Basic.PanelFooter>
        </Basic.Panel>
      </Basic.Row>
    );
  }
}

LoggingEventDetail.propTypes = {
  uiKey: PropTypes.string.isRequired,
  isNew: PropTypes.bool
};
LoggingEventDetail.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;

  return {
    entity: manager.getEntity(state, entityId)
  };
}

export default connect(select)(LoggingEventDetail);
