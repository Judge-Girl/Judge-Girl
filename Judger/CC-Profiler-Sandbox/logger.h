#ifndef JUDGER_LOGGER_H
#define JUDGER_LOGGER_H

#define LOG_LEVEL_FATAL 0
#define LOG_LEVEL_WARNING 1
#define LOG_LEVEL_INFO 2
#define LOG_LEVEL_DEBUG 3


FILE *log_open(const char *log_path);

void log_close(FILE *log_fp);

void log_write(int level, const char *source_filename, const int line_number, const char *submission_id, const FILE *log_fp, const char *, ...);

#ifdef JUDGER_DEBUG
#define LOG_DEBUG(submission_id, log_fp, x...)   log_write(LOG_LEVEL_DEBUG, __FILE__, __LINE__, submission_id, log_fp, ##x)
#else
#define LOG_DEBUG(submission_id, log_fp, x...)
#endif

#define LOG_INFO(submission_id, log_fp, x...)  log_write(LOG_LEVEL_INFO, __FILE__, __LINE__, submission_id, log_fp, ##x)
#define LOG_WARNING(submission_id, log_fp, x...) log_write(LOG_LEVEL_WARNING, __FILE__, __LINE__, submission_id, log_fp, ##x)
#define LOG_FATAL(submission_id, log_fp, x...)   log_write(LOG_LEVEL_FATAL, __FILE__, __LINE__, submission_id, log_fp, ##x)

#endif //JUDGER_LOGGER_H