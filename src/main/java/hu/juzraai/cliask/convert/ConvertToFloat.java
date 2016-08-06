package hu.juzraai.cliask.convert;

import javax.annotation.Nonnull;

/**
 * @author Zsolt Jurányi
 */
public class ConvertToFloat implements ConvertTo<Float> {

	@Override
	@Nonnull
	public Float convert(@Nonnull String rawValue) throws ConvertFailedException {
		try {
			return Float.parseFloat(rawValue);
		} catch (NumberFormatException e) {
			throw new ConvertFailedException("Invalid value for: float");
		}
	}
}
