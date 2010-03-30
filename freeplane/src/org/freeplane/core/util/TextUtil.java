package org.freeplane.core.util;

import java.text.MessageFormat;
import java.util.regex.Matcher;

import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;

public class TextUtil {
	/**
	 * Example: expandPlaceholders("Hello $1.","Dolly"); => "Hello Dolly."
	 */
	static String expandPlaceholders(final String message, final String s1) {
		String result = message;
		if (s1 != null) {
			result = result.replaceAll("\\$1", Matcher.quoteReplacement(s1));
		}
		return result;
	}

	static String expandPlaceholders(final String message, final String s1, final String s2) {
		String result = message;
		if (s1 != null) {
			result = result.replaceAll("\\$1", Matcher.quoteReplacement(s1));
		}
		if (s2 != null) {
			result = result.replaceAll("\\$2", Matcher.quoteReplacement(s2));
		}
		return result;
	}

	public static String format(final String resourceKey, final Object... messageArguments) {
		final MessageFormat formatter = new MessageFormat(TextUtil.getText(resourceKey));
		final String stringResult = formatter.format(messageArguments);
		return stringResult;
	}

	public static String formatText(final String key, final String s1) {
		final String format = TextUtil.getText(key);
		if (format == null) {
			return null;
		}
		return TextUtil.expandPlaceholders(format, s1);
	}

	public static String formatText(final String key, final String s1, final String s2) {
		final String format = TextUtil.getText(key);
		if (format == null) {
			return null;
		}
		return TextUtil.expandPlaceholders(format, s1, s2);
	}

	public static String getOptionalText(final String string) {
		return string == null ? null : TextUtil.getText(string);
	}

	public static String removeMnemonic(final String rawLabel) {
		return rawLabel.replaceFirst("&([^ ])", "$1");
	}

	/**
	 * Removes the "TranslateMe" sign from the end of not translated texts.
	 */
	// TODO ARCH rladstaetter 15.02.2009 method should have no need for existance! the build process should filter out resources not fit for production.
	public static String removeTranslateComment(String inputString) {
		if (inputString != null && inputString.endsWith(ResourceBundles.POSTFIX_TRANSLATE_ME)) {
			inputString = inputString
			    .substring(0, inputString.length() - ResourceBundles.POSTFIX_TRANSLATE_ME.length());
		}
		return inputString;
	}

	public static NamedObject createTranslatedString(final String key) {
		final String fs = TextUtil.getText(key);
		return new NamedObject(key, fs);
	}

	public static String getText(final String key) {
		if (key == null) {
			return null;
		}
		return ((ResourceBundles) ResourceController.getResourceController().getResources()).getResourceString(key);
	}

	public static String getText(final String key, final String defaultString) {
		if (key == null) {
			return defaultString;
		}
		return ((ResourceBundles) ResourceController.getResourceController().getResources()).getResourceString(key,
		    defaultString);
	}
}