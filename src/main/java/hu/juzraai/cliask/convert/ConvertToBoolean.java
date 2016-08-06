package hu.juzraai.cliask.convert;

import javax.annotation.Nonnull;

/**
 * @author Zsolt Jurányi
 */
public class ConvertToBoolean implements ConvertTo<Boolean> {

	private static final String TRUE_PATTERN = "TRUE|YES|ON|1";
	private static final String FALSE_PATTERN = "FALSE|NO|OFF|0";

	@Override
	@Nonnull
	public Boolean convert(@Nonnull String rawValue) throws ConvertFailedException {
		if (rawValue.toUpperCase().matches(TRUE_PATTERN)) {
			return Boolean.TRUE;
		}
		if (rawValue.toUpperCase().matches(FALSE_PATTERN)) {
			return Boolean.FALSE;
		}
		throw new ConvertFailedException(String.format("Invalid boolean value, specify one of these: %s|%s", TRUE_PATTERN, FALSE_PATTERN);
	}
}
