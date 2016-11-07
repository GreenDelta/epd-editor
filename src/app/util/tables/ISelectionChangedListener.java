package app.util.tables;

@FunctionalInterface
public interface ISelectionChangedListener<T> {

	public void selectionChanged(T selection);

}
