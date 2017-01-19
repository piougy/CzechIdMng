import React, { PropTypes } from 'react';
//
import { Basic, Advanced } from 'czechidm-core';
import SystemEntityTypeEnum from '../domain/SystemEntityTypeEnum';

/**
 * Show entity info by given SystemEntityTypeEnum
 *
 * TODO: could be used universal for all entity types
 */
export default class EntityInfo extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
  }

  render() {
    const { rendered, showLoading, entityType, entityIdentifier, face, style } = this.props;
    if (!rendered) {
      return null;
    }
    if (showLoading) {
      return (
        <Basic.Icon value="refresh" showLoading/>
      );
    }
    if (!entityType || !entityIdentifier) {
      return null;
    }
    //
    // entity link and info
    const _entityType = SystemEntityTypeEnum.findSymbolByKey(entityType);
    switch (_entityType) {
      case SystemEntityTypeEnum.IDENTITY: {
        return (<Advanced.IdentityInfo id={entityIdentifier} face={face} style={style} />);
      }
      default: {
        this.getLogger().warn(`[ProvisioningOperations]: Entity info for type [${entityType}] is not supported.`);
      }
    }
    //
    return (
      <div style={style}>
        <Basic.EnumValue value={entityType} enum={SystemEntityTypeEnum}/>
        {' '}
        { entityIdentifier }
      </div>
    );
  }
}

EntityInfo.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  /**
   * Entity type (see SystemEntityTypeEnum)
   */
  entityType: PropTypes.string.isRequired,
  /**
   * Entity identifier
   */
  entityIdentifier: PropTypes.string.isRequired,
  /**
   * Decorator
   */
  face: PropTypes.oneOf(['full', 'link'])
};
EntityInfo.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  face: 'full'
};
