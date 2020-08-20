/**
 * Completers for mapping context.
 *
 * TODO: Move to the BE and from the core module.
 * @author Vít Švanda
 */
export default class MappingContextCompleters {

	static getCompleters() {
		return [
			{
				name: 'context.getIdentityRoles()',
				returnType: 'List<IdmIdentityRoleDto>',
				description: 'Get all assigned identity roles.'
			},
			{
				name: 'context.getIdentityRolesForSystem()',
				returnType: 'List<IdmIdentityRoleDto>',
				description: 'Get all assigned identity roles that this system assigns.'
			},
			{
				name: 'context.getContracts()',
				returnType: 'List<IdmIdentityContractDto>',
				description: 'Get all assigned identity contracts.'
			},
			{
				name: 'context.getConnectorObject()',
				returnType: 'IcConnectorObject',
				description: 'Get an object from the target system.'
			},
			{
				name: 'context.put(key, value)',
				returnType: 'void',
				description: 'Put the value to the context.'
			},
			{
				name: 'context.get(key)',
				returnType: 'Object',
				description: 'Get the value from the context.'
			},
			{
				name: 'context.getContext()',
				returnType: 'Map<String,Object>',
				description: 'Get the whole context map.'
			}
		];
	}
}
