package app.logging;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * The configuration of the application logging.
 */
public class LoggerConfig {

	public static void setUp() {
		Logger logger = Logger.getRootLogger();
		logger.setLevel(Level.INFO);
		HtmlLogFile.create(logger);
		addConsoleOutput(logger);
	}

	private static void addConsoleOutput(Logger logger) {
		BasicConfigurator.configure();
		ConsoleAppender appender = new ConsoleAppender(new PatternLayout());
		logger.addAppender(appender);
		appender.setTarget(ConsoleAppender.SYSTEM_OUT);
		appender.activateOptions();
	}
}
