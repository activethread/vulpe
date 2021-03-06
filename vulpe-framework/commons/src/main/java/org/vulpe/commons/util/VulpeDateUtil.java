/**
 * Vulpe Framework - Quick and Smart ;)
 * Copyright (C) 2011 Active Thread
 *
 * Este programa é software livre; você pode redistribuí-lo e/ou
 * modificá-lo sob os termos da Licença Pública Geral GNU, conforme
 * publicada pela Free Software Foundation; tanto a versão 2 da
 * Licença como (a seu critério) qualquer versão mais nova.
 *
 * Este programa é distribuído na expectativa de ser útil, mas SEM
 * QUALQUER GARANTIA; sem mesmo a garantia implícita de
 * COMERCIALIZAÇÃO ou de ADEQUAÇÃO A QUALQUER PROPÓSITO EM
 * PARTICULAR. Consulte a Licença Pública Geral GNU para obter mais
 * detalhes.
 *
 * Você deve ter recebido uma cópia da Licença Pública Geral GNU
 * junto com este programa; se não, escreva para a Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
/**
 * Vulpe Framework - Quick and Smart ;)
 * Copyright (C) 2011 Active Thread
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.vulpe.commons.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vulpe.commons.VulpeConstants;
import org.vulpe.commons.enumeration.DaysOfWeek;
import org.vulpe.commons.helper.VulpeConfigHelper;

/**
 * Utility class to date format.
 *
 * @author <a href="mailto:fabio.viana@vulpe.org">Fábio Viana</a>
 */
public final class VulpeDateUtil {

	private static final Logger LOG = LoggerFactory.getLogger(VulpeDateUtil.class);

	public static final String HHMM = "HH:mm";
	public static final String HHMMSS = "HH:mm:ss";

	/** Date format: dd/MM/yyyy. */
	public static final String DDMMYYYY = "dd/MM/yyyy";

	/** Date format: ddMMyyyy. */
	public static final String DDMMYYNB = "ddMMyy";

	/** Date format: dd/MM/yyyy HH:mm:ss. */
	public static final String DDMMYYYYHHMMSS = "dd/MM/yyyy HH:mm:ss";

	/** Date format: dd/MM/yyyy HH:mm. */
	public static final String DDMMYYYYHHMM = "dd/MM/yyyy HH:mm";

	/** Date format: dd/MM/yyyy HH:mm:ss,SSSSSS . */
	public static final String DATE_TIME_FULL = "dd/MM/yyyy HH:mm:ss.SSSSS";

	/** Date format: dd-MM-yyyy hh:mm:ss . */
	public static final String DATE_TIME_AMPM = "dd-MM-yyyy hh:mm:ss";

	public static final Locale LOCALE = VulpeConfigHelper.getLocale();

	public static final SimpleDateFormat SDF = new SimpleDateFormat(DDMMYYYY, LOCALE);

	private VulpeDateUtil() {

	}

	/**
	 * Retrieves current date.
	 *
	 * @author <a href="mailto:smendes@cit.com.br">Silvio Mendes</a>
	 * @since 12/04/2006
	 * @return VulpeDate Data corrente
	 */
	public static Date getCurrentDate() {
		final Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(System.currentTimeMillis());
		return calendar.getTime();
	}

	/**
	 * Retrieves current date plus one millisecond more.
	 *
	 * @return
	 */
	public static Date getCurrentDatePlusOne() {
		final Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(System.currentTimeMillis() + 1);
		return calendar.getTime();
	}

	/**
	 * Retrieves current date decrease one millisecond.
	 *
	 * @return
	 */
	public static Date getCurrentDateMinusOne() {
		final Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(System.currentTimeMillis() - 1);
		return calendar.getTime();
	}

	/**
	 * Retrieves yesterday date.
	 *
	 * @return
	 */
	public static Date getYesterdayDate() {
		final Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(System.currentTimeMillis() - 86400000);
		return calendar.getTime();
	}

	/**
	 * Retrieves current date on TimeStamp format: ddmmaahhmmss.
	 */
	public static String getCurrentTimeStamp() {
		return getTimeStamp(System.currentTimeMillis());
	}

