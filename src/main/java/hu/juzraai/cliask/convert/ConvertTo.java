package hu.juzraai.cliask.convert;

import javax.annotation.Nonnull;

/**
 * @author Zsolt Jurányi
 */
public interface ConvertTo<T> {

	@Nonnull
	T convert(@Nonnull String rawValue) throws ConvertFailedException;
}
