import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import { ScriptManager } from '../../redux';
import uuid from 'uuid';
import Helmet from 'react-helmet';
//
import { SecurityManager } from '../../redux';
import * as Advanced from '../../components/advanced';
import ScriptCategoryEnum from '../../enums/ScriptCategoryEnum';

// const uiKey = 'script-authorities';
const scriptManager = new ScriptManager();
const MAX_DESCRIPTION_LENGTH = 60;

/**
 * Script usage in other scripts
 *
 * @author Patrik Stloukal
 */
class ScriptReferences extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.scripts.references';
  }

    getNavigationKey() {
      return 'script-references';
    }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().useFilterForm(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  showDetail(entity, event) {
    if (event) {
      event.preventDefault();
    }
    // when create new script is generate non existing id
    // and set parameter new to 1 (true)
    // this is necessary for ScriptDetail
    if (entity.id === undefined) {
      const uuidId = uuid.v1();
      this.context.router.push(`/scripts/${uuidId}?new=1`);
    } else {
      this.context.router.push(`/scripts/${entity.id}/detail`);
    }
  }

  render() {
    const { uiKey, _entity } = this.props;
    if (this.props._entity == null) {
      return null;
    }
    //
    return (
      <div>

        <Helmet title={ this.i18n('title') } />
        <Basic.Panel className={ 'no-border last' }>
        <Basic.PanelHeader text={ this.i18n('header') } />
        <Basic.PanelBody style={ { padding: 0 } }>

        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          manager={scriptManager}
          forceSearchParameters={scriptManager.getDefaultSearchParameters().setFilter('usedIn', _entity.code)}
          showRowSelection={SecurityManager.hasAuthority('SCRIPT_DELETE')}
          rowClass={({rowIndex, data}) => { return data[rowIndex].disabled ? 'disabled' : ''; }}>
          <Advanced.Column
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={this.i18n('button.detail')}
                    onClick={this.showDetail.bind(this, data[rowIndex])}/>
                );
              }
            }
            sort={false}/>
          <Advanced.ColumnLink to="scripts/:id/detail" property="code" sort />
          <Advanced.Column property="name" sort />
          <Advanced.Column property="category" sort face="enum" enumClass={ScriptCategoryEnum}/>
          <Advanced.Column property="description" cell={ ({ rowIndex, data }) => {
            if (data[rowIndex] && data[rowIndex].description !== null) {
              const description = data[rowIndex].description.replace(/<(?:.|\n)*?>/gm, '').substr(0, MAX_DESCRIPTION_LENGTH);
              return description.substr(0, Math.min(description.length, description.lastIndexOf(' ')));
            }
            return '';
          }}/>
        </Advanced.Table>
        </Basic.PanelBody>
        </Basic.Panel>
      </div>
    );
  }
  }

ScriptReferences.propTypes = {
  _entity: PropTypes.object,
  _permissions: PropTypes.arrayOf(PropTypes.string),
  uiKey: PropTypes.string.isRequired,
  script: PropTypes.object.isRequired,
  rendered: PropTypes.bool.isRequired
};
ScriptReferences.defaultProps = {
  _entity: null,
  _permissions: null,
  rendered: true
};

function select(state, component) {
  return {
    _entity: scriptManager.getEntity(state, component.params.entityId),
    _permissions: scriptManager.getPermissions(state, null, component.params.entityId),
  };
}

export default connect(select)(ScriptReferences);
