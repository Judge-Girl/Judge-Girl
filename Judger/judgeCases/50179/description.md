## Task Description ##

We want to split segments from an input file into segments and write segments into many output files.

In the input file we have a sequence of arbitrary unsigned characters. We first split this string using delimiter `255`.
Then we write each segment in order to the output file that has the minimum number of characters. If there are more than
one file having the minimum total number of characters, we choose the one with the smallest index.

The following animation shows how you split the sample file.

![](/images/problems/p10270_fin.gif)

## Input Format ##

The first line is the input file name. The second line is the number of output files $N$. The third line is the prefix
of output files. The length of the file names are all between 1 and 50.

* $1 < N \leq 10$

## Output Format ##

The output file names should have the prefix from stdin, the followed by its index (from $1$ to $N$). If the prefix
is `output` and $N$ is 3, then the output file names are `output1`, `output2` and `output3`.

## Sample Input 1 (stdin) ##

[input00](/downloads/testdata/10270/input00)

```
input00
3
output00
```

## Sample output 1 ##

[output001](/downloads/testdata/10270/output001), [output002](/downloads/testdata/10270/output002)
, [output003](/downloads/testdata/10270/output003)

## Sample Input 2 (stdin) ##

[input01](/downloads/testdata/10270/input01)

```
input01
2
output01
```

## Sample output 2 ##

[output011](/downloads/testdata/10270/output011), [output012](/downloads/testdata/10270/output012)

## Sample Input 3 (stdin) ##

[input02](/downloads/testdata/10270/input02)

```
input02
4
output02
```

## Sample output 3 ##

[output021](/downloads/testdata/10270/output021), [output022](/downloads/testdata/10270/output022)
, [output023](/downloads/testdata/10270/output023), [output024](/downloads/testdata/10270/output024)