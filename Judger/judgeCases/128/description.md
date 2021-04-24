### Task Description
Write a function to determine the number of intersections in a city. You will be given a city map as a $100$ by $100$ integer array. When the element in the array is 1, it means there is a section of road in that place. When the element in the array is not 1, then it is not a section of road. Now we define all kinds of intersections.

* A cell is an intersection if it is 1, and all of its four neighbors are also 1.
* A cell is a T-junction, if it is 1, and exactly three of its neighbors are also 1.
* A cell is a turn if it is 1 and exactly two of its neighbors are 1, and these two neighbors do not form a straight line with the cell.
* A cell is a dead end if it is 1, and exactly one of its neighbors is 1.

The prototype of the function is as follows.
```
void intersection(int map[100][100], int result[4]);
```
The map parameter is the city map and the result is where you should place the results - the number of intersection should be placed in result[0], and T-junction should be placed in result[1], etc.

## Reference ##

Problem 33. Intersections 