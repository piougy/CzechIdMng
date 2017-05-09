import React from 'react';
import Helmet from 'react-helmet';
import * as Basic from '../../../components/basic';
import { SmsManager } from '../../../redux';
import SmsTable from './SmsTable';

/**
 * List of sms logs
 */
export default class Sms extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.manager = new SmsManager();
  }

  getContentKey() {
    return 'content.sms';
  }

  componentDidMount() {
    this.selectNavigationItem('notification-sms');
  }

  render() {
    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.PageHeader>
          {this.i18n('header')}
        </Basic.PageHeader>

        <Basic.Panel>
          <SmsTable uiKey="sms-table" manager={this.manager} filterOpened/>
        </Basic.Panel>

      </div>
    );
  }
}

Sms.propTypes = {
};
Sms.defaultProps = {
};