	/**
	 * Retrieves date on TimeStamp format: ddmmaahhmmss.
	 *
	 * @param timestamp
	 * @return
	 */
	public static String getTimeStamp(final long timestamp) {

		final Calendar now = new GregorianCalendar();
		now.setTimeInMillis(timestamp);

		SDF.applyLocalizedPattern("yyyyMMddHHmmss");
		final String ddmmaahhmmss = SDF.format(now.getTime());

		if (LOG.isDebugEnabled()) {
			LOG.debug("Data no formato ddmmaahhmmss: " + ddmmaahhmmss);
		}

		return ddmmaahhmmss;
	}

	/**
	 * Format date with pattern dd/MM/yyyy.
	 *
	 * @return String
	 */
	public static String getDateFormated(final Date data) {
		SDF.applyLocalizedPattern(VulpeDateUtil.DDMMYYYY);
		return SDF.format(data);
	}

	/**
	 * Format date to pattern ddmmYY.
	 *
	 * @return String
	 */
	public static String getDateFormattedNoBar(final Date data) {
		SDF.applyLocalizedPattern(VulpeDateUtil.DDMMYYNB);
		return SDF.format(data);
	}

	/**
	 * Format date to pattern dd/MM/yyyy HH:mm:ss.
	 *
	 * @return String
	 */
	public static String getDateTimeFormatted(final Date data) {
		SDF.applyLocalizedPattern(VulpeDateUtil.DDMMYYYYHHMMSS);
		return SDF.format(data);
	}

	/**
	 * Format date with specific pattern.
	 *
	 * @return java.util.Date
	 */
	public static Date getDate(final String date, final String pattern) {
		Date returnDate = null;
		SDF.applyLocalizedPattern(pattern);
		try {
			returnDate = SDF.parse(date);
		} catch (Exception e) {
			LOG.debug("Invalid date pattern: " + pattern);
		}

		return returnDate;
	}

	/**
	 * Format date with specific pattern.
	 *
	 * @return String
	 */
	public static String getDate(final Date date, final String pattern) {
		String returnDate = null;
		SDF.applyLocalizedPattern(pattern);
		try {
			returnDate = SDF.format(date);
		} catch (Exception e) {
			LOG.debug("Invalid date format: " + pattern);
		}

		return returnDate;
	}

