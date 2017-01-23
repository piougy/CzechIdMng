import React, { PropTypes } from 'react';
import ReactDOM from 'react-dom';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import TabPanelItem from './TabPanelItem';
import { getNavigationItems, resolveNavigationParameters } from '../../../redux/layout/layoutActions';
import * as Basic from '../../basic';

/**
 * Sidebar renders tabs by given navigation parent (parentId)
 */
class TabPanel extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
  }

  componentDidMount() {
    // window.addEventListener('resize', this.handleResize);
    this.handleResize();
  }

  componentDidUpdate() {
    this.handleResize();
  }

  componentWillUnmount() {
    // window.removeEventListener('resize', this.handleResize);
  }

  handleResize() {
    if (typeof $ !== undefined) {
      const tabPanelSidebar = $(ReactDOM.findDOMNode(this.refs.tabPanelSidebar));
      const tabPanelContent = $(ReactDOM.findDOMNode(this.refs.tabPanelContent));
      tabPanelSidebar.css({
        height: tabPanelContent.height()
      });
    }
  }

  getNavigationItems() {
    const { navigation, userContext, parentId, selectedNavigationItems } = this.props;
    const { revID, entityId } = this.props.params;

    const params = { revID, entityId };
    return getNavigationItems(navigation, parentId, null, userContext, params).map(item => {
      const labelParams = resolveNavigationParameters(userContext, params);
      labelParams.defaultValue = item.label;
      switch (item.type) {
        case 'TAB':
        case 'DYNAMIC': {
          return (
            <TabPanelItem
              id={`nav-item-${item.id}`}
              key={`nav-item-${item.id}`}
              to={item.to}
              icon={item.icon}
              iconColor={item.iconColor}
              title={this.i18n(item.titleKey, { defaultValue: item.title })}
              active={_.includes(selectedNavigationItems, item.id)}>
              {
                (item.labelKey || item.label)
                ?
                <span>{this.i18n(item.labelKey, labelParams)}</span>
                :
                <span>{this.i18n(item.titleKey, { defaultValue: item.title })}</span>
              }
            </TabPanelItem>
          );
        }
        default: {
          this.getLogger().error('WARNING: navigation: ' + item.type + ' type not implemeted for item id [' + item.id + ']');
        }
      }
    });
  }

  render() {
    const { position } = this.props;
    const navigationItems = this.getNavigationItems();

    if (position === 'top') {
      return (
        <div className="tab-horizontal">
          <ul className="nav nav-tabs">
            { navigationItems }
          </ul>
          <div className="tab-content">
            <div className="tab-pane active" style={{ padding: '0px 15px' }}>
              { this.props.children }
            </div>
          </div>
        </div>
      );
    }
    //
    // left
    return (
      <Basic.Panel className="clearfix">
        <div ref="tabPanel" className="tab-panel tab-vertical clearfix">
          <ul ref="tabPanelSidebar" className="tab-panel-sidebar nav nav-pills nav-stacked">
            {navigationItems}
          </ul>
          <div ref="tabPanelContent" className="tab-panel-content tab-content">
            {this.props.children}
          </div>
        </div>
      </Basic.Panel>
    );
  }
}

TabPanel.propTypes = {
  navigation: PropTypes.object,
  userContext: PropTypes.object,
  /**
   * which navigation parent wil be rendered - sub menus to render
   */
  parentId: PropTypes.string,
  /**
   * Tabs position
   *
   * @param  {[type]} ['left' [description]
   * @param  {[type]} 'top']  [description]
   * @return {[type]}         [description]
   */
  position: PropTypes.oneOf(['left', 'top'])
};
TabPanel.defaultProps = {
  navigation: null,
  userContext: null,
  position: 'left'
};

function select(state) {
  const selectedNavigationItems = state.layout.get('selectedNavigationItems');
  return {
    navigation: state.layout.get('navigation'),
    selectedNavigationItems,
    userContext: state.security.userContext
  };
}

export default connect(select)(TabPanel);
