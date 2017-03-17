/**
 * Helper methods for response error and info handling
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

  static hasInfo(/* responseJson */) {
    throw new Error('unsupported operation');
  }

  static getFirstError(responseJson) {
    const content = responseJson;
    // no content = no error
    if (!content) {
      return null;
    }
    // one error
    if (content.error) {
      return content.error;
    }
    // array of errors (e.g. when validation fails)
    if (!content._errors || content._errors.length === 0) {
      return null;
    }
    return content._errors[0];
  }

  static getInfos(/* responseJson */) {
    throw new Error('unsupported operation');
  }

  static getErrors(/* responseJson */) {
    throw new Error('unsupported operation');
  }

}
