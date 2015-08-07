package eredmel.logger;

/**
 * An exception that is used to break out of evaluation. If one of these is
 * raised in logging an error, it halts evaluation until the top level rather
 * than triggering an Error Raised in Displaying Error.
 * 
 * @author Kavi Gupta
 */
public class ControlFlow extends RuntimeException {}
