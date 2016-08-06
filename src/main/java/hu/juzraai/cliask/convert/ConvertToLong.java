package hu.juzraai.cliask.convert;

import javax.annotation.Nonnull;

/**
 * @author Zsolt Jurányi
 */
public class ConvertToLong implements ConvertTo<Long> {

	@Override
	@Nonnull
	public Long convert(@Nonnull String rawValue) throws ConvertFailedException {
		try {
			return Long.parseLong(rawValue);
		} catch (NumberFormatException e) {
			throw new ConvertFailedException("Invalid value for: long");
		}
	}
}
