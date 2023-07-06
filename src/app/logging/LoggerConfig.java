package app.logging;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * The configuration of the application logging.
 */
public class LoggerConfig {

	static void setLevel(Level level) {
		if (level == null)
			return;

		var domainLog = LoggerFactory.getLogger("epd.editor");
		if (domainLog instanceof Logger log) {
			log.setLevel(level);
		}
		domainLog.info("set log-level=" + level);

		var rootLog = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		if (rootLog instanceof Logger log) {
			log.setLevel(translateForRoot(level));
		}
	}

	public static void setUp() {
		var root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		if (!(root instanceof Logger log))
			return;

		var html = HtmlLog.createAppender();
		if (html != null) {
			log.addAppender(html);
		}

		setLevel(Level.INFO);
	}


	/**
	 * Depending on the log-level of the EPD Editor domain logger,
	 * we hide some details for the root logger.
	 */
	private static Level translateForRoot(Level level) {
		if (level == null)
			return Level.ERROR;
		return switch (level.levelInt) {
			case Level.WARN_INT -> Level.ERROR;
			case Level.INFO_INT -> Level.WARN;
			case Level.DEBUG_INT -> Level.INFO;
			default -> level;
		};
	}

}
