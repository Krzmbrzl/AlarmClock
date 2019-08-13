package util;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Control;

/**
 * A class containing various util methods
 * 
 * @author Raven
 *
 */
public class Util {
	
	/**
	 * Magnifies the font of the given control by a given factor
	 * 
	 * @param control
	 *            The control whose font should be magnified
	 * @param factor
	 *            The magnification factor
	 */
	public static void magnifyFont(Control control, double factor) {
		FontData[] fontData = control.getFont().getFontData();
		fontData[0].setHeight((int) (fontData[0].getHeight() * factor));
		control.setFont(new Font(control.getDisplay(), fontData));
	}
	
	/**
	 * Gets the file extension for the given file
	 * 
	 * @param file
	 *            The file to check
	 * @return The respective extension or <code>null</code> if none could be
	 *         found
	 */
	public static String getFileExtension(File file) {
		if (!file.isFile()) {
			return null;
		}
		
		return (file.getName().contains("."))
				? file.getName().substring(file.getName().lastIndexOf(".") + 1)
				: "";
	}
	
	/**
	 * Gets the current time from a NTP server.
	 * 
	 * @return The time in milliseconds.
	 * @throws NTPException
	 *             This is thrown when the connection to the NTP servers failed
	 */
	public static long getNTPTime() throws NTPException {
		String[] hosts = new String[] { "0.pool.ntp.org", "1.pool.ntp.org", "2.pool.ntp.org", "3.pool.ntp.org", "0.de.pool.ntp.org",
				"1.de.pool.ntp.org", "2.de.pool.ntp.org", "3.de.pool.ntp.org" };
		
		NTPUDPClient client = new NTPUDPClient();
		// We want to timeout if a response takes longer than 5 seconds
		client.setDefaultTimeout(5000);
		
		for (String host : hosts) {
			try {
				InetAddress hostAddr = InetAddress.getByName(host);
				TimeInfo info = client.getTime(hostAddr);
				
				client.close();
				
				return info.getMessage().getTransmitTimeStamp().getTime();
				
			} catch (IOException e) {
				// Don't print as the unavailability of a single server isn't important
				// e.printStackTrace();
			}
		}
		
		client.close();
		
		throw new NTPException("All configured NTP servers are unreachable!");
	}
}
