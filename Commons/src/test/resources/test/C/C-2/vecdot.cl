static inline uint rotate_left(uint x, uint n) {
    return  (x << n) | (x >> (32-n));
}
static inline uint encrypt(uint m, uint key) {
    return (rotate_left(m, key&31) + key)^key;
}
 
__kernel void dot_sum(const uint key1, const uint key2, __global uint *sum, int chunksize, int N, __local uint localArr[], int localsize) {
    int globalIdx = get_global_id(0);
    int localIdx = get_local_id(0);
    int k = globalIdx*chunksize;
 
    localArr[localIdx] = 0;
    for (int j = k; j < k+chunksize && j < N; j++)
        localArr[localIdx] += encrypt(j, key1) * encrypt(j, key2);
 
    if (localIdx == 0) {
        barrier(CLK_LOCAL_MEM_FENCE);
        uint ans = 0;
        for (int i = 0; i < localsize ; i++) {
            ans += localArr[i];
        }
 
        sum[globalIdx/localsize] = ans;
    }
    //sum[i] = i;
    //printf("%u %u\n", ans, sum[i]);
}