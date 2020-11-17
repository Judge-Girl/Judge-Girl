#define _GNU_SOURCE
#define _POSIX_SOURCE

#include <libgen.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sched.h>
#include <signal.h>
#include <pthread.h>
#include <errno.h>
#include <unistd.h>

#include <sys/wait.h>
#include <sys/time.h>
#include <sys/resource.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <dirent.h>

#include "profiler.h"
#include "killer.h"
#include "sandbox.h"
#include "logger.h"

//mount list
const char *mount_list[] = {"bin", "dev/full", "dev/null", "dev/zero", "dev/urandom", "lib", "lib64", \
                            "usr", "proc", "sys/fs/cgroup/memory", "etc/OpenCL"};
const char *mount_dir[] = {"bin", "dev", "lib", "lib64", "usr", "proc", "sys/fs/cgroup/memory", "etc/OpenCL"};

void init_config(struct config *_config, char const *argv[]) {
    sscanf(argv[1], "%d", &(_config->max_cpu_time));
    sscanf(argv[2], "%d", &(_config->max_real_time));
    sscanf(argv[3], "%lld", &(_config->max_memory));
    sscanf(argv[4], "%lld", &(_config->max_stack));
    sscanf(argv[5], "%d", &(_config->max_process_number));
    sscanf(argv[6], "%lld", &(_config->max_output_size));
    sscanf(argv[7], "%d", &(_config->memory_limit_check_only));
    strncpy(_config->exe_path, argv[8], sizeof(_config->exe_path));
    strncpy(_config->input_path, argv[9], sizeof(_config->exe_path));
    strncpy(_config->output_path, argv[10], sizeof(_config->exe_path));
    strncpy(_config->error_path, argv[11], sizeof(_config->exe_path));
    strncpy(_config->log_path, argv[12], sizeof(_config->exe_path));
    strncpy(_config->sandbox_path, argv[13], sizeof(_config->exe_path));
    strncpy(_config->seccomp_rule_name, argv[14], sizeof(_config->exe_path));
    strncpy(_config->submission_id, argv[15], sizeof(_config->exe_path));
    sscanf(argv[16], "%u", &(_config->uid));
    sscanf(argv[17], "%u", &(_config->gid));
    /*
    _config->args[0] = "cgexec";
    _config->args[1] = "-g";
    _config->args[2] = "memory:sandbox"; //if we want to judge simultaneously，group "sandbox" should be submissionId
    _config->args[3] = _config->exe_path;
    _config->args[4] = NULL;
    */
    _config->args[0] = _config->exe_path;
    _config->args[1] = NULL;    

    _config->env[0] = NULL;
}

void print_config(struct config *_config) {
    printf("_config->max_cpu_time: %d\n", (_config->max_cpu_time));
    printf("_config->max_real_time: %d\n", (_config->max_real_time));
    printf("_config->max_memory: %lld\n", (_config->max_memory));
    printf("_config->max_stack: %lld\n", (_config->max_stack));
    printf("_config->max_process_number: %d\n", (_config->max_process_number));
    printf("_config->max_output_size: %lld\n", (_config->max_output_size));
    printf("_config->memory_limit_check_only: %d\n", (_config->memory_limit_check_only));
    printf("_config->exe_path: %s\n", _config->exe_path);
    printf("_config->input_path: %s\n", _config->input_path);
    printf("_config->output_path: %s\n", _config->output_path);
    printf("_config->error_path: %s\n", _config->error_path);
    printf("_config->log_path: %s\n", _config->log_path);
    printf("_config->sandbox_path: %s\n", _config->sandbox_path);
    printf("_config->seccomp_rule_name: %s\n", _config->seccomp_rule_name);
    printf("_config->submission_id: %s\n", _config->submission_id);
    printf("_config->uid: %u\n", _config->uid);
    printf("_config->gid: %u\n", _config->gid);   
}

