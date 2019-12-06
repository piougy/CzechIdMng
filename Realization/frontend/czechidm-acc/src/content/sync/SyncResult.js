import React from 'react';
//
import { Basic } from 'czechidm-core';

/**
 * Results for given synchronization log
 *
 * @author Vít Švanda
 */
class SyncResult extends Basic.AbstractContent {

  render() {
    const {log} = this.props;

    const actions = [];
    for (const action of log.syncActionLogs) {
      let level = 'default';
      if (action.operationResult === 'SUCCESS') {
        level = 'success';
      }
      if (action.operationResult === 'ERROR') {
        level = 'danger';
      }
      if (action.operationResult === 'WARNING') {
        level = 'warning';
      }
      if (action.operationResult === 'WF') {
        level = 'warning';
      }
      if (action.operationResult === 'IGNORE') {
        level = 'primary';
      }
      actions.push(
        <div>
          <Basic.Label style={{marginRight: '5px'}} level={level} text={action.operationCount}/>
          <strong>
            {this.i18n(`acc:entity.SynchronizationLog.actions.${action.operationResult}.${action.syncAction}`)}
          </strong>
        </div>
      );
    }
    return (
      <div>
        {actions}
      </div>
    );
  }

}

export default SyncResult;
