Reinforcement Learning of Risk-Averse Policies in Partially Observable Markov Decision Processes

The paper implementation itself is in ".../MasterThesis/PaperSandbox/src/main/java/vahy/paperGenerics/"

The Java-CLP libraries are provided and the whole program was tested on Windows OS and Linux OS.

How to start:
if any problems encountered, please, contact me on snurkabill@gmail.com

1. Install JRE 13+ and latest Maven build tool on your system
2. Clone this repository
3. In root directory (.../MasterThesis/), execute command `mvn clean install -Dcheckstyle.skip -DskipTests -U`. It will build the whole project.
4. Prepared HallwayGame examples can be found in `.../MasterThesis/HallwayGame/src/main/java/vahy/solutionExamples/` path
5. To execute some example, in directory `../MasterThesis/HallwayGame/` execute command `mvn exec:java@<benchmarkId>`
    - example: `mvn exec:java@05`
    - Note: not all benchmakrks have aliases in maven exec plugin. 
    - the algorithm parameters and hyper-parameters are not necessarily optimal
6. The training will start. For some benchmarks, it can take time.



