#define CL_USE_DEPRECATED_OPENCL_2_0_APIS
 
#include <stdio.h>
#include <assert.h>
#include <CL/cl.h>
 
#define MAXGPU 10
#define MAXK 1024
#define N (1024 * 1024)
int f() {
    return f();
}
/* main */
int main(int argc, char *argv[])
{ 
    f();
  return 0;
}
/* end */
