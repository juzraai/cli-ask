package hu.juzraai.cliask.core;

import hu.juzraai.cliask.annotation.Ask;

import java.lang.reflect.Field;

/**
 * @author Zsolt Jurányi
 */
public class PreparedField {

	private final Field field;
	private final Object object;
	private final boolean relevant;
	private final String name;
	private final Object defaultValue;

	protected PreparedField(Field field, Object object, boolean relevant, String name, Object defaultValue) {
		this.field = field;
		this.object = object;
		this.relevant = relevant;
		this.name = name;
		this.defaultValue = defaultValue;
	}

	public static PreparedField prepare(Field field, Object object) {

		// relevance

		boolean relevant = true;
		try {
			field.setAccessible(true);
			// TODO also check type: String, Integer, basic are OK, others OK if there's @AskAndConvert or @AskRecursively
			relevant = field.isAnnotationPresent(Ask.class);
		} catch (SecurityException e) {
			// TODO LOG
			relevant = false;
		}

		String name = null;
		Object defaultValue = null;
		if (relevant) {

			// name

			Ask ask = field.getAnnotation(Ask.class);
			name = null != ask && ask.value().isEmpty() ? field.getName() : ask.value();

			// default value

			try {
				defaultValue = field.get(object);
			} catch (IllegalAccessException e) {
				// TODO LOG
				relevant = false;
			}
		}

		return new PreparedField(field, object, relevant, name, defaultValue);
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public Field getField() {
		return field;
	}

	public String getName() {
		return name;
	}

	public Object getObject() {
		return object;
	}

	public boolean isRelevant() {
		return relevant;
	}

	public void set(Object value) {
		if (relevant) {
			try {
				field.set(object, value);
			} catch (IllegalAccessException e) {
				// TODO LOG
			}
		} else {
			throw new IllegalStateException("set() called on irrelevant PreparedField");
		}
	}
}
