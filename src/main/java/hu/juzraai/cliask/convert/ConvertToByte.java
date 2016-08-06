package hu.juzraai.cliask.convert;

import javax.annotation.Nonnull;

/**
 * @author Zsolt Jurányi
 */
public class ConvertToByte implements ConvertTo<Byte> {

	@Override
	@Nonnull
	public Byte convert(@Nonnull String rawValue) throws ConvertFailedException {
		try {
			return Byte.parseByte(rawValue);
		} catch (NumberFormatException e) {
			throw new ConvertFailedException("Invalid value for: byte");
		}
	}
}