void init_result(struct result *_result) {
    _result->result = _result->error = SUCCESS;
    _result->cpu_time = _result->real_time = _result->signal = _result->exit_code = 0;
    _result->memory = 0;
}

//create directory recursively if it doesn't exist
int recursive_mkdir(char *sandbox_path, const char *dir) {
    char tmp[2*MAX_PATH_LENGTH];
    char *p = NULL;
    size_t len;
    struct stat st = {0};

    snprintf(tmp, sizeof(tmp), "%s/%s", sandbox_path, dir);

    len = strlen(tmp);
    if(tmp[len-1] == '/')
        tmp[len-1] = 0;
    for(p = tmp+1; *p; p++) {
        if(*p == '/') {
            *p = 0;
            if (stat(tmp, &st) == -1) {
                if (mkdir(tmp, 0755) != 0) {
                    return -1;
                }
            }
            *p = '/';
        }
    }
    if (stat(tmp, &st) == -1) {
        if (mkdir(tmp, 0755) != 0) {
            return -1;
        }
    }

    return 0;
}

int makefile(char *sandbox_path, const char *dir, const char *file) {
    char cmd[MAX_PATH_LENGTH] = "";
    snprintf(cmd, sizeof(cmd), "touch %s/%s/%s", sandbox_path, dir, file);
    if (system(cmd) != 0){
        return -1;
    }

    return 0;
}

int set_directory_permission(char *sandbox_path) {
    //since the function dirname will modify sandbox_path to it's parent path, we need to clone it first
    char clone_sandbox_path[MAX_PATH_LENGTH] = "";
    char expected_output_path[MAX_PATH_LENGTH] = "";

    strncpy(clone_sandbox_path, sandbox_path, sizeof(clone_sandbox_path));
    char *parent_dir_path = dirname(clone_sandbox_path);
    snprintf(expected_output_path, sizeof(expected_output_path), "%s/out", parent_dir_path);
    if (chmod(expected_output_path, 0700) != 0) {
        return -1;
    }
    return 0;
}

int _mount(const char *mount_list[], size_t len, char *sandbox_path) {
    char cmd[MAX_PATH_LENGTH] = "";
    for (int i = 0; i < len; i++) {
        snprintf(cmd, sizeof(cmd), "mount -o bind /%s %s/%s", mount_list[i], sandbox_path, mount_list[i]);
        if (system(cmd) != 0) {
            //rollback
            _umount(mount_list, i, sandbox_path);
            return -1;
        }
    }

    return 0;
}

int _umount(const char *mount_list[], size_t len, char *sandbox_path) {
    char cmd[MAX_PATH_LENGTH] = "";
    for (int i = 0; i < len; i++) {
        //lazy umount
        snprintf(cmd, sizeof(cmd), "umount -l %s/%s", sandbox_path, mount_list[i]);
        if (system(cmd) != 0) {
            //kill the process using the mount directory
            snprintf(cmd, sizeof(cmd), "fuser -k %s/%s", sandbox_path, mount_list[i]);
            system(cmd);
            //force umount
            snprintf(cmd, sizeof(cmd), "umount -f %s/%s", sandbox_path, mount_list[i]);
            if (system(cmd) != 0) {
                return -1;
            }
        }
    }

    return 0;
}

