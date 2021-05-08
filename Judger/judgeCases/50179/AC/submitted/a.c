#include<stdio.h>
#include<assert.h>

int main(){
    char in[100],out[100]; int n;
    scanf("%s",in);
    scanf("%d",&n);
    scanf("%s",out);
    //FILE *tmpf=fopen("out/std.out","w");
    //fclose(tmpf);
    //printf("hello.\n");
    FILE *fin=fopen(in,"r");
    assert(fin);
    FILE *fout[15];
    for(int i=1;i<=n;i++){
        char tmp[100];
        sprintf(tmp,"%s%d",out,i);
        fout[i-1]=fopen(tmp,"w");
    }
    int cnt[15]={};
    int i=0,c,mn;
    while(1){
        while((c=fgetc(fin))==255&&c!=EOF);
        if(c==EOF) break;
        for(i=0,mn=1e9;i<n;i++) mn=(mn<cnt[i]?mn:cnt[i]);
        for(i=0;i<n&&cnt[i]!=mn;i++);
        do{
            fputc(c,fout[i]);
            cnt[i]++;
        }while((c=fgetc(fin))!=255&&c!=EOF);
        if(c==EOF) break;
    }
    for(int i=0;i<n;i++) fclose(fout[i]);
}
