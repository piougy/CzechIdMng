import React from 'react';
import {Basic} from 'czechidm-core';

export default class ExampleDashboard extends Basic.AbstractContent {

  _goToProfil() {
    this.context.router.push('/user/' + this.props.entityId + '/profile');
  }

  getContentKey() {
    return 'example:content.dashboard.exampleDashboard';
  }

  render() {
    return (
      <Basic.Panel>
        <Basic.PanelHeader text={this.i18n('header')}/>
        <Basic.PanelBody >
          <Basic.Panel className="panel-warning">
            <Basic.PanelHeader>
              <Basic.Row>
                <div className="col-lg-3">
                  <Basic.Icon type="fa" icon="dashboard" className="fa-5x"/>
                </div>
                <div className="col-lg-9">
                  <div><strong>{this.i18n('title')}</strong></div>
                  <div>{this.i18n('text')}</div>
                </div>
              </Basic.Row>
            </Basic.PanelHeader>
          </Basic.Panel>
        </Basic.PanelBody>
      </Basic.Panel>
    );
  }
}
