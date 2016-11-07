package app.util.tables;

@FunctionalInterface
public interface Getter<T> {

	String getText(T element);

}
