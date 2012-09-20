package dependencies;

import java.util.ArrayList;
import java.util.List;

public class SimpleTree<T> {

	T data;
	List<SimpleTree<T>> children;
	
	public SimpleTree(T data) {
		this.data = data;
		children = new ArrayList<>();
	}
	
	public void addChildren(List<SimpleTree<T>> children) {
		children.addAll(children);
	}
	
	public void addChild(SimpleTree<T> child) {
		children.add(child);
	}
	
	public boolean isLeaf() {
		return children.isEmpty();
	}
	
	public List<SimpleTree<T>> getChildren() {
		return children;
	}
	
	public T getRootData() {
		return data;
	}
}
