package dependencies;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import dependencies.exception.CycleException;

public class SimpleTree<T> {

	T data;
	SimpleTree<T> parent;
	
	List<SimpleTree<T>> children;
	
	public SimpleTree(T data) {
		this(data, null);
	}

	public SimpleTree(T data, SimpleTree<T> parent) {
		this.data = data;
		this.parent = parent;
		children = new ArrayList<>();
	}
	
	public void setParent(SimpleTree<T> parent) {
		this.parent = parent;
	}
	
	public SimpleTree<T> getParent() {
		return this.parent;
	}
	
	public void addChildren(List<SimpleTree<T>> children) throws CycleException {
		for (SimpleTree<T> child : children) {
			this.addChild(child);
		}
	}
	
	public void addChild(SimpleTree<T> child) throws CycleException {
		if(checkHasAncestor(child))
			throw new CycleException(child.getData());
		else {
			child.setParent(parent);
			children.add(child);
		}
	}
	
	public boolean isLeaf() {
		return children.isEmpty();
	}
	
	public List<SimpleTree<T>> getChildren() {
		return children;
	}
	
	public T getData() {
		return data;
	}
	
	public Iterator<T> getChildrenFirstIterator() {
		return new ChildrenFirstIterator(this);
	}
	
	private class ChildrenFirstIterator implements Iterator<T> {
		SimpleTree<T> tree;
		
		public ChildrenFirstIterator(SimpleTree<T> tree) {
			this.tree = tree;
		}

		@Override
		public boolean hasNext() {
			// TODO: implement
			return false;
		}

		@Override
		public T next() {
			// TODO: implement
			return null;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
			
		}
	}
	
	private boolean checkHasAncestor(SimpleTree<T> parent) {
		SimpleTree<T> currentNode = this;
		do {
			if (parent.data.equals(currentNode.data))
				return true;
			
			currentNode = currentNode.parent;
		}
		while(currentNode != null);
		return false;
	}
}
