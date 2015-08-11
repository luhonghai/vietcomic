/**
 * Copyright (c) CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */

package com.cmg.mobile.shared.log;

import com.cmg.mobile.shared.data.LogDetail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator Hai Lu
 * @Last changed: $LastChangedDate$
 */

enum LogType {
    INFO, DEBUG, ERROR
}

class LogMessage {
    LogDetail.Type type;
    String[] values;
}

class LogLine {
    Date date;
    LogType type;
    String threadId;
    String message;
    LogMessage logMessage;
}

@SuppressWarnings("serial")
class LogParserException extends Exception {

    public LogParserException(String message) {
        super(message);
    }
}

public class LogParser {
    private static final String[] DATE_PATTERN = {"yyyy-MM-dd HH:mm:ss,SSSS",
            "yyyy MM dd HH:mm:ss,SSSS", "yyyy-MM-dd HH:mm:ss,SSS",
            "yyyy MM dd HH:mm:ss,SSS"};

    private static final String ANALYTICS_PATTERN = "^(\\d{4}-\\d{2}-\\d{2}) (\\d{2}:\\d{2}:\\d{2},\\d{3}) - \\[(DEBUG|INFO|ERROR)(.*)\\] - (.*)$";

    private static Map<LogDetail.Type, String> logPatterns;

    public LogParser() {
        if (logPatterns == null) {
            logPatterns = new HashMap<LogDetail.Type, String>();
            logPatterns.put(LogDetail.Type.OPEN_NEWSLETTER, "^Open newsletter id (.*)$");
            logPatterns.put(LogDetail.Type.DOWNLOAD_NEWLETTER, "^Download newsletter id (.*)$");
            logPatterns.put(LogDetail.Type.READ_NEWSLETTER_PAGE, "^Read newsletter id (.*) page (\\d+)$");
        }
    }

    private Date parseDate(String date) throws LogParserException {
        SimpleDateFormat sdf;
        for (String pattern : DATE_PATTERN) {
            sdf = new SimpleDateFormat(pattern);
            try {
                return sdf.parse(date);
            } catch (ParseException e) {
                // Try again
            }
        }

        throw new LogParserException("Could not parse date");
    }

    public LogLine parseLine(String line) throws LogParserException {
        Pattern p = Pattern.compile(ANALYTICS_PATTERN);
        Matcher m = p.matcher(line);

        if (m.matches()) {
            LogLine out = new LogLine();
            out.date = parseDate(m.group(1) + " " + m.group(2));
            String logType = m.group(3);
            if (logType.equalsIgnoreCase("info")) {
                out.type = LogType.INFO;
            } else if (logType.equalsIgnoreCase("debug")) {
                out.type = LogType.DEBUG;
            } else if (logType.equalsIgnoreCase("error")) {
                out.type = LogType.ERROR;
            }
            out.threadId = m.group(4);
            out.message = m.group(5);
            out.logMessage = parseMessage(out.message);

            return out;
        }
        return null;
    }

    private LogMessage parseMessage(String message) {

        Iterator<LogDetail.Type> keys = logPatterns.keySet().iterator();
        while (keys.hasNext()) {
            LogDetail.Type type = keys.next();
            String pattern = logPatterns.get(type);
            Matcher m = Pattern.compile(pattern).matcher(message);
            if (m.matches()) {
                LogMessage logMessage = new LogMessage();
                logMessage.type = type;
                int count = m.groupCount();
                if (count > 0) {
                    String[] values = new String[count];
                    for (int i = 0; i < count; i++) {
                        values[i] = m.group(i);
                    }
                    logMessage.values = values;
                }

                return logMessage;
            }
        }
        return null;
    }
}
