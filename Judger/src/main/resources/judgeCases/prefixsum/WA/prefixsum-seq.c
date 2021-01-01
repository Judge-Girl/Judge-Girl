#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <inttypes.h>
#include "utils.h"

#define MAXN 10000005
uint32_t prefix_sum[MAXN];
int main() {
    int n;
    uint32_t key;
    while (scanf("%d %" PRIu32, &n, &key) == 2) {
        for (int i = 1; i <= n; i++) {
            prefix_sum[i] = 1; // wrong answer
        }
        output(prefix_sum, n);
    }
    return 0;
}