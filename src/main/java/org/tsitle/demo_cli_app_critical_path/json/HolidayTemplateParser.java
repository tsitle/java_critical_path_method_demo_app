package org.tsitle.demo_cli_app_critical_path.json;

import io.github.tsitle.criticalpath.exceptions.InvalidInputDataException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HolidayTemplateParser {
	private record TemplatePartValue(String desc, int value, int delta) { }

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private static final String FIELD_DESC_YEAR = "year";
	private static final String FIELD_DESC_MONTH = "month";
	private static final String FIELD_DESC_DAY = "day";

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	public static @NonNull LocalDate parse(@Nullable String templateStr) throws InvalidInputDataException {
		if (templateStr == null) {
			throw new InvalidInputDataException("Holiday template cannot be null");
		}

		List<String> templateParts = parseTemplateParts(templateStr);

		try {
			TemplatePartValue tpvYear = convertYearToTPV(templateParts.get(0));
			TemplatePartValue tpvMonth = convertMonthToTPV(templateParts.get(1));
			TemplatePartValue tpvDay = convertDayToTPV(templateParts.get(2));

			return getLocalDateFromTemplateParts(tpvYear, tpvMonth, tpvDay);
		} catch (DateTimeException e) {
			throw new InvalidInputDataException("Holiday template '" + templateStr + "' cannot be parsed: " +
					e.getMessage());
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private static @NonNull List<String> parseTemplateParts(String templateStr) throws InvalidInputDataException {
		final String originalTemplateStr = templateStr;

		boolean inBrackets = false;
		List<String> templateParts = new ArrayList<>();
		StringBuilder currentPart = new StringBuilder();
		boolean havePendingData = false;
		boolean areBracketsAllowed = true;
		while (! templateStr.isEmpty()) {
			boolean addToCurrentPart = false;
			boolean moveToParts = false;
			if (templateStr.charAt(0) == '<') {
				if (inBrackets) {
					throw new InvalidInputDataException("Nested brackets found in Holiday template: '" +
							originalTemplateStr + "'");
				}
				if (! areBracketsAllowed) {
					throw new InvalidInputDataException("Unexpected '<' found in Holiday template: '" +
							originalTemplateStr + "'");
				}
				inBrackets = true;
			} else if (templateStr.charAt(0) == '>') {
				if (! inBrackets) {
					throw new InvalidInputDataException("Unmatched '>' found in Holiday template: '" +
							originalTemplateStr + "'");
				}
				inBrackets = false;
				areBracketsAllowed = false;
			} else if (templateStr.charAt(0) == '-' && ! inBrackets) {
				moveToParts = true;
			} else {
				addToCurrentPart = true;
			}
			if (moveToParts) {
				templateParts.add(currentPart.toString().strip());
				currentPart = new StringBuilder();
				havePendingData = false;
				areBracketsAllowed = true;
			} else if (addToCurrentPart) {
				currentPart.append(templateStr.charAt(0));
				havePendingData = true;
			}
			templateStr = templateStr.substring(1);
		}
		if (havePendingData) {
			templateParts.add(currentPart.toString().strip());
		}
		if (templateParts.size() != 3) {
			throw new InvalidInputDataException("Invalid Holiday template: '" +
					originalTemplateStr + "' - invalid number of segments");
		}
		return templateParts;
	}

	private static @NonNull TemplatePartValue convertYearToTPV(String templatePart) throws InvalidInputDataException {
		return convertXxxToTPV(FIELD_DESC_YEAR, templatePart, "\\d{4}", "YEAR.*");
	}

	private static @NonNull TemplatePartValue convertMonthToTPV(String templatePart) throws InvalidInputDataException {
		return convertXxxToTPV(FIELD_DESC_MONTH, templatePart, "\\d{2}", "MONTH.*");
	}

	private static @NonNull TemplatePartValue convertDayToTPV(String templatePart) throws InvalidInputDataException {
		return convertXxxToTPV(FIELD_DESC_DAY, templatePart, "\\d{2}", "DAY.*");
	}

	private static @NonNull TemplatePartValue convertXxxToTPV(
				String desc,
				String templatePart,
				String regexNum,
				String regexAlphaNum
			) throws InvalidInputDataException {
		validateTemplatePartStr(desc, templatePart);
		if (templatePart.matches(regexNum)) {
			int value = parseNumber(templatePart);
			validateTemplatePartNumber(desc, value);
			return new TemplatePartValue(desc, value, 0);
		}
		final String templatePartUpper = templatePart.toUpperCase();
		if (templatePartUpper.matches(regexAlphaNum)) {
			return parseOneTemplatePart(desc, templatePartUpper);
		}
		throwForTemplatePart(desc, templatePart);
		return new TemplatePartValue(desc, 0, 0);  // only for compiler
	}

	private static void validateTemplatePartStr(String desc, String templatePart) throws InvalidInputDataException {
		if (templatePart.isBlank()) {
			throw new InvalidInputDataException("Holiday template field '" + desc + "' cannot be blank");
		}
	}

	private static void validateTemplatePartNumber(String desc, int value) throws InvalidInputDataException {
		final int maxValue = switch (desc) {
				case FIELD_DESC_YEAR -> 9999;
				case FIELD_DESC_MONTH -> 12;
				default -> 31;
			};
		if (value < 1) {
			throw new InvalidInputDataException("Holiday template field '" + desc + "' has invalid value " +
					"(is=" + value + ", must be >= 1)");
		}
		if (value > maxValue) {
			throw new InvalidInputDataException("Holiday template field '" + desc + "' has invalid value " +
					"(is=" + value + ", must be <= " + maxValue + ")");
		}
	}

	private static int parseNumber(String templatePart) throws InvalidInputDataException {
		try {
			return Integer.parseInt(templatePart);
		} catch (NumberFormatException e) {
			throw new InvalidInputDataException("Invalid integer value: '" + templatePart + "'");
		}
	}

	private static @NonNull TemplatePartValue parseOneTemplatePart(String desc, String templateStr) throws InvalidInputDataException {
		final String originalTemplateStr = templateStr;

		StringBuilder partDesc = new StringBuilder();
		boolean havePartArithmetic = false;
		boolean partArithmetic = false;
		StringBuilder partValue = new StringBuilder();
		StringBuilder currentPart = new StringBuilder();
		boolean havePendingData = false;
		while (! templateStr.isEmpty()) {
			if (templateStr.charAt(0) == '+' || templateStr.charAt(0) == '-') {
				if (havePartArithmetic) {
					throw new InvalidInputDataException("Unexpected '" + templateStr.charAt(0) +
							"' found in Holiday template field '" + desc + "': '" + originalTemplateStr + "'");
				}
				partArithmetic = (templateStr.charAt(0) == '+');
				havePartArithmetic = true;
				partDesc = new StringBuilder(currentPart.toString().strip());
				currentPart = new StringBuilder();
				havePendingData = false;
			} else {
				currentPart.append(templateStr.charAt(0));
				havePendingData = true;
			}
			templateStr = templateStr.substring(1);
		}
		if (havePendingData) {
			String pending = currentPart.toString().strip();
			if (! havePartArithmetic) {
				partDesc.append(pending);
			} else {
				partValue.append(pending);
			}
		}

		final String partDescAsStr = partDesc.toString();
		final String partValueAsStr = partValue.toString();
		if (havePartArithmetic && partValueAsStr.isBlank()) {
			throw new InvalidInputDataException("Invalid Holiday template field '" + desc + "': '" +
					originalTemplateStr + "' - value in arithmetic operation cannot be blank");
		}

		switch (desc) {
			case FIELD_DESC_YEAR: if (! partDescAsStr.equals("YEAR")) { throwForTemplatePart(desc, partDescAsStr); } break;
			case FIELD_DESC_MONTH: if (! partDescAsStr.equals("MONTH")) { throwForTemplatePart(desc, partDescAsStr); } break;
			default: if (! partDescAsStr.equals("DAY")) { throwForTemplatePart(desc, partDescAsStr); } break;
		}

		final int deltaValueAsInt = (havePartArithmetic ? (parseNumber(partValueAsStr) * (partArithmetic ? 1 : -1)) : 0);

		LocalDate tmpNow = LocalDate.now();
		final int absValueAsInt = switch (desc) {
				case FIELD_DESC_YEAR -> tmpNow.getYear();
				case FIELD_DESC_MONTH -> tmpNow.getMonthValue();
				default -> tmpNow.getDayOfMonth();
			};
		return new TemplatePartValue(desc, absValueAsInt, deltaValueAsInt);
	}

	private static void throwForTemplatePart(String desc, String templatePart) throws InvalidInputDataException {
		throw new InvalidInputDataException("Holiday template field '" + desc + "' has invalid value '" + templatePart + "'");
	}

	private static @NonNull LocalDate getLocalDateFromTemplateParts(
				TemplatePartValue tpvYear,
				TemplatePartValue tpvMonth,
				TemplatePartValue tpvDay
			) {
		LocalDate tmp = LocalDate.of(
				tpvYear.value(),
				tpvMonth.value(),
				tpvDay.value()
			);
		if (tpvYear.delta() != 0) {
			tmp = (tpvYear.delta() > 0 ? tmp.plusYears(tpvYear.delta()) : tmp.minusYears(tpvYear.delta() * -1));
		}
		if (tpvMonth.delta() != 0) {
			tmp = (tpvMonth.delta() > 0 ? tmp.plusMonths(tpvMonth.delta()) : tmp.minusMonths(tpvMonth.delta() * -1));
		}
		if (tpvDay.delta() != 0) {
			tmp = (tpvDay.delta() > 0 ? tmp.plusDays(tpvDay.delta()) : tmp.minusDays(tpvDay.delta() * -1));
		}
		return tmp;
	}
}
