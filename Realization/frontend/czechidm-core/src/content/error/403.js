import React from 'react';
import Helmet from 'react-helmet';
import { AbstractContent, Icon } from '../../components/basic';


export default class Error403 extends AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  componentDidMount() {
    this.selectNavigationItem('home');
  }

  render() {
    return (
      <div>
        <Helmet title={this.i18n('content.error.403.title')} />
        <div className="alert alert-danger">
          <div className="alert-icon"><Icon icon="exclamation-sign"/></div>
          <div className="alert-desc">{this.i18n('content.error.403.description')}</div>
        </div>
      </div>
    );
  }
}
