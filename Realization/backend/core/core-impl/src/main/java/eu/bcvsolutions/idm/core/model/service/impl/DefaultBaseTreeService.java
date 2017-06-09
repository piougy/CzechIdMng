package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.entity.BaseTreeEntity;
import eu.bcvsolutions.idm.core.api.service.BaseTreeService;

@Service
public class DefaultBaseTreeService<E extends BaseTreeEntity<E>> implements BaseTreeService<E> {

	@Override
	public boolean validateTreeNodeParents(E treeNode) {
		// TODO: future implementation of all tree was by right left connection.
		// This is temporary solutions
		E tmp = treeNode;
		List<E> listIds = new ArrayList<E>();
		while (tmp.getParent() != null) {
			if (listIds.contains(tmp)) {
				return true;
			}
			listIds.add(tmp);
			tmp = tmp.getParent();
		}
		return false;
	}
	
	@Override
	public boolean validateUniqueName(List<E> treeNodes, E newSavedNode) {
		List<E> copy = new ArrayList<>(treeNodes);
		//
		if (!copy.contains(newSavedNode)) {
			copy.add(newSavedNode);
		}
		// get distinct names and compare to all names
		Long count = copy.stream().map(BaseTreeEntity::getName).distinct().count();
		if (count != copy.size()) {
			return true;
		}
		return false;
	}
}