	/**
	 * Checks if is a valid date.
	 *
	 * @param date
	 * @return
	 */
	public static boolean isValidDate(final String date) {
		SDF.applyLocalizedPattern(VulpeDateUtil.DDMMYYYY);
		if (date != null) {
			try {
				return date.equals(SDF.format(SDF.parse(date)));
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	/**
	 *
	 * @param date
	 * @return
	 */
	public static boolean isWeekend(final Date date) {
		final Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
				|| calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
	}

	/**
	 *
	 * @param date
	 * @param field
	 * @param value
	 * @return
	 */
	public static Date overrideField(final Date date, final int field, final int value) {
		final Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.set(field, value);
		return calendar.getTime();
	}

	/**
	 * Format Time in minutes to String.
	 *
	 * @param minutes
	 * @return
	 */
	public static String getFormatedTime(final int minutes) {
		final int hours = minutes / 60;
		final int min = minutes % 60;
		final StringBuilder time = new StringBuilder();
		if (hours < 10) {
			time.append("0");
		}
		time.append(hours).append(":");
		if (min < 10) {
			time.append("0");
		}
		time.append(min);
		return time.toString();
	}

	/**
	 * Format Time in minutes to String.
	 *
	 * @param minutes
	 * @return
	 */
	public static String getFormatedTime(final Integer minutes) {
		return minutes == null ? null : getFormatedTime(minutes.intValue());
	}

	/**
	 * Calculate minutes of duration.
	 *
	 * @param duration
	 * @return
	 */
	public static Integer getTimeInMinutes(final String duration) {
		Integer totalMinutes = null;
		if (!"".equals(duration)) {
			final int index = duration.indexOf(':');

			final Integer hours = Integer.valueOf(duration.substring(0, index));
			final Integer minutes = Integer.valueOf(duration.substring(index + 1));

			totalMinutes = Integer.valueOf((hours.intValue() * 60) + minutes.intValue());
		}
		return totalMinutes;
	}

	/**
	 * Format Date to same date with first hour of day.
	 *
	 * @param date
	 * @return
	 */
	public static Date truncateTime(final Date date) {
		final Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		date.setTime(calendar.getTime().getTime());
		return date;
	}

	public static boolean isValidTime(final String time) {
		return isValidTime(time, false);
	}

	/**
	 * Checks if is a valid Time.
	 *
	 * @param time
	 * @param lenient
	 * @return
	 */
	public static boolean isValidTime(final String time, final boolean lenient) {
		boolean valid = false;
		SDF.applyPattern(HHMM);
		if (lenient) {
			try {
				final int index = time.indexOf(':');
				// Integer hours = Integer.valueOf(time.substring(0, index));
				final Integer minutes = Integer.valueOf(time.substring(index + 1));
				if ((minutes != null) && (minutes.intValue() < 60)) {
					valid = true;
				}
			} catch (Exception e) {
				valid = false;
			}

		} else {
			SDF.setLenient(lenient);
			if (HHMM.length() != time.length()) {
				return valid;
			}
			try {
				SDF.parse(time);
				valid = true;
			} catch (ParseException e) {
				valid = false;
			}
		}
		return valid;
	}

	/**
	 * Checks if is a valid Date.
	 *
	 * @param date
	 * @return
	 */
	public static boolean isDateValid(final String date) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Entering in VulpeDateUtil.isDateValid(String date) - Start");
			LOG.debug("date: " + date);
		}
		boolean valid = false;

		try {
			final String newDate = date.trim();

			if (newDate.length() != DDMMYYYY.length()) {
				throw new ParseException("Tamanho inv�lido para um campo data.", newDate.length());
			}

			try {
				final StringTokenizer stringTokenizer = new StringTokenizer(newDate, "/");

				if (stringTokenizer.countTokens() != 3) {
					return false;
				}

			} catch (NumberFormatException n) {
				return false;
			}

			SDF.applyLocalizedPattern(DDMMYYYY);
			SDF.setLenient(false);
			SDF.parse(newDate);
			valid = true;
		} catch (ParseException pe) {
			valid = false;
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Leaving de VulpeDateUtil.isDateValid(String date) - End");
		}
		return valid;
	}

	/**
	 * Retrieves date on extensive format. Example: 01 of Jully of 2010.
	 *
	 * @return
	 */
	public String getExtensiveDate() {
		final SimpleDateFormat format = (SimpleDateFormat) SimpleDateFormat.getDateInstance(
				DateFormat.MEDIUM, LOCALE);
		format.applyPattern("MMMM");
		final Calendar calendar = Calendar.getInstance();
		final String month = format.format(calendar.getTime());
		final String day = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
		final String year = Integer.toString(calendar.get(Calendar.YEAR));
		final String ofSepareate = " of ";
		return day + ofSepareate + month + ofSepareate + year;
	}

	/**
	 * Calculate diff in days between dates.
	 *
	 * @param begin
	 * @param end
	 * @return
	 */
	public static long getDaysDifference(final Date begin, final Date end) {

		long milliseconds;
		long days;

		final Calendar calendarBegin = new GregorianCalendar();
		final Calendar calendarEnd = new GregorianCalendar();

		calendarBegin.setTime(begin);
		calendarBegin.set(Calendar.HOUR_OF_DAY, 0);
		calendarBegin.set(Calendar.MINUTE, 0);
		calendarBegin.set(Calendar.SECOND, 0);
		calendarBegin.set(Calendar.MILLISECOND, 0);

		calendarEnd.setTime(end);
		calendarEnd.set(Calendar.HOUR_OF_DAY, 0);
		calendarEnd.set(Calendar.MINUTE, 0);
		calendarEnd.set(Calendar.SECOND, 0);
		calendarEnd.set(Calendar.MILLISECOND, 0);

		milliseconds = calendarEnd.getTime().getTime() - calendarBegin.getTime().getTime();

		days = milliseconds / 86400000;

		return days;
	}

	/**
	 * Convert String to Date.
	 *
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	public static Date convertStringToDate(final String date) throws ParseException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Entering in VulpeDateUtil.convertStringToDate(String date) - Start");
			LOG.debug("date: " + date);
		}

		final SimpleDateFormat format = new SimpleDateFormat(DDMMYYYY, LOCALE);
		format.setLenient(false);
		final Date formatedDate = format.parse(date);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Leaving of VulpeDateUtil.convertStringToDate(String date) - End");
		}

		return formatedDate;

	}

	/**
	 * Convert String to Time.
	 *
	 * @param time
	 * @return
	 * @throws ParseException
	 */
	public static Date convertStringToTime(final String time) throws ParseException {
		final SimpleDateFormat timeFormat = new SimpleDateFormat(HHMM, LOCALE);
		timeFormat.setLenient(false);
		return timeFormat.parse(time);
	}

	/**
	 * Convert String to Date Time.
	 *
	 * @param dateTime
	 * @return
	 * @throws ParseException
	 */
	public static Date convertStringToDateTime(final String dateTime) throws ParseException {
		final SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DDMMYYYY + " " + HHMM, LOCALE);
		dateTimeFormat.setLenient(false);
		return dateTimeFormat.parse(dateTime);
	}

