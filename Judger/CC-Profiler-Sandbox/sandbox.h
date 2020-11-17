#ifndef JUDGER_SANDBOX_H
#define JUDGER_SANDBOX_H

#include <string.h>
#include "profiler.h"

//if the directory have been mounted, we must first unmount it before remove it, or it will cause horrible system crash
#define CHILD_ERROR_EXIT(error_code)\
    {\
        LOG_FATAL(submission_id, log_fp, "Error: System errno: %s; Internal errno: "#error_code, strerror(errno)); \
        close_file(input_file); \
        if (output_file == error_file) { \
            close_file(output_file); \
        } else { \
            close_file(output_file); \
            close_file(error_file);  \
        } \
        raise(SIGUSR1);  \
        exit(EXIT_FAILURE); \
    }


int _cgcreate(char *controller, char *group, uid_t uid, gid_t gid);
int _cgset_memory(char *controller, char *group, long long max_memory);
void child_process(FILE *log_fp, struct config *_config, int mount_list_len);

#endif //JUDGER_SANDBOX_H