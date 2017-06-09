import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import Immutable from 'immutable';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import { EntityEventProcessorManager, DataManager } from '../../redux';
import * as Utils from '../../utils';

/**
 * BE event precessors
 *
 * @author Radek Tomiška
 */
class EntityEventProcessors extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.manager = new EntityEventProcessorManager();
  }

  getContentKey() {
    return 'content.system.fe-modules';
  }

  componentDidMount() {
    this.selectNavigationItems(['system', 'modules', 'entity-event-processors']);
    this.context.store.dispatch(this.manager.fetchRegisteredProcessors());
  }

  render() {
    const { registeredProcessors, showLoading } = this.props;
    //
    let _registeredProcessors = new Immutable.OrderedMap();
    if (registeredProcessors) {
      registeredProcessors.forEach(processor => {
        if (!_registeredProcessors.has(processor.entityType)) {
          _registeredProcessors = _registeredProcessors.set(processor.entityType, []);
        }
        const entityProcessors = _registeredProcessors.get(processor.entityType);
        entityProcessors.push(processor);
        _registeredProcessors = _registeredProcessors.set(processor.entityType, entityProcessors);
      });
    }
    if (showLoading) {
      return (
        <Basic.Loading isStatic show />
      );
    }
    //
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-deactivate" level="warning"/>
        <Basic.Confirm ref="confirm-activate" level="success"/>
        {
          _registeredProcessors.map((processors, entityType) => {
            processors.sort((one, two) => {
              return one.order > two.order;
            });
            return (
              <div className="tab-pane-table-body" style={{ marginBottom: 15 }}>
                <Basic.ContentHeader text={entityType}/>

                <Basic.Table
                  data={processors}
                  showLoading={showLoading}
                  noData={this.i18n('component.basic.Table.noData')}
                  rowClass={({ rowIndex, data }) => { return Utils.Ui.getRowClass(data[rowIndex]); }}>
                  <Basic.Column property="module" header={this.i18n('entity.EntityEventProcessor.module')} width={75} />
                  <Basic.Column property="name" header={this.i18n('entity.EntityEventProcessor.name')} width="30%"/>
                  <Basic.Column
                    property="description"
                    header={this.i18n('entity.EntityEventProcessor.description')}
                    cell={
                      ({ rowIndex, data, property }) => {
                        const values = [];
                        _.keys(data[rowIndex].configurationProperties).map(configurationProperty => {
                          const value = data[rowIndex].configurationProperties[configurationProperty];
                          if (value) {
                            values.push({ configurationProperty, value });
                          }
                        });
                        return (
                          <div>
                            <div>{data[rowIndex][property]}</div>
                            {
                              values.length === 0
                              ||
                              <div>
                                <div>Configuration:</div>
                                {
                                  values.map(value => {
                                    return (<div>{ `- ${value.configurationProperty}: ${value.value}` }</div>);
                                  })
                                }
                              </div>
                            }
                          </div>
                        );
                      }
                    }/>
                  <Basic.Column
                    property="eventTypes"
                    header={this.i18n('entity.EntityEventProcessor.eventTypes')}
                    width={125}
                    cell={
                      ({ rowIndex, data, property }) => {
                        if (!data[rowIndex][property]) {
                          return null;
                        }
                        return data[rowIndex][property].join(', ');
                      }
                    }/>
                  <Basic.Column property="order" header={this.i18n('entity.EntityEventProcessor.order')} width={100}/>
                  <Basic.Column
                    property="disabled"
                    header={<Basic.Cell className="column-face-bool">{this.i18n('entity.EntityEventProcessor.disabled')}</Basic.Cell>}
                    cell={<Basic.BooleanCell className="column-face-bool"/>}
                    width="100px"/>
                </Basic.Table>
              </div>
            );
          })
        }
      </div>
    );
  }
}

EntityEventProcessors.propTypes = {
  userContext: PropTypes.object,
  registeredProcessors: PropTypes.object,
  showLoading: PropTypes.bool
};
EntityEventProcessors.defaultProps = {
  userContext: null,
  showLoading: true,
  registeredProcessors: null
};

function select(state) {
  return {
    userContext: state.security.userContext,
    registeredProcessors: DataManager.getData(state, EntityEventProcessorManager.UI_KEY_PROCESSORS),
    showLoading: Utils.Ui.isShowLoading(state, EntityEventProcessorManager.UI_KEY_PROCESSORS)
  };
}

export default connect(select)(EntityEventProcessors);
