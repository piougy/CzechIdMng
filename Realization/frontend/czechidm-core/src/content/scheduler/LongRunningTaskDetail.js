import React, { PropTypes } from 'react';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import _ from 'lodash';
import moment from 'moment';
import { LocalizationService } from '../../services';
import { LongRunningTaskManager } from '../../redux';
import OperationStateEnum from '../../enums/OperationStateEnum';

const longRunningTaskManager = new LongRunningTaskManager();

/**
 * Table with long running task items and detail of LRT
 *
 * @author Marek Klement
 * @author Radek Tomiška
 */
export default class LongRunningTaskDetail extends Basic.AbstractContent {

  constructor(props) {
    super(props);
  }

  getContentKey() {
    return 'content.scheduler.all-tasks';
  }

  render() {
    const { entity } = this.props;
    //
    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.Panel className="no-border last">
          <Basic.PanelHeader text={ this.i18n('tabs.basic') } />

          <Basic.PanelBody style={{ padding: 0 }}>

            <Basic.AbstractForm data={entity} readOnly>
              <Basic.Row>
                <Basic.Col lg={ 6 }>
                  <Basic.LabelWrapper label={this.i18n('entity.created')}>
                    <div style={{ margin: '7px 0' }}>
                      <Advanced.DateValue value={entity.created} showTime/>
                    </div>
                  </Basic.LabelWrapper>
                </Basic.Col>
                <Basic.Col lg={ 6 }>
                  <Basic.LabelWrapper label={this.i18n('entity.LongRunningTask.instanceId.label')}>
                    <div style={{ margin: '7px 0' }}>
                      {entity.instanceId}
                      <span className="help-block">{this.i18n('entity.LongRunningTask.instanceId.help')}</span>
                    </div>
                  </Basic.LabelWrapper>
                </Basic.Col>
              </Basic.Row>

              <Basic.Row>
                <Basic.Col lg={ 6 }>
                  <Basic.LabelWrapper label={this.i18n('entity.LongRunningTask.taskType')}>
                    <div style={{ margin: '7px 0' }}>
                      { Utils.Ui.getSimpleJavaType(entity.taskType) }
                    </div>
                  </Basic.LabelWrapper>
                </Basic.Col>
                <Basic.Col lg={ 6 }>
                  <Basic.LabelWrapper label={this.i18n('entity.LongRunningTask.taskProperties.label')}>
                    <div style={{ margin: '7px 0' }}>
                      {
                        _.keys(entity.taskProperties).map(propertyName => {
                          return (
                            <div>{ propertyName }: { '' + entity.taskProperties[propertyName] }</div>
                          );
                        })
                      }
                    </div>
                  </Basic.LabelWrapper>
                </Basic.Col>
              </Basic.Row>

              <Basic.TextArea
                label={this.i18n('entity.LongRunningTask.taskDescription')}
                disabled
                value={entity.taskDescription}/>

              <Basic.Row>
                <Basic.Col lg={ 6 }>
                  {
                    !entity.taskStarted
                    &&
                    <Basic.LabelWrapper label={this.i18n('entity.LongRunningTask.notstarted')}/>
                    ||
                    <Basic.LabelWrapper label={this.i18n('entity.LongRunningTask.counter')}>
                      <div style={{ margin: '7px 0' }}>
                        { longRunningTaskManager.getProcessedCount(entity) }
                      </div>
                    </Basic.LabelWrapper>
                  }
                </Basic.Col>
                <Basic.Col lg={ 6 }>
                  {
                    !entity.taskStarted
                    ||
                    <Basic.LabelWrapper label={this.i18n('entity.LongRunningTask.duration')}>
                        <div style={{ margin: '7px 0' }}>
                          <Basic.Tooltip
                            ref="popover"
                            placement="bottom"
                            value={ moment.utc(moment.duration(moment(entity.modified).diff(moment(entity.taskStarted))).asMilliseconds()).format(this.i18n('format.times'))}>
                            <span>
                              { moment.duration(moment(entity.taskStarted).diff(moment(entity.modified))).locale(LocalizationService.getCurrentLanguage()).humanize() }
                            </span>
                          </Basic.Tooltip>
                        </div>
                    </Basic.LabelWrapper>
                  }
                </Basic.Col>
              </Basic.Row>

              {
                !entity.taskStarted
                ||
                <Basic.LabelWrapper label={this.i18n('entity.LongRunningTask.started')}>
                  <div style={{ margin: '7px 0' }}>
                    <Advanced.DateValue value={entity.taskStarted} showTime/>
                  </div>
                </Basic.LabelWrapper>
              }

              <Basic.ContentHeader text={ this.i18n('content.scheduler.all-tasks.detail.result') }/>
              <div style={{ marginBottom: 15 }}>
                <Basic.EnumValue value={ entity.resultState } enum={ OperationStateEnum }/>
                {
                  (!entity.result || !entity.result.code)
                  ||
                  <span style={{ marginLeft: 15 }}>
                    {this.i18n('content.scheduler.all-tasks.detail.resultCode')}: { entity.result.code }
                  </span>
                }
                <Basic.FlashMessage message={this.getFlashManager().convertFromResultModel(entity.result.model)} style={{ margin: '15px 0 0 0' }}/>
              </div>
              {
                (!entity.result || !entity.result.stackTrace)
                ||
                <div>
                  <textArea
                    rows="10"
                    value={ entity.result.stackTrace }
                    readOnly
                    style={{ width: '100%', marginBottom: 15 }}/>
                </div>
              }


            </Basic.AbstractForm>
          </Basic.PanelBody>

          <Basic.PanelFooter>
            <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>
              {this.i18n('button.back')}
            </Basic.Button>
          </Basic.PanelFooter>
        </Basic.Panel>
      </div>
    );
  }
}

LongRunningTaskDetail.propTypes = {
  entity: PropTypes.object,
};
