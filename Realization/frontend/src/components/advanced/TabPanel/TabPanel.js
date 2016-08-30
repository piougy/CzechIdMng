import React, { PropTypes } from 'react';
import ReactDOM from 'react-dom';
import { connect } from 'react-redux';
//
import TabPanelItem from './TabPanelItem';
import { getNavigationItems, resolveNavigationParameters } from '../../../redux/Layout/layoutActions';
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
    const { navigation, userContext, activeItem, activeNavigationItem, parentId } = this.props;
    const { userID, revID } = this.props.params;

    const params = { userID, revID };

    return getNavigationItems(navigation, parentId || activeNavigationItem, null, userContext, params).map(item => {
      const labelParams = resolveNavigationParameters(userContext, params);
      labelParams.defaultValue = item.label;
      //
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
              active={activeItem === item.id}>
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
    const navigationItems = this.getNavigationItems();

    return (
      <div ref="tabPanel" className="tab-panel clearfix">
        <ul ref="tabPanelSidebar" className="tab-panel-sidebar nav nav-pills nav-stacked">
          {navigationItems}
        </ul>
        <div ref="tabPanelContent" className="tab-panel-content tab-content">
          {this.props.children}
        </div>
      </div>
    );
  }
}

TabPanel.propTypes = {
  navigation: PropTypes.object,
  activeNavigationItem: PropTypes.string,
  activeItem: PropTypes.string,
  userContext: PropTypes.object,
  /**
   * which navigation parent wil be rendered - sub menus to render
   */
  parentId: PropTypes.string
};
TabPanel.defaultProps = {
  navigation: null,
  activeNavigationItem: null,
  activeItem: null,
  userContext: null
};

function select(state) {
  const selectedNavigationItems = state.layout.get('selectedNavigationItems');
  const activeNavigationItem = (selectedNavigationItems.length > 0 ? selectedNavigationItems[0] : null);
  const selectedSidebarItem = (selectedNavigationItems.length > 1 ? selectedNavigationItems[1] : null);
  return {
    navigation: state.layout.get('navigation'),
    activeNavigationItem,
    activeItem: selectedSidebarItem,
    userContext: state.security.userContext
  };
}

export default connect(select)(TabPanel);
