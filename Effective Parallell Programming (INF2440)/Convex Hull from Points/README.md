# Calculate Convex Hull from points
This project was the fifth mandatory assignment in INF2440 (Spring 2018).
I have created both a parallell and sequential soloution to the problem.
My program runs both soloutions 7 times and compares the result.
My parallell soloution is quicker when the point amount is larger than 100 000 (At least on my Ryzen 2700x System)

How to compile & run:

javac *.java

java oblig5 <amount of points to generate> <number of cores to use (0 for all)>

Add another argument to make my program print the convex hull of 100 points.
