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
	
	public Iterator<T> getChildrenFirstIterator() {
		return new ChildrenFirstIterator(this);
	}
	
	private class ChildrenFirstIterator implements Iterator<T> {
		private final SimpleTree<T> root;
		private SimpleTree<T> node;
		private int pos = 0;
		
		public ChildrenFirstIterator(SimpleTree<T> root) {
			this.root = root;
			setToFirst();
		}

		@Override
		public boolean hasNext() {
			return (this.node != null);
		}

		@Override
		public T next() {
			if (!hasNext())
				throw new NoSuchElementException();
			
			T result = this.node.data;
			next(this.node);
			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		private void next(SimpleTree<T> previous) {
			if (!hasNext()) {
				setToFirst();
			}
			
			if (this.pos < 0) {
				this.node = root;
				return;
			}
			
			SimpleTree<T> parent = previous.parent;
			if (parent == null) {
				// previous was the root
				// no next element
				this.node = null;
				return;
			}
			
			this.pos++;
			
			if (parent.children.size() <= this.pos) {
				this.pos = getIndex(parent);
				next(parent);
			}
			this.node = (SimpleTree<T>) parent.children.get(this.pos);
		}

		private void setToFirst() {
			this.node = this.root;
			while(this.node.children.size() > 0) {
				this.node = (SimpleTree<T>) this.node.children.get(0);
			}
		}
		
		private int getIndex(SimpleTree<T> node) {
			if(node.parent == null) return -1;
			return node.parent.children.indexOf(this);
		}
	}
}
