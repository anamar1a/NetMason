options nomlogic nonotes  compress=yes;
/* filename junk dummy;
proc printto  log=junk; run; */


DATA NetMason.runs; 
  SET NetMason.runs; 
  beta=round(beta,0.01);
  delta=round(delta,0.01);
RUN; 

PROC SQL;
	CREATE TABLE NetMason.profiles AS
	SELECT COUNT(*) AS count, beta, delta  FROM NetMason.runs 
	GROUP BY beta, delta;
QUIT;

DATA NetMason.profiles; 
		  SET NetMason.profiles; 
		  profile = _n_;
RUN; 

PROC SQL;
	CREATE TABLE NetMason.characters AS
	SELECT runID, prefix, AVG(level) as level, AVG(planningHorizon) as horizon, strategistID FROM NetMason.strategists 
	GROUP BY runID, prefix, strategistID;
QUIT;

PROC SQL;
	CREATE TABLE NetMason.runs AS
	SELECT A.*, B.level as levelZero, B.horizon as horizonZero FROM NetMason.runs A,  NetMason.characters B
	WHERE B.strategistID EQ 0 AND A.runID EQ B.runID and A.prefix EQ B.prefix;

	CREATE TABLE NetMason.runs AS
	SELECT A.*, B.level as levelOne, B.horizon as horizonOne FROM NetMason.runs A,  NetMason.characters B
	WHERE B.strategistID EQ 1 AND A.runID EQ B.runID and A.prefix EQ B.prefix;
QUIT;

DATA NetMason.runs; 
		  SET NetMason.runs; 
		  id = _n_;
RUN; 



/* CREATE FITNESS DATA */

DATA NetMason.fitness (drop=prefix runID); 
  SET NetMason.fitness; 
  tick=round(tick,1);
  p1=round(p1,0.05);
  p2=round(p2,0.05);
  p3=round(p3,0.05);
  if (predictedProfit=.) then delete;
  if (p1=.) then delete;
  if (p2=.) then delete;
  if (p3=.) then delete;
  if (p1>1) then delete;
  if (p2>1) then delete;
  if (p3>1) then delete;
  if (tick=.) then delete;
RUN; 

PROC EXPORT DATA= NetMason.fitness
         OUTFILE= "C:\MASTools\workspace\NetMason\outputs\temp\fitness.txt" 
         DBMS=TAB REPLACE;
RUN;

proc sort data=NetMason.Strategists 
     out=NetMason.Strategists;
by prefix runID tick;
run;

DATA NetMason.incomeRate (drop=VAR13); 
  SET NetMason.Strategists; 
  by prefix runID;
  tick=round(tick,1);
  previousTick=lag(tick);
RUN; 

PROC SQL;
 CREATE TABLE Netmason.incomeRate AS
 SELECT A.*, B.* from Netmason.incomeRate A, Netmason.runs B WHERE A.runID EQ B.runID and A.prefix EQ B.prefix;
QUIT;


PROC SQL;
 CREATE TABLE Netmason.incomeRate AS
 SELECT * from Netmason.incomeRate A, Netmason.runs B WHERE A.runID EQ B.runID and A.prefix EQ B.prefix;
QUIT;


DATA NetMason.incomeRate (drop=prefix runID actionExecuted); 
  SET NetMason.incomeRate; 
  executionTime=tick-previousTick;
  if (actionExecuted='BT1') then task=1;
  if (actionExecuted='BT2') then task=2;
  if (actionExecuted='BT3') then task=3;
  if (actionExecuted='RT1') then task=1;
  if (actionExecuted='RT2') then task=2;
  if (actionExecuted='RT3') then task=3;
  if (predictedProfit=.) then delete;
  if (p1=.) then delete;
  if (p2=.) then delete;
  if (p3=.) then delete;
  if (p1>1) then delete;
  if (p2>1) then delete;
  if (p3>1) then delete;
  if (tick=.) then delete;
  if (task=.) then delete;
  if (previousTick=.) then delete;
  if (executionTime<0) then delete;
RUN; 

PROC EXPORT DATA= NetMason.incomeRate
         OUTFILE= "C:\MASTools\workspace\NetMason\outputs\temp\incomeRate.txt" 
         DBMS=TAB REPLACE;
RUN;

PROC SQL;
 CREATE TABLE NetMason.sampleRun AS
 SELECT * FROM NetMason.incomeRate;
QUIT;

PROC EXPORT DATA=  NetMason.sampleRun
         OUTFILE= "C:\MASTools\workspace\NetMason\outputs\temp\sampleRun.txt" 
         DBMS=TAB REPLACE;
RUN;

DATA NetMason.fitness; 
  SET NetMason.fitness; 
  tick=round(tick,100);
RUN; 


PROC SQL;
 CREATE TABLE NetMason.fitnessComp AS
 SELECT tick, p1, p2 ,p3, AVG(predictedProfit) as predictedProfit FROM NetMason.fitness GROUP BY tick,p1,p2,p3 ;
QUIT;

PROC EXPORT DATA= NetMason.fitnessComp
         OUTFILE= "C:\MASTools\workspace\NetMason\products\temp\fitnessComp.txt" 
         DBMS=TAB REPLACE;
RUN;

/* Compare performance of different recursion levels again each other */

PROC SQL;
	CREATE TABLE NetMason.Zeros AS
	SELECT * FROM NetMason.strategists WHERE strategistID EQ 0;
	
	CREATE TABLE NetMason.Ones AS
	SELECT * FROM NetMason.strategists WHERE strategistID EQ 1;
QUIT;

PROC SQL;
	CREATE TABLE NetMason.Zeros AS
	SELECT runID, prefix, AVG(level) as level, AVG(planningHorizon) as planningHorizon, SUM(actualProfit) as actualProfit FROM NetMason.Zeros GROUP BY runID, prefix;
	
	CREATE TABLE NetMason.Ones AS
	SELECT runID, prefix, AVG(level) as level, AVG(planningHorizon) as planningHorizon, SUM(actualProfit) as actualProfit FROM NetMason.Ones GROUP BY runID, prefix;
QUIT;

DATA  NetMason.Ones (rename=(actualProfit=actualProfitOne level=rationalityLevelOne planningHorizon=planningHorizonOne)); 
  SET  NetMason.Ones; 
RUN; 

DATA  NetMason.Zeros (rename=(actualProfit=actualProfitZero level=rationalityLevelZero planningHorizon=planningHorizonZero)); 
  SET  NetMason.Zeros; 
RUN;

PROC SQL;
	CREATE TABLE NetMason.Combined AS
	SELECT A.*, B.* FROM NetMason.Zeros A, NetMason.Ones B WHERE A.prefix EQ B.prefix AND A.runID EQ B.runID;

	CREATE TABLE NetMason.Combined AS 
	SELECT A.*, B.delta, B.beta FROM NetMason.Combined A, NetMason.runs B WHERE A.runID EQ B.runID AND A.prefix EQ B.prefix;
QUIT;

PROC EXPORT DATA= NetMason.Combined
         OUTFILE= "C:\MASTools\workspace\NetMason\outputs\temp\combinedPerformance.txt" 
         DBMS=TAB REPLACE;
RUN;
