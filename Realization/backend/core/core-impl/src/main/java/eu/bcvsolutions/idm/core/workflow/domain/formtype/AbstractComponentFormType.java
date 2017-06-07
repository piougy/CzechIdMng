package eu.bcvsolutions.idm.core.workflow.domain.formtype;

import java.util.Map;

import org.activiti.engine.form.AbstractFormType;


/**
 * 
 * @author svandav
 *
 */
public abstract class AbstractComponentFormType extends AbstractFormType {
	
  private static final long serialVersionUID = 1L;
  
  protected Map<String, String> values;

  public AbstractComponentFormType(Map<String, String> values) {
    this.values = values;
  }

  @Override
  public Object getInformation(String key) {
    if ("values".equals(key)) {
      return values;
    }
    return null;
  }
  
  /**
   * If true, then will be item added to REST
   * @return
   */
  public boolean isExportableToRest(){
	  return true;
  }

}
