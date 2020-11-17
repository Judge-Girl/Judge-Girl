#define CL_USE_DEPRECATED_OPENCL_2_0_APIS
 
#include <stdio.h>
#include <assert.h>
#include <CL/cl.h>
 
#define MAXGPU 10
#define MAXK 1024
#define N (1024 * 1024)
/* main */
int main(int argc, char *argv[])
{
  // printf("Hello, OpenCL\n");
  cl_int status;
  cl_platform_id platform_id;
  cl_uint platform_id_got;
  status = clGetPlatformIDs(1, &platform_id,
                            &platform_id_got);
  assert(status == CL_SUCCESS && platform_id_got == 1);
  //printf("%d platform found\n", platform_id_got);
  cl_device_id GPU[MAXGPU];
  cl_uint GPU_id_got;
  status = clGetDeviceIDs(platform_id, CL_DEVICE_TYPE_GPU,
                          MAXGPU, GPU, &GPU_id_got);
  assert(status == CL_SUCCESS);
  //printf("There are %d GPU devices\n", GPU_id_got);
  /* getcontext */
  cl_context context = clCreateContext(NULL, 1, GPU, NULL, NULL, &status);
  assert(status == CL_SUCCESS);
  /* commandqueue */
  cl_command_queue commandQueue = clCreateCommandQueue(context, GPU[0], 0, &status);
  assert(status == CL_SUCCESS);
  /* kernelsource */
  char filename[30] = {0};
  scanf("%s", filename);
  FILE *kernelfp = fopen(filename, "r");
  assert(kernelfp != NULL);
  char kernelBuffer[MAXK];
  const char *constKernelSource = kernelBuffer;
  size_t kernelLength = fread(kernelBuffer, 1, MAXK, kernelfp);
  //printf("The size of kernel source is %zu\n", kernelLength);
  cl_program program = clCreateProgramWithSource(context, 1, &constKernelSource, &kernelLength, &status);
  assert(status == CL_SUCCESS);
  /* buildprogram */
  status = clBuildProgram(program, 1, GPU, NULL, NULL, NULL);
  size_t length;
  size_t size = 0;
  if(status != CL_SUCCESS){
    clGetProgramBuildInfo(program, GPU[0], CL_PROGRAM_BUILD_LOG, sizeof(char*), NULL, &length);
    char* debug_message = calloc(length + 1, sizeof(char));
    clGetProgramBuildInfo(program, GPU[0], CL_PROGRAM_BUILD_LOG, length, debug_message, &length);
    debug_message[length] = '\0';
    printf("%s", debug_message);
    // printf("Build program incompletes\n");
  }
 
  return 0;
}
/* end */
