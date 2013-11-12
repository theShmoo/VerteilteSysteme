/**
 * 
 */
package util;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * UserLoader loads all users from the properties file
 * 
 * @author David
 */
public final class UserLoader {
	
	private final static ResourceBundle bundle = ResourceBundle.getBundle("user");

	/**
	 * Returns all users in the user.properties file
	 * 
	 * @return all users in the user.properties file
	 */
	public static Set<String> load() {
		Set<String> set = bundle.keySet();
		Set<String> out = new HashSet<String>();
		for(String s : set){
			out.add(s.split("\\.")[0]);
		}
		return out;
	}
}