	/**
	 *
	 * @param dateTime
	 * @return
	 */
	public static String convertDateTimeToString(final Date dateTime) {
		String formatedDateTime = null;
		if (!VulpeValidationUtil.isEmpty(formatedDateTime)) {
			final SimpleDateFormat formatDateTime = new SimpleDateFormat(DDMMYYYY + " " + HHMM,
					LOCALE);
			formatedDateTime = formatDateTime.format(formatedDateTime);
		}
		return formatedDateTime;
	}

	/**
	 *
	 * @param dateTime
	 * @return
	 */
	public static String convertDateToString(final Date dateTime) {
		String formatedDateTime = null;
		if (!VulpeValidationUtil.isEmpty(dateTime)) {
			final SimpleDateFormat formatDateTime = new SimpleDateFormat(DDMMYYYY, LOCALE);
			formatedDateTime = formatDateTime.format(dateTime);
		}
		return formatedDateTime;
	}

	/**
	 *
	 * @param dateTime
	 * @return
	 */
	public static String convertDateTimeSecondToString(final Date dateTime) {
		String formatedDateTime = null;
		if (!VulpeValidationUtil.isEmpty(dateTime)) {
			final SimpleDateFormat formatDateTime = new SimpleDateFormat(DDMMYYYY + " " + HHMMSS,
					LOCALE);
			formatedDateTime = formatDateTime.format(dateTime);
		}
		return formatedDateTime;
	}

	/**
	 * Convert time to String.
	 *
	 * @param time
	 * @return
	 */
	public static String convertTimeToString(final Date time) {
		String formatedTime = "";
		if (time != null) {
			SDF.applyLocalizedPattern(HHMM);
			formatedTime = SDF.format(time);
		}
		return formatedTime;
	}

	/**
	 * Convert Date to String with day of week. Example: Date: 04/10/2005 Value
	 * returned: Tue
	 *
	 * @param date
	 * @return
	 */
	public static String getExtensiveAbbreviatedDate(final Date date) {
		SDF.applyLocalizedPattern(VulpeConstants.SIMPLE_DATE_FORMAT);
		return SDF.format(date);
	}

	/**
	 * Convert Date to same Date with last minute of day. Example: Input:
	 * 01/01/2005 Output: 01/01/2005 23:59:59
	 *
	 * @param date
	 * @return
	 */
	public static Date createFinalDateQuery(final Date date) {
		final Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTime();
	}

