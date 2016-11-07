package app.util.tables;

@FunctionalInterface
public interface Setter<T> {

	void setText(T element, String text);

}
