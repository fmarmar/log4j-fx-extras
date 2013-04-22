package me.fx.log4j.layout;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

/**
 * 
 * @author fmarmar
 *
 */
public class MaskMessagePatternLayout extends PatternLayout {

	private static final String EMPTY_STRING = "";

	public static final String DEFAULT_REPLACEMENT = "****";

	private Pattern maskPattern;

	private int numGroups;

	private String replacement;

	public MaskMessagePatternLayout() {
		replacement = DEFAULT_REPLACEMENT;
	}

	@Override
	public String format(LoggingEvent event) {

		if (maskPattern == null) {
			return super.format(event);
		} else {
			return super.format(cloneLogginEvent(event, mapMessage(event.getRenderedMessage())));
		}
	}

	String mapMessage(String originalMessage) {

		String maskedMsg = originalMessage;

		if (originalMessage != null) {

			Matcher matcher = maskPattern.matcher(originalMessage);

			if (matcher.find()) {

				final StringBuilder msg = new StringBuilder(originalMessage.length());
				int beginIdx = 0;
				int endIdx = -1;

				do {

					for (int groupIdx=1; groupIdx<=numGroups; groupIdx++) {

						endIdx = matcher.start(groupIdx);

						if (endIdx > beginIdx) {
							msg.append(originalMessage.subSequence(beginIdx, endIdx)).append(replacement);
							beginIdx = matcher.end(groupIdx);
						} 

					}

				} while (matcher.find());

				if (beginIdx < originalMessage.length()) {
					msg.append(originalMessage.substring(beginIdx));
				}

				maskedMsg = msg.toString();

			}

		}

		return maskedMsg;

	}

	private LoggingEvent cloneLogginEvent(LoggingEvent originalEvent, Object newMessage) {
		return new LoggingEvent(originalEvent.getFQNOfLoggerClass(), originalEvent.getLogger(), originalEvent.getTimeStamp(), originalEvent.getLevel(), newMessage, originalEvent.getThreadName(), originalEvent.getThrowableInformation(), originalEvent.getNDC(), originalEvent.getLocationInformation(), originalEvent.getProperties());
	}

	public void setMaskRegex(String maskRegex) {
		try {
			maskPattern = Pattern.compile(maskRegex);
			numGroups = maskPattern.matcher(EMPTY_STRING).groupCount();

			if (numGroups == 0) {
				LogLog.warn("maskRegex '" + maskRegex + "' has no capturing groups for replacement");
				maskPattern = null;
			}

		} catch (PatternSyntaxException psE) {
			LogLog.error("PatternSyntaxError: " + psE, psE);
		}
	}

	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}

}