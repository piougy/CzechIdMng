import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import { ScriptManager } from '../../redux';
//
import { SecurityManager, ScriptAuthorityManager, DataManager } from '../../redux';
import ScriptAuthorityTypeEnum from '../../enums/ScriptAuthorityTypeEnum';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';

const scriptManager = new ScriptManager();

class ScriptReferences extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'scripts.references';
  }

  getNavigationKey() {
    return 'script-references';
  }

  _getScriptReferences(scriptId) {
    scriptManager._getScriptReferences(scriptId);
  }

  render() {
    return null;
  }
}

ScriptReferences.propTypes = {
  _entity: PropTypes.object,
  _permissions: PropTypes.arrayOf(PropTypes.string)
};
ScriptReferences.defaultProps = {
  _entity: null,
  _permissions: null
};

function select(state, component) {
  return {
    _entity: scriptManager.getEntity(state, component.params.entityId),
    _permissions: scriptManager.getPermissions(state, null, component.params.entityId)
  };
}

export default connect(select)(ScriptReferences);
