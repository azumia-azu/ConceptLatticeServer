package fca.messages;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author Ludovic Thomas
 * @version 1.0
 */
public class GUIMessages {
	private static final String BUNDLE_NAME = "fca.messages.GUIMessages"; //$NON-NLS-1$
	
	private GUIMessages() {
	}
	
	public static String getString(String key) {
		try {
			return key;
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
