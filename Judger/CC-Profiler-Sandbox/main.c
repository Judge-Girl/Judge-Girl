#include "profiler.h"
#include <assert.h>
#include <stdlib.h>

int main(int argc, char const *argv[])
{
    struct result *_result = (struct result *)malloc(sizeof(struct result));
    struct config *_config = (struct config *)malloc(sizeof(struct config));

    //program + number of entries in struct config
    assert(argc == 18);

    //init config
    init_config(_config, argv);
    //print_config(_config);

    //run
    run(_config, _result);

    //concatenate result
    /*
    printf("_result->cpu_time: %d\n", _result->cpu_time);
    printf("_result->real_time: %d\n", _result->real_time);
    printf("_result->memory: %lld\n", _result->memory);
    printf("_result->signal: %d\n", _result->signal);
    printf("_result->exit_code: %d\n", _result->exit_code);
    printf("_result->error: %d\n", _result->error);
    printf("_result->result: %d\n", _result->result);
    */

    printf("%d,%d,%lld,%d,%d,%d,%d", 
    				_result->cpu_time, _result->real_time, _result->memory, _result->signal, _result->exit_code, _result->error, _result->result);

    free(_result);
    free(_config);
    return 0;
}