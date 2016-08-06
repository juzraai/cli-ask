package hu.juzraai.cliask.convert;

import javax.annotation.Nonnull;

/**
 * @author Zsolt Jurányi
 */
public class ConvertToShort implements ConvertTo<Short> {

	@Override
	@Nonnull
	public Short convert(@Nonnull String rawValue) throws ConvertFailedException {
		try {
			return Short.parseShort(rawValue);
		} catch (NumberFormatException e) {
			throw new ConvertFailedException("Invalid value for: short");
		}
	}
}
