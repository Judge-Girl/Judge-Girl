#include<stdio.h>
#include "intersection.h"

main(){
	int m[100][100];
	int r[4] = { -1, -1, -1, -1 };
	int i, j;
	for(i=0; i<100; i++)
		for(j=0; j<100; j++){
			scanf("%d", &m[i][j]);
		}
	intersection(m, r);
	for(i=0; i<4; i++)
		printf("%d ", r[i] );
	printf("\n");
}
