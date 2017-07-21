import React, { PropTypes } from 'react';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import { LoggingEventExceptionManager } from '../../../redux';

const EU_BCVSOLUTIONS_PREFIX = 'eu.bcvsolutions.idm.';

/**
 * Logging event exception detail
 *
 * @author Ondřej Kopr
 */

const manager = new LoggingEventExceptionManager();

export default class LoggingEventExceptionDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  componentDidMount() {
    //
    super.componentDidMount();
  }

  getContentKey() {
    return 'content.audit.logging-event';
  }


  render() {
    const { eventId } = this.props;

    return (
      <div>
        <Advanced.Table
          ref="table" style={{ whiteSpace: 'pre' }}
          filterOpened
          manager={manager} showId={false}
          forceSearchParameters={manager.getDefaultSearchParameters().setFilter('event', eventId)}
          rowClass={({ rowIndex, data }) => {
            if (data[rowIndex].traceLine.indexOf(EU_BCVSOLUTIONS_PREFIX) + 1) {
              return 'warning';
            }
          }}>
          <Advanced.Column property="id" width="5%" />
          <Advanced.Column property="traceLine"
            cell={
              ({ rowIndex, data }) => {
                return data[rowIndex].traceLine;
              }
            }/>

        </Advanced.Table>
      </div>
    );
  }
}

LoggingEventExceptionDetail.propTypes = {
  eventId: PropTypes.string
};

LoggingEventExceptionDetail.defaultProps = {

};
