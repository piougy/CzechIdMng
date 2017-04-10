/**
 * Helper methods for response error and info handling
 *
 * TODO: split error and info messages
 *
 * @author Radek Tomiška
 */
export default class ResponseUtils {

  static hasError(responseJson) {
    if (ResponseUtils.getFirstError(responseJson) === null) {
      return false;
    }
    return true;
  }

  /**
   * TODO: split error and info messages, remove this method and all usages
   */
  static _isError(error) {
    return error.statusCode >= 300;
  }

  static getFirstError(responseJson) {
    const content = responseJson;
    // no content = no error
    if (!content) {
      return null;
    }
    // one error
    if (content.error) {
      if (ResponseUtils._isError(content.error)) {
        return content.error;
      }
      return null;
    }
    // array of errors (e.g. when validation fails)
    if (!content._errors || content._errors.length === 0) {
      return null;
    }
    for (const error of content._errors) {
      if (ResponseUtils._isError(error)) {
        return error;
      }
    }
    return null;
  }

  static hasInfo(responseJson) {
    if (ResponseUtils.getFirstInfo(responseJson) === null) {
      return false;
    }
    return true;
  }

  static getFirstInfo(responseJson) {
    const content = responseJson;
    // no content = no error
    if (!content) {
      return null;
    }
    // one error
    if (content.error) {
      if (!ResponseUtils._isError(content.error)) {
        return content.error;
      }
      return null;
    }
    // array of errors (e.g. when validation fails)
    if (!content._errors || content._errors.length === 0) {
      return null;
    }
    for (const error of content._errors) {
      if (!ResponseUtils._isError(error)) {
        return error;
      }
    }
    return null;
  }

  static getInfos(/* responseJson */) {
    throw new Error('unsupported operation');
  }

  static getErrors(/* responseJson */) {
    throw new Error('unsupported operation');
  }
}