	/**
	 * Convert Date to same Date with first minute of day. Example: Input:
	 * 01/01/2005 Output: 01/01/2005 00:00:00
	 *
	 * @param date
	 * @return
	 */
	public static Date createInitialDateQuery(final Date date) {
		final Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * Calculate diff in milliseconds between dates.
	 *
	 * @param begin
	 * @param end
	 * @return
	 */
	public static long getMillisecondDifference(final Date begin, final Date end) {
		long milliseconds;

		final Calendar dtBegin = new GregorianCalendar();
		final Calendar dtEnd = new GregorianCalendar();

		dtBegin.setTime(begin);
		dtBegin.set(Calendar.HOUR_OF_DAY, dtBegin.get(Calendar.HOUR_OF_DAY));
		dtBegin.set(Calendar.MINUTE, dtBegin.get(Calendar.MINUTE));
		dtBegin.set(Calendar.SECOND, dtBegin.get(Calendar.SECOND));
		dtBegin.set(Calendar.MILLISECOND, dtBegin.get(Calendar.MILLISECOND));

		dtEnd.setTime(end);
		dtEnd.set(Calendar.HOUR_OF_DAY, dtEnd.get(Calendar.HOUR_OF_DAY));
		dtEnd.set(Calendar.MINUTE, dtEnd.get(Calendar.MINUTE));
		dtEnd.set(Calendar.SECOND, dtEnd.get(Calendar.SECOND));
		dtEnd.set(Calendar.MILLISECOND, dtEnd.get(Calendar.MILLISECOND));

		milliseconds = dtEnd.getTime().getTime() - dtBegin.getTime().getTime();
		return milliseconds;
	}

	/**
	 * Calculate diff in minutes between dates.
	 *
	 * @param begin
	 * @param end
	 * @return
	 */
	public static int getMinutesDifference(final Date begin, final Date end) {
		return (int) getMillisecondDifference(begin, end) / 60000;
	}

	/**
	 * Calculate quantity of minutes of task.
	 *
	 * @param dateTimeBegin
	 * @param dateTimeEnd
	 * @param begin
	 * @param end
	 * @return
	 */
	private static int calculateTruncatedTime(final Date dateTimeBegin, final Date dateTimeEnd,
			final Date begin, final Date end) {

		if (begin.compareTo(end) > 0) {
			final Calendar calendarBegin1 = new GregorianCalendar();
			calendarBegin1.setTime(begin);
			calendarBegin1.set(Calendar.HOUR_OF_DAY, 0);
			calendarBegin1.set(Calendar.MINUTE, 0);
			calendarBegin1.set(Calendar.SECOND, 0);

			final Calendar calendarEnd2 = new GregorianCalendar();
			calendarEnd2.setTime(calendarBegin1.getTime());
			calendarEnd2.add(Calendar.DAY_OF_YEAR, 1);

			return calculateTruncatedTime(dateTimeBegin, dateTimeEnd, calendarBegin1.getTime(), end)
					+ calculateTruncatedTime(dateTimeBegin, dateTimeEnd, begin, calendarEnd2
							.getTime());
		}

		int returnedTime = 0;
		final Calendar periodBegin = new GregorianCalendar();
		periodBegin.setTime(begin);
		final Calendar periodEnd = new GregorianCalendar();
		periodEnd.setTime(end);
		final Calendar startActivity = new GregorianCalendar();
		startActivity.setTime(dateTimeBegin);

		periodBegin.set(startActivity.get(Calendar.YEAR), startActivity.get(Calendar.MONTH),
				startActivity.get(Calendar.DATE));
		periodEnd.set(startActivity.get(Calendar.YEAR), startActivity.get(Calendar.MONTH),
				startActivity.get(Calendar.DATE));

		if (periodBegin.getTime().compareTo(periodEnd.getTime()) > 0) {
			periodEnd.add(Calendar.DATE, 1);
		}
		final Date start = periodBegin.getTime();
		final Date finish = periodEnd.getTime();

		final int period = getMinutesDifference(start, finish);
		final int activityTime = getMinutesDifference(dateTimeBegin, dateTimeEnd);

		if (dateTimeBegin.compareTo(start) < 0) {
			returnedTime = Math.max(Math.min(getMinutesDifference(start, dateTimeEnd), period), 0);
		} else if (dateTimeBegin.compareTo(finish) > 0) {
			periodBegin.add(Calendar.DATE, 1);
			returnedTime = Math.max(Math.min(getMinutesDifference(periodBegin.getTime(),
					dateTimeEnd), period), 0);
		} else if (dateTimeEnd.compareTo(finish) < 0) {
			returnedTime = activityTime;
		} else {
			periodBegin.add(Calendar.DATE, 1);
			if (dateTimeEnd.compareTo(periodBegin.getTime()) > 0) {
				returnedTime = activityTime - period;
			} else {
				returnedTime = getMinutesDifference(dateTimeBegin, finish);
			}
		}
		return returnedTime;
	}

	/**
	 * Checks time period.
	 *
	 * @param beginHour
	 * @param endHour
	 * @param hour
	 * @return
	 */
	public static boolean checkTimePeriod(final Date beginHour, final Date endHour, final Date hour) {
		final Calendar calendarBegin = Calendar.getInstance();
		calendarBegin.setTime(beginHour);
		final Calendar calendarEnd = Calendar.getInstance();
		calendarEnd.setTime(endHour);
		final Calendar calendarTime = Calendar.getInstance();
		calendarTime.setTime(hour);

		boolean betweenPeriod = false;

		calendarBegin.set(Calendar.DAY_OF_MONTH, 1);
		calendarBegin.set(Calendar.MONTH, 1);
		calendarBegin.set(Calendar.YEAR, 1970);

		calendarEnd.set(Calendar.DAY_OF_MONTH, 2);
		calendarEnd.set(Calendar.MONTH, 1);
		calendarEnd.set(Calendar.YEAR, 1970);

		calendarTime.set(Calendar.DAY_OF_MONTH, 1);
		calendarTime.set(Calendar.MONTH, 1);
		calendarTime.set(Calendar.YEAR, 1970);

		if (calendarTime.after(calendarBegin) && calendarTime.before(calendarEnd)) {
			betweenPeriod = true;
		} else {
			calendarTime.set(Calendar.DAY_OF_MONTH, 2);
			if (calendarTime.after(calendarBegin) && calendarTime.before(calendarEnd)) {
				betweenPeriod = true;
			}
		}
		return betweenPeriod;
	}

	/**
	 * Retrieves time of activity on period.
	 *
	 * @param dateTimeStart
	 * @param dateTimeEnd
	 * @param start
	 * @param end
	 * @return
	 */
	public static int calculateTruncatedTimeByDays(final Date dateTimeStart,
			final Date dateTimeEnd, final Date start, final Date end) {
		int minutes = 0;
		final Date startActivity = new Date(dateTimeStart.getTime());
		final Date endActivity = new Date(dateTimeEnd.getTime());

		final Date periodBegin = new Date(start.getTime());
		final Date periodEnd = new Date(end.getTime());

		if (startActivity.compareTo(endActivity) < 0) {
			final Calendar calendarBegin = new GregorianCalendar();
			calendarBegin.setTime(periodBegin);
			final Date datePeriodStart = calendarBegin.getTime();

			final Calendar calendarEnd = new GregorianCalendar();
			calendarEnd.setTime(periodEnd);
			calendarEnd.set(Calendar.DAY_OF_MONTH, calendarBegin.get(Calendar.DAY_OF_MONTH));
			calendarEnd.set(Calendar.MONTH, calendarBegin.get(Calendar.MONTH));
			calendarEnd.set(Calendar.YEAR, calendarBegin.get(Calendar.YEAR));
			final Date datePeriodEnd = calendarEnd.getTime();

			Date dtBeginActivity = startActivity;
			Date dtEndActivity = endActivity;
			final Calendar dayEndStart = new GregorianCalendar();
			final Calendar beginNextDay = new GregorianCalendar();
			do {
				dayEndStart.setTime(dtBeginActivity);
				dayEndStart.set(Calendar.HOUR_OF_DAY, 23);
				dayEndStart.set(Calendar.MINUTE, 59);
				dayEndStart.set(Calendar.SECOND, 59);
				dayEndStart.add(Calendar.SECOND, 1);
				final Date dTimeEnd = dayEndStart.getTime();
				if (endActivity.compareTo(dTimeEnd) > 0) {
					dtEndActivity = dTimeEnd;
				} else {
					dtEndActivity = endActivity;
				}
				final int parcialMinutes = calculateTruncatedTime(dtBeginActivity, dtEndActivity,
						datePeriodStart, datePeriodEnd);
				minutes += parcialMinutes;
				beginNextDay.setTime(dtEndActivity);
				dtBeginActivity = beginNextDay.getTime();
			} while (dtEndActivity.compareTo(endActivity) != 0);
		}
		return minutes;
	}

	/**
	 * Adds days on Date.
	 *
	 * @param date
	 * @param numberOfDays
	 * @return
	 */
	public static Date addDaysOnDate(final Date date, final int numberOfDays) {
		Date addedDate = null;
		if (date != null) {
			final Calendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			calendar.add(Calendar.DATE, numberOfDays);
			addedDate = calendar.getTime();
		}
		return addedDate;
	}

	/**
	 * Retrieves Time Zone (GTM)
	 *
	 * @return
	 */
	public static String getOffSetTimeZone() {
		final int off = Calendar.getInstance().get(Calendar.ZONE_OFFSET);
		final StringBuilder offset = new StringBuilder(VulpeConstants.STRING_NULL);
		if (off < 0) {
			offset.append("-");
		} else {
			offset.append("+");
		}
		offset.append(getFormatedTime(Math.abs(off) * 60 / 3600000));
		return offset.toString();
	}

	/**
	 * Calculate diff in days between dates.
	 *
	 * @param minorDate
	 * @param majorDate
	 * @return
	 */
	public static int getDifferencesBetweenIndicateDays(final Date minorDate, final Date majorDate) {
		int numDiff = 0;

		if (majorDate.after(minorDate)) {
			final Calendar calendarMinor = new GregorianCalendar();
			calendarMinor.setTime(minorDate);
			final int dayYearMinor = calendarMinor.get(Calendar.DAY_OF_YEAR);
			final int yearMinor = calendarMinor.get(Calendar.YEAR);

			final Calendar calendarMajor = new GregorianCalendar();
			calendarMajor.setTime(majorDate);
			int dayYearMaior = calendarMajor.get(Calendar.DAY_OF_YEAR);
			final int yearMaior = calendarMajor.get(Calendar.YEAR);

			if (yearMinor < yearMaior) {
				int auxYear = yearMinor;
				final Calendar calendarAux = calendarMinor;
				while (auxYear < yearMaior) {
					dayYearMaior += calendarAux.getActualMaximum(Calendar.DAY_OF_YEAR);
					++auxYear;
					calendarAux.set(Calendar.YEAR, auxYear);
				}
			}

			numDiff = (dayYearMaior - dayYearMinor);
		}

		return numDiff;
	}

	/**
	 * Checks whether the dates are on the same day.
	 *
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static boolean isDatesInSameDay(final Date date1, final Date date2) {
		if (truncateTime(date1).equals(truncateTime(date2))) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if data is between period.
	 *
	 * @param date
	 * @param periodStart
	 * @param periodEnd
	 * @return
	 */
	public static boolean validatePeriodBetweenDate(final Date date, final Date periodStart,
			final Date periodEnd) {
		return (date.after(periodStart) && (periodEnd == null || date.before(periodEnd)));
	}

	/**
	 * Retrieves dates by days of week on month
	 *
	 * @param days
	 * @return
	 */
	public static List<Date> getDatesOnMonth(final DaysOfWeek... daysOfWeek) {
		final List<Date> dates = new ArrayList<Date>();
		if (daysOfWeek != null) {
			final Calendar calendar = Calendar.getInstance();
			final int firstDay = Calendar.getInstance().getActualMinimum(Calendar.DATE);
			final int lastDay = Calendar.getInstance().getActualMaximum(Calendar.DATE);
			final int period = lastDay - firstDay;
			for (int i = 1; i <= period; i++) {
				calendar.set(Calendar.DATE, i);
				final int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
				for (DaysOfWeek day : daysOfWeek) {
					if (dayOfWeek == day.getValue()) {
						dates.add(calendar.getTime());
						continue;
					}
				}
			}
		}
		return dates;
	}

	/**
	 * Retrieves the first date of the month.
	 *
	 * @return
	 */
	public static Date getFirstDateOfTheMonth() {
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * Retrieves the last date of the month.
	 *
	 * @return
	 */
	public static Date getLastDateOfTheMonth() {
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	public static Date getDateOfBusinessDay(int addDays) {
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		if (addDays > 0) {
			int count = 0;
			for (int i = 0; i < addDays; i++) {
				calendar.add(Calendar.DAY_OF_MONTH, 1);
				if (isWeekend(calendar.getTime())) {
					++count;
				}
			}
			calendar.add(Calendar.DAY_OF_MONTH, count);
		} else {
			while (isWeekend(calendar.getTime())) {
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			}
		}
		return calendar.getTime();
	}

}