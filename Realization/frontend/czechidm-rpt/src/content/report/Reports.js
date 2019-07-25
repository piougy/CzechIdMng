import React from 'react';
//
import { Basic } from 'czechidm-core';
import ReportTableComponent from './ReportTable';

/**
 * List of reports
 *
 * @author Radek Tomiška
 */
export default class Reports extends Basic.AbstractContent {

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
      <Basic.Div>
        { this.renderPageHeader() }

        <Basic.Panel>
          <ReportTableComponent
            uiKey="rpt-report-table"
            filterOpened
            location={ this.props.location }/>
        </Basic.Panel>

      </Basic.Div>
    );
  }
}
