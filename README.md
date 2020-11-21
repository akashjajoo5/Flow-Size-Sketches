# Flow Size Sketches
### Implementation
1. Implemented Count Min, Counter Sketch and Active Counter in Java.
2. Kindly refer _project3.pdf_ for project requirements and to understand the input format for the file.

### How to Execute the code
1. Download FlowSize.java and compile the file using `javac FlowSize.java`.
2. A class file will be generated named **FlowSize.class**, execute the file using `java FlowSize`.
3. Enter the desired input for all three Flow Size Sketches and the output file of each flow sketch will be generated in the same folder.

### Output format
1. **countMin_output.txt** is the output for **Count Min**. It has many lines of output; the first line shows the average error among all flows. From the 4th line, the next 100 lines shows the Flow ID, Estimated Size and True Size of the Top 100 largest Estimated Sizes.
2. **counterSketch_output.txt** is the output for **Counter Sketch**. It has many lines of output; the first line shows the average error among all flows. From the 4th line, the next 100 lines shows the Flow ID, Estimated Size and True Size of the Top 100 largest Estimated Sizes
3. **activeCounter_output.txt** is the output for **Active Counter**. It has one line of output which shows the final value of Active Counter in decimal.
