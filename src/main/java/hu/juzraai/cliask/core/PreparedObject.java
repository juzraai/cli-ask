package hu.juzraai.cliask.core;

import hu.juzraai.cliask.annotation.Ask;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Zsolt Jurányi
 */
public class PreparedObject {

	private final Class<?> clazz;
	private final Object object;
	private final String name;
	private final List<PreparedField> fields;

	protected PreparedObject(Class<?> clazz, Object object, String name, List<PreparedField> fields) {
		this.clazz = clazz;
		this.object = object;
		this.name = name;
		this.fields = fields;
	}

	public static PreparedObject prepare(Object object) {
		Class<?> clazz = object.getClass();

		// name

		Ask ask = clazz.getAnnotation(Ask.class);
		String name = null != ask && !ask.value().isEmpty() ? ask.value() : "";

		// fields

		List<PreparedField> fields = new ArrayList<>();
		for (Field field : clazz.getDeclaredFields()) {
			PreparedField preparedField = PreparedField.prepare(field, object);
			if (preparedField.isRelevant()) {
				fields.add(preparedField);
			}
		}

		return new PreparedObject(clazz, object, name, fields);
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public List<PreparedField> getFields() {
		return fields;
	}

	public String getName() {
		return name;
	}

	public Object getObject() {
		return object;
	}


}
