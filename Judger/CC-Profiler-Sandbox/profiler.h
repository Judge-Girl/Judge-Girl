#ifndef JUDGER_PROFILER_H
#define JUDGER_PROFILER_H

#include <sys/types.h>
#include <stdio.h>

// (ver >> 16) & 0xff, (ver >> 8) & 0xff, ver & 0xff  -> real version
#define VERSION 0x020101

#define UNLIMITED -1

#define LOG_ERROR(error_code) LOG_FATAL(submission_id, log_fp, "Error: "#error_code);

#define ERROR_EXIT(error_code)\
    {\
        LOG_ERROR(error_code);  \
        _result->error = error_code; \
        _result->result = SYSTEM_ERROR; \
        log_close(log_fp);  \
        return; \
    }

#define ARGS_MAX_NUMBER 256
#define ENV_MAX_NUMBER 256
#define MAX_PATH_LENGTH 1024


enum {
    SUCCESS = 0,
    INVALID_CONFIG = -1,
    FORK_FAILED = -2,
    PTHREAD_FAILED = -3,
    WAIT_FAILED = -4,
    ROOT_REQUIRED = -5,
    LOAD_SECCOMP_FAILED = -6,
    SETRLIMIT_FAILED = -7,
    DUP2_FAILED = -8,
    SETUID_FAILED = -9,
    EXECVE_FAILED = -10,
    SPJ_ERROR = -11,
    CGROUP_FAILED = -12,
    CHROOT_FAILED = -13,
    MOUNT_FAILED = -14,
    UMOUNT_FAILED = -15,
    CHMOD_EXECUTABLE_FAILED = -16,
    MKDIR_FAILED = -17,
    MKFILE_FAILED = -18, 
    SET_PERMISSION_FAILED = -19
};


struct config {
    int max_cpu_time;
    int max_real_time;
    long long max_memory;
    long long max_stack;
    int max_process_number;
    long long max_output_size;
    int memory_limit_check_only;
    char exe_path[MAX_PATH_LENGTH];
    char input_path[MAX_PATH_LENGTH];
    char output_path[MAX_PATH_LENGTH];
    char error_path[MAX_PATH_LENGTH];
    char *args[ARGS_MAX_NUMBER];
    char *env[ENV_MAX_NUMBER];
    char log_path[MAX_PATH_LENGTH];
    char sandbox_path[MAX_PATH_LENGTH];
    char seccomp_rule_name[MAX_PATH_LENGTH];
    char submission_id[MAX_PATH_LENGTH];
    uid_t uid;
    gid_t gid;
};


enum {
    WRONG_ANSWER = -1,
    CPU_TIME_LIMIT_EXCEEDED = 1,
    REAL_TIME_LIMIT_EXCEEDED = 2,
    MEMORY_LIMIT_EXCEEDED = 3,
    RUNTIME_ERROR = 4,
    SYSTEM_ERROR = 5,
    OUTPUT_LIMIT_EXCEEDED = 6
};


struct result {
    int cpu_time;
    int real_time;
    long long memory;
    int signal;
    int exit_code;
    int error;
    int result;
};

extern const char *mount_list[];
extern const char *mount_dir[];


void run(struct config *_config, struct result *_result);
int recursive_mkdir(char* sandbox_path, const char *dir);
int makefile(char* sandbox_path, const char *dir, const char* file);
int _mount(const char *mount_list[], size_t len, char* sandbox_path);
int _umount(const char *mount_list[], size_t len, char* sandbox_path);
int set_directory_permission(char *input_path);
void init_config(struct config *_config, char const *argv[]);
void print_config(struct config *_config);
#endif //JUDGER_PROFILER_H
