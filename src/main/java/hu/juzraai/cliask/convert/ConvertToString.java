package hu.juzraai.cliask.convert;

import javax.annotation.Nonnull;

/**
 * @author Zsolt Jurányi
 */
public class ConvertToString implements ConvertTo<String> {

	@Override
	@Nonnull
	public String convert(@Nonnull String rawValue) throws ConvertFailedException {
		return rawValue;
	}
}
