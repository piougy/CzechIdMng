import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import { AbstractContent, Panel, PanelHeader, PanelBody } from '../../../../components/basic';

class Approve extends AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  componentDidMount() {
    this.selectSidebarItem('profile-approve');
  }

  render() {
    return (
      <div>
        <Helmet title={this.i18n('content.user.approve.title')} />
        <Panel>
          <PanelHeader text={this.i18n('content.user.approve.header')}/>
            <PanelBody>
              TODO: odebrat záložku a řešit prostřednictvím samostatné agendy rolí, kde bude rychlý filtr na role, pro které je uživatel schvalovatelem.
            </PanelBody>
        </Panel>
      </div>
    );
  }
}

Approve.propTypes = {
};
Approve.defaultProps = {
};

function select() {
  return {
  };
}

export default connect(select)(Approve);
