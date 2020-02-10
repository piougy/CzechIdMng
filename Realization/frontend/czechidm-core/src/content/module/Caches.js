import React from 'react';
import PropTypes from 'prop-types';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { CacheManager, DataManager } from '../../redux';
import * as Utils from '../../utils';
import ResultCodesModal from './ResultCodesModal';

/**
 * Caches administration
 *
 * @author Peter Štrunc
 */
class Caches extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      detail: {
        show: false
      }
    };
    this.cacheManager = new CacheManager();
  }

  getContentKey() {
    return 'content.system.cache';
  }

  getNavigationKey() {
    return 'caches';
  }

  componentDidMount() {
    super.componentDidMount();
    //
    this.reload();
  }

  reload() {
    this.context.store.dispatch(this.cacheManager.fetchAvailableCaches());
  }

  onEvict(entity, event) {
    if (event) {
      event.preventDefault();
    }
    this.refs[`confirm-evict`].show(
      this.i18n(`action.evict.message`, { count: 1, record: entity.name }),
      this.i18n(`action.evict.header`, { count: 1 })
    ).then(() => {
      this.context.store.dispatch(this.cacheManager.evictCache(entity.id, (patchedEntity, error) => {
        if (!error) {
          this.addMessage({ message: this.i18n(`action.evict.success`, { count: 1, record: entity.name }) });
          this.reload();
        } else {
          this.addError(error);
        }
      }));


    }, () => {
      // Rejected
    });
  }

  onEvictAll(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs[`confirm-evict`].show(
      this.i18n(`action.evict.message`, { count: 2 }),
      this.i18n(`action.evict.header`, { count: 1 })
    ).then(() => {
      this.context.store.dispatch(this.cacheManager.evictAllCaches((patchedEntity, error) => {
        if (!error) {
          this.addMessage({ message: this.i18n(`action.evict.success`, { count: 2 }) });
          this.reload();
        } else {
          this.addError(error);
        }
      }));
    }, () => {
      // Rejected
    });
  }

  render() {
    const { caches, showLoading } = this.props;
    const { detail } = this.state;

    const _caches = [];
    if (caches) {
      caches.forEach(cache => {
        _caches.push(cache);
      });
    }
    _caches.sort((one, two) => (one.name > two.name ? 1 : -1));

    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-evict" level="warning"/>

        <ResultCodesModal detail={ detail } />

        <Basic.Toolbar>
          <div>
            <div className="pull-right">
              <Basic.Button
                level="warning"
                onClick={this.onEvictAll.bind(this)}
                className="btn-xs"
                title={this.i18n('button.evictAll')}
                titlePlacement="bottom">
                {this.i18n('button.evictAll')}
              </Basic.Button>
              <Advanced.RefreshButton
                onClick={this.reload.bind(this)}
                title={this.i18n('button.refresh')}
                showLoading={showLoading}/>
            </div>
            <div className="clearfix"/>
          </div>
        </Basic.Toolbar>

        <Basic.Table
          ref="table"
          data={_caches}
          showLoading={showLoading}
          noData={this.i18n('component.basic.Table.noData')}
          rowClass={({rowIndex, data}) => Utils.Ui.getRowClass(data[rowIndex]) }>

          <Basic.Column
            header={this.i18n('entity.Cache.module')}
            className="text"
            sort
            cell={
              ({rowIndex, data}) => {
                const { name } = data[rowIndex];
                const [module] = name.split(':');
                return (
                  module
                );
              }
            }/>
          <Basic.Column property="name" header={this.i18n('entity.Cache.name')}/>
          <Basic.Column
            header={this.i18n('entity.Cache.description')}
            className="text"
            cell={
              ({rowIndex, data}) => {
                const { name } = data[rowIndex];
                const [module, nameWithoutModule] = name.split(':');
                return (
                  this.i18n(`${module}:cache.${nameWithoutModule}.description`)
                );
              }
            }/>
          <Basic.Column property="size" header={this.i18n('entity.Cache.size')}/>
          <Basic.Column
            header={this.i18n('label.action')}
            className="action"
            cell={
              ({rowIndex, data}) => {
                return (
                  <Basic.Button
                    level="warning"
                    onClick={this.onEvict.bind(this, data[rowIndex])}
                    className="btn-xs"
                    title={this.i18n('button.evict')}
                    titlePlacement="bottom">
                    {this.i18n('button.evict')}
                  </Basic.Button>
                );
              }
            }/>
        </Basic.Table>
      </div>
    );
  }
}

Caches.propTypes = {
  userContext: PropTypes.object,
  caches: PropTypes.object,
  showLoading: PropTypes.bool
};
Caches.defaultProps = {
  userContext: null,
  caches: null,
  showLoading: true
};

function select(state) {
  return {
    userContext: state.security.userContext,
    caches: DataManager.getData(state, CacheManager.UI_KEY_CACHES),
    showLoading: Utils.Ui.isShowLoading(state, CacheManager.UI_KEY_CACHES)
  };
}

export default connect(select)(Caches);
