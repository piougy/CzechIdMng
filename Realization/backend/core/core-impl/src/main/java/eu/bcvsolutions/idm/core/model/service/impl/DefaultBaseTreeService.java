package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.entity.BaseTreeEntity;
import eu.bcvsolutions.idm.core.api.service.BaseTreeService;

@Service
public class DefaultBaseTreeService<E extends BaseTreeEntity<E>> implements BaseTreeService<BaseTreeEntity<E>> {

	@Override
	public boolean validateTreeNodeParents(BaseTreeEntity<E> treeNode) {
		// TODO: future implementation of all tree was by right left connection.
		// This is temporary solutions
		BaseTreeEntity<E> tmp = treeNode;
		List<BaseTreeEntity<E>> listIds = new ArrayList<BaseTreeEntity<E>>();
		while (tmp.getParent() != null) {
			if (listIds.contains(tmp)) {
				return true;
			}
			listIds.add(tmp);
			tmp = tmp.getParent();
		}
		return false;
	}
}