void run(struct config *_config, struct result *_result) {
    // init log fp
    FILE *log_fp = log_open(_config->log_path);
    char *submission_id = _config->submission_id;

    size_t mount_list_len = sizeof(mount_list) / sizeof(mount_list[0]);
    size_t mount_dir_len = sizeof(mount_dir) / sizeof(mount_dir[0]);

    // init result
    init_result(_result);

    // check whether current user is root
    uid_t uid = getuid();
    if (uid != 0) {
        ERROR_EXIT(ROOT_REQUIRED);
    }

    // check args
    if ((_config->max_cpu_time < 1 && _config->max_cpu_time != UNLIMITED) ||
        (_config->max_real_time < 1 && _config->max_real_time != UNLIMITED) ||
        (_config->max_stack < 1 && _config->max_memory != UNLIMITED) ||
        (_config->max_memory < 1 && _config->max_memory != UNLIMITED) ||
        (_config->max_process_number < 1 && _config->max_process_number != UNLIMITED) ||
        (_config->max_output_size < 1 && _config->max_output_size != UNLIMITED)) {
        ERROR_EXIT(INVALID_CONFIG);
    }

    // avoid child process change it's working directory
    if (set_directory_permission(_config->sandbox_path) != 0) {
        ERROR_EXIT(SET_PERMISSION_FAILED);
    }

    // record current time
    struct timeval start, end;
    gettimeofday(&start, NULL);

    pid_t child_pid = fork();

    // pid < 0 shows clone failed
    if (child_pid < 0) {
        ERROR_EXIT(FORK_FAILED);
    }
    else if (child_pid == 0) {
        child_process(log_fp, _config, mount_list_len);
    }
    else if (child_pid > 0) {
        // create new thread to monitor process running time
        pthread_t tid = 0;
        if (_config->max_real_time != UNLIMITED) {
            struct timeout_killer_args killer_args;

            killer_args.timeout = _config->max_real_time;
            killer_args.pid = child_pid;
            if (pthread_create(&tid, NULL, timeout_killer, (void *) (&killer_args)) != 0) {
                kill_pid(child_pid);
                ERROR_EXIT(PTHREAD_FAILED);
            }
        }

        int status;
        struct rusage resource_usage;

        // wait for child process to terminate
        // on success, returns the process ID of the child whose state has changed;
        // On error, -1 is returned.
        if (wait4(child_pid, &status, WSTOPPED, &resource_usage) == -1) {
            kill_pid(child_pid);
            ERROR_EXIT(WAIT_FAILED);
        }
        // get end time
        gettimeofday(&end, NULL);
        _result->real_time = (int) (end.tv_sec * 1000 + end.tv_usec / 1000 - start.tv_sec * 1000 - start.tv_usec / 1000);

        // process exited, we may need to cancel timeout killer thread
        if (_config->max_real_time != UNLIMITED) {
            if (pthread_cancel(tid) != 0) {
                // todo logging
            };
        }

        if (WIFSIGNALED(status) != 0) {
            _result->signal = WTERMSIG(status);
        }

        if(_result->signal == SIGUSR1) {
            _result->result = SYSTEM_ERROR;
        }
        else {
            _result->exit_code = WEXITSTATUS(status);
            _result->cpu_time = (int) (resource_usage.ru_utime.tv_sec * 1000 +
                                       resource_usage.ru_utime.tv_usec / 1000);
            _result->memory = resource_usage.ru_maxrss * 1024;

            if (_result->exit_code != 0) {
                _result->result = RUNTIME_ERROR;
            }

            if (_result->signal == SIGSEGV) {
                if (_config->max_memory != UNLIMITED && _result->memory > _config->max_memory) {
                    _result->result = MEMORY_LIMIT_EXCEEDED;
                }
                else {
                    _result->result = RUNTIME_ERROR;
                }
            } else if (_result->signal == SIGXFSZ) {
                _result->result = OUTPUT_LIMIT_EXCEEDED;
            } else {
                if (_result->signal != 0) {
                    _result->result = RUNTIME_ERROR;
                }
                if (_config->max_memory != UNLIMITED && _result->memory > _config->max_memory) {
                    _result->result = MEMORY_LIMIT_EXCEEDED;
                }
                if (_config->max_real_time != UNLIMITED && _result->real_time > _config->max_real_time) {
                    _result->result = REAL_TIME_LIMIT_EXCEEDED;
                }
                if (_config->max_cpu_time != UNLIMITED && _result->cpu_time > _config->max_cpu_time) {
                    _result->result = CPU_TIME_LIMIT_EXCEEDED;
                }
            }
        }

        log_close(log_fp);
    }
}
