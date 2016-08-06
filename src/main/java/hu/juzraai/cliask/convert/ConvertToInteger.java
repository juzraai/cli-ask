package hu.juzraai.cliask.convert;

import javax.annotation.Nonnull;

/**
 * @author Zsolt Jurányi
 */
public class ConvertToInteger implements ConvertTo<Integer> {

	@Override
	@Nonnull
	public Integer convert(@Nonnull String rawValue) throws ConvertFailedException {
		try {
			return Integer.parseInt(rawValue);
		} catch (NumberFormatException e) {
			throw new ConvertFailedException("Invalid value for: integer");
		}
	}
}
