#include <stdio.h>
#include <stdlib.h>
int pos[2][50000][2];
int main(void) {
    int n;
    scanf("%d", &n);
    for (int i=0; i<n; i++) {
        int x1, x2, y1, y2;
        scanf("%d%d", &x1, &y1);
        scanf("%d%d", &x2, &y2);
        pos[0][i][0]=x1, pos[0][i][1]=y1;
        pos[1][i][0]=x2, pos[1][i][1]=y2;
        if (!i) printf("%d\n", abs(x1-x2)+abs(y1-y2));
        else {
            int d=abs(x1-pos[1][0][0])+abs(y1-pos[1][0][1]);
            for (int j=0; j<i; j++) {
                if (d>abs(x1-pos[1][j][0])+abs(y1-pos[1][j][1]))
                    d=abs(x1-pos[1][j][0])+abs(y1-pos[1][j][1]);
            }
            printf("%d\n", d);
            d=abs(x2-pos[0][0][0])+abs(y2-pos[0][0][1]);
            for (int j=0; j<=i; j++) {
                if (d>abs(x2-pos[0][j][0])+abs(y2-pos[0][j][1]))
                    d=abs(x2-pos[0][j][0])+abs(y2-pos[0][j][1]);
            }
            printf("%d\n", d);
        }
    }
}