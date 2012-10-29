/*
 * DateUtil.java
 * Oct 28, 2012
 *
 * Simple Web Server (SWS) for EE407/507 and CS455/555
 * 
 * Copyright (C) 2011 Chandan Raj Rupakheti, Clarkson University
 * 
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either 
 * version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/lgpl.html>.
 * 
 * Contact Us:
 * Chandan Raj Rupakheti (rupakhcr@clarkson.edu)
 * Department of Electrical and Computer Engineering
 * Clarkson University
 * Potsdam
 * NY 13699-5722
 * http://clarkson.edu/~rupakhcr
 */

package util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class DateUtil {

	/* We have to support the following formats:
	 * Sun, 06 Nov 1994 08:49:37 GMT  ; RFC 822, updated by RFC 1123
	 * Sunday, 06-Nov-94 08:49:37 GMT ; RFC 850, obsoleted by RFC 1036
	 * Sun Nov  6 08:49:37 1994       ; ANSI C's asctime() format
	 */
	private static final ThreadLocal<DateFormat> RFC822_FORMAT = new ThreadLocal<DateFormat>(){
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZ");
		}
	};
	private static final ThreadLocal<DateFormat> RFC850_FORMAT
	= new ThreadLocal<DateFormat>(){
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("EEE, dd-MMM-yy HH:mm:ss zzz");
		}
	};
	private static final ThreadLocal<DateFormat> ASCTIME_FORMAT
	= new ThreadLocal<DateFormat>(){
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy");
		}
	};

	/**
	 * Constructs a Date object corresponding to the provided String representation. The String must comply with HTTP standards for date formats.
	 * @param dateString - The String representing the date
	 * @return a Date object corresponding to the date, or null if the String did not contain a correctly formatted date
	 */
	public static Date getDateFromHttpRequestString(String dateString) {
		Date result = new Date();
		boolean correctFormat = true;
		try {
			result = RFC822_FORMAT.get().parse(dateString);
			correctFormat = true;
		} catch (ParseException e) {
			correctFormat = false;
		}

		if (correctFormat == false) {
			// try next format
			try {
				result = RFC850_FORMAT.get().parse(dateString);
				correctFormat = true;
			} catch (ParseException e) {
				// still wrong format
				correctFormat = false;
			}
		}
		
		if (correctFormat == false) {
			try {
				result = ASCTIME_FORMAT.get().parse(dateString);
				correctFormat = true;
			} catch (ParseException e) {
				correctFormat = false;
			}
		}
		
		if (!correctFormat) {
			result = null;
		}
		return result;
	}
}
