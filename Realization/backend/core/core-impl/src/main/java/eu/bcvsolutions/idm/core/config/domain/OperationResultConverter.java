package eu.bcvsolutions.idm.core.config.domain;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;

import eu.bcvsolutions.idm.core.api.entity.OperationResult;

/**
 * Converter for {@link OperationResult} and for resolve problem with 0x00 character in Postgres.
 * 
 * @author svandav
 *
 */
public class OperationResultConverter implements Converter<OperationResult, OperationResult> {

	private ModelMapper modeler;
	
	public OperationResultConverter(ModelMapper modeler) {
		this.modeler = modeler;
	}

	@Override
	public OperationResult convert(MappingContext<OperationResult, OperationResult> context) {
		if (context != null && context.getSource() != null && context.getSource() instanceof OperationResult) {
			OperationResult source = context.getSource();
			OperationResult operationResult = new OperationResult();
			
			// We need use convertors for the String (StringToStringConverter)
			operationResult.setCause(source.getCause() != null ? modeler.map(source.getCause(), String.class) : null);
			operationResult.setCode(source.getCode() != null ? modeler.map(source.getCode(), String.class) : null);
			operationResult.setState(source.getState());
			operationResult.setModel(source.getModel());
			return operationResult;
		}
		return null;
	}
	
}