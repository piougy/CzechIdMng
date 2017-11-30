import React from 'react';
//
import { Basic } from 'czechidm-core';
import ReportTable from './ReportTable';

/**
 * List of reports
 *
 * @author Radek Tomiška
 */
export default class Reports extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  /**
   * "Shorcut" for localization
   */
  getContentKey() {
    return 'rpt:content.reports';
  }

  /**
   * Selected navigation item
   */
  getNavigationKey() {
    return 'rpt-reports';
  }

  render() {
    return (
      <div>
        { this.renderPageHeader() }

        <Basic.Panel>
          <ReportTable uiKey="rpt-report-table" filterOpened location={ this.props.location }/>
        </Basic.Panel>

      </div>
    );
  }
}
