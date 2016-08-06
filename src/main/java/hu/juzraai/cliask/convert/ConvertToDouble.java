package hu.juzraai.cliask.convert;

import javax.annotation.Nonnull;

/**
 * @author Zsolt Jurányi
 */
public class ConvertToDouble implements ConvertTo<Double> {

	@Override
	@Nonnull
	public Double convert(@Nonnull String rawValue) throws ConvertFailedException {
		try {
			return Double.parseDouble(rawValue);
		} catch (NumberFormatException e) {
			throw new ConvertFailedException("Invalid value for: double");
		}
	}
}
