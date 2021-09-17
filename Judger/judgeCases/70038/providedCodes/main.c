#include <stdio.h>

int main() {
  int n;
  for (long i = 0; i < 5000000000L; i ++) {}
  scanf("%d", &n);
  printf("%d", n);
  return 0;
}