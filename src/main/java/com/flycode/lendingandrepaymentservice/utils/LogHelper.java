package com.flycode.lendingandrepaymentservice.utils;

import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;


public class LogHelper {
    @Accessors(fluent = true)
    public static class LogBuilder {

        private final Logger logger;
        private StringBuilder stringBuilder;

        @Setter
        private String logMsg;
        @Setter
        private String logDetailedMsg;

        public LogBuilder(Logger logger) {
            this.logger = logger;
        }

        private void build() {
            stringBuilder = new StringBuilder();
            if (logMsg != null) stringBuilder.append("LogMsg=").append(logMsg);
            if (logDetailedMsg != null) stringBuilder.append(" | LogDetailedMsg=").append(logDetailedMsg);
        }

        public void error() {
            build();
            logger.error(StringEscapeUtils.escapeJava(stringBuilder.toString()));
        }

        public void info() {
            build();
            logger.info(StringEscapeUtils.escapeJava(stringBuilder.toString()));
        }
    }

    public static LogBuilder builder(Logger logger) {
        return new LogBuilder(logger);
    }
}
