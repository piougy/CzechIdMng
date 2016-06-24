import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import { AbstractContent, Panel, PanelHeader, PanelBody, Icon } from '../../../../components/basic';


export default class Error503 extends AbstractContent {

  constructor(props, context) {
     super(props, context);
  }

  componentDidMount(){
    this.selectNavigationItem('home');
  }

  render() {
    return (
      <div>
        <Helmet title={this.i18n('content.error.503.title')} />
        <div className="alert alert-danger">
          <div className="alert-icon"><Icon icon="exclamation-sign"/></div>
          <div className="alert-desc">{this.i18n('content.error.503.description')}</div>
        </div>
      </div>
    );
  }
}
