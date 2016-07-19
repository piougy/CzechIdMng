

import AbstractEnum from './AbstractEnum';

/**
 * CRUD method for api calls
 */
export default class ApiOperationTypeEnum extends AbstractEnum {

  static getNiceLabel(key){
    return super.getNiceLabel(`enums.ApiOperationTypeEnum.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }
}

ApiOperationTypeEnum.GET  = Symbol('GET');
ApiOperationTypeEnum.CREATE  = Symbol('CREATE');
ApiOperationTypeEnum.UPDATE  = Symbol('UPDATE');
ApiOperationTypeEnum.DELETE  = Symbol('DELETE');
