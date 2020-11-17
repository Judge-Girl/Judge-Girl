#include <stdio.h>
#include <stdlib.h>
#include <inttypes.h>
#include <assert.h>
 
#ifdef __APPLE__
#include <OpenCL/opencl.h>
#else
#include <CL/cl.h>
#endif
 
#define MAX_GPU (0x8)
#define MAX_SOURCE_SIZE (0x100000)
#define DEVICENUM (0x2)
#define MAX_INPUT (20000)
 
int main(void) {
    // Create the two input vectors
    int CHUNKSIZE = 2 << 7;
    long long int MAX_SIZE = (2LL << 29 + 1);
    //fprintf(stderr, "%lld\n", MAX_SIZE);
    long long int MAX_GROUPS = (MAX_SIZE/CHUNKSIZE);
    //fprintf(stderr, "%lld\n", MAX_GROUPS);
    int LOCAL_SIZE = 512;
 
    uint32_t ans[MAX_INPUT];
    int count = 0;
 
    int N[MAX_INPUT];
    uint32_t key1[MAX_INPUT];
    uint32_t key2[MAX_INPUT];
 
    // Load the kernel source code into the array source_str
    FILE *fp;
    char *source_str;
    size_t source_size;
 
    fp = fopen("vecdot.cl", "r");
    if (!fp) {
        fprintf(stderr, "Failed to load kernel.\n");
        exit(1);
    }
    source_str = (char*)malloc(MAX_SOURCE_SIZE);
    source_size = fread( source_str, 1, MAX_SOURCE_SIZE, fp);
    fclose( fp );
 
    // Get platform and device information
    cl_platform_id platform_id = NULL;
    cl_device_id device_id[MAX_GPU];   
    cl_uint ret_num_devices;
    cl_uint ret_num_platforms;
    cl_int ret = clGetPlatformIDs(1, &platform_id, &ret_num_platforms);
    ret = clGetDeviceIDs( platform_id, CL_DEVICE_TYPE_GPU, MAX_GPU, 
            device_id, &ret_num_devices);
    //printf("%d\n", ret_num_devices);
    assert(ret == CL_SUCCESS && ret_num_devices >= DEVICENUM);
 
    int tmp_n;
    uint32_t tmp_key1, tmp_key2;
    while (scanf("%d %" PRIu32 " %" PRIu32, &tmp_n, &tmp_key1, &tmp_key2) == 3) {
        N[count] = tmp_n;
        key1[count] = tmp_key1;
        key2[count++] = tmp_key2;
    }
 
#pragma omp parallel for
    for (int device = 0; device < DEVICENUM; device++) {
        // Create an OpenCL context
        cl_context context = clCreateContext( NULL, 1, device_id+device, NULL, NULL, &ret);
        assert(ret == CL_SUCCESS);
 
        // Create a command queue
        cl_command_queue command_queue = clCreateCommandQueue(context, device_id[device], 0, &ret);
        assert(ret == CL_SUCCESS);
 
        // Create a program from the kernel source
        cl_program program = clCreateProgramWithSource(context, 1, 
            (const char **)&source_str, (const size_t *)&source_size, &ret);
        assert(ret == CL_SUCCESS);
 
        // Build the program
        ret = clBuildProgram(program, 1, device_id+device, NULL, NULL, NULL);
        assert(ret == CL_SUCCESS);
 
        // Create the OpenCL kernel
        cl_kernel kernel = clCreateKernel(program, "dot_sum", &ret);
        assert(ret == CL_SUCCESS);
 
        uint32_t *sum = (uint32_t*)malloc(sizeof(uint32_t)*MAX_GROUPS/LOCAL_SIZE);
        cl_mem sum_mem_obj = clCreateBuffer(context, CL_MEM_WRITE_ONLY, MAX_GROUPS/LOCAL_SIZE*sizeof(uint32_t), NULL, &ret);
       //     fprintf(stderr, "%lld\n", MAX_GROUPS/LOCAL_SIZE/DEVICENUM*sizeof(uint32_t));
         //   fprintf(stderr, "%lld\n", MAX_GROUPS);
     //       sum_mem_obj[device] = clCreateBuffer(context, CL_MEM_WRITE_ONLY, sizeof(uint32_t), NULL, &ret);
        assert(ret == CL_SUCCESS);
 
        for (int i = (count/DEVICENUM+1)*device; i < (count/DEVICENUM+1)*(device+1) && i < count; i++) {
            // Execute the OpenCL kernel on the list
            size_t globalSize = N[i]/CHUNKSIZE+1;
            while (globalSize % LOCAL_SIZE)
                globalSize++;
            size_t globalThreads[] = {(size_t)globalSize}; // Process the entire listsA
            size_t localThreads[] = {(size_t)LOCAL_SIZE}; // Divide work items into groups of LOCAL_SIZE
 
            // Set the arguments of the kernel
            ret = clSetKernelArg(kernel, 0, sizeof(uint32_t), (void *)&key1[i]);
            ret = clSetKernelArg(kernel, 1, sizeof(uint32_t), (void *)&key2[i]);
            ret = clSetKernelArg(kernel, 2, sizeof(cl_mem), (void *)&sum_mem_obj);
            ret = clSetKernelArg(kernel, 3, sizeof(int), (void *)&CHUNKSIZE);
            ret = clSetKernelArg(kernel, 4, sizeof(int), (void *)&N[i]);
            ret = clSetKernelArg(kernel, 5, sizeof(uint32_t)*LOCAL_SIZE, NULL);
            ret = clSetKernelArg(kernel, 6, sizeof(int), (void *)&LOCAL_SIZE);
            assert(ret == CL_SUCCESS);
 
            ret = clEnqueueNDRangeKernel(command_queue, kernel, 1, NULL, 
                globalThreads, localThreads, 0, NULL, NULL);
            //fprintf(stderr, "%d\n", ret);
            assert(ret == CL_SUCCESS);
 
            ret = clEnqueueReadBuffer(command_queue, sum_mem_obj, CL_TRUE, 0, globalSize/LOCAL_SIZE * sizeof(uint32_t), (cl_uint*)sum, 0, NULL, NULL);
            //printf("globalSize: %d\n", int(globalSize/LOCAL_SIZE));
    //        fprintf(stderr, "%d\n", ret);
            assert(ret == CL_SUCCESS);
 
            // Display the result to the screen
            uint32_t tmp = 0;
 
            for(int j = 0; j < globalSize/LOCAL_SIZE; j++) {
   //             printf("%u ", sum[j]);
                tmp += sum[j];
            }
 //           printf("\n");
            ans[i] = tmp;
        }
 
        ret = clFlush(command_queue);
        ret = clFinish(command_queue);
        ret = clReleaseCommandQueue(command_queue);
        ret = clReleaseMemObject(sum_mem_obj);
        ret = clReleaseKernel(kernel);
        ret = clReleaseProgram(program);
        ret = clReleaseContext(context);
        free(sum);
    }
 
    for (int i = 0; i < count; i++) {
        //printf("%" PRIu32 " %" PRIu32 " %" PRIu32 " %" PRIu32 "\n", sum[0], sum[1], sum[2], sum[3]);
        printf("%" PRIu32 "\n", ans[i]);
    }
 
    return 0;
}