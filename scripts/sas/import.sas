options nomlogic nonotes compress=yes;
filename junk dummy;
proc printto  log=junk; run;

/* Read data from multiple runs / nodes */

%macro ReadTxt(dir =, table = );

	DATA _null_;
		/* MANIPULATION THE STRING OF FOLDER PATH */
		dir1 = '"'||symget('dir')||'\"';
		dir21 = "'dir "||tranwrd(dir1, '\"', '\*.txt"');
		dir22 = " /b'";
		dir2 = compbl(dir21||dir22);
		/* PASS THE STRINGS TO MACRO VARIABLES */
		call symput('dir1', dir1);
		call symput('dir2', dir2);
	RUN;

	filename txtPipe pipe &dir2;

	DATA Netmason.Files&table;
		infile txtPipe truncover end = last;
		input Name $32.;
		Path = compbl(&dir1||Name);
		i + 1;
		index = compress('f'||put(i, 7.), ' ');
		/* GET THE NUMBER OF FILES */
		if last then call symput('last', put(i, 7.));
		/* PASS THE FILES PATH TO MACRO VARIABLES */
		call symput(index, Path);
		drop i;
	run;

	%do i = 1 %to &last;
	
	/* READ DATA FROM EXCEL FILES */
	
	PROC IMPORT out = Work.temp
	    	        datafile = "&&f&i"
	        	    DBMS=DLM REPLACE;
			    	DELIMITER='3B'x; 
			    	GETNAMES=YES;
			     	DATAROW=2; 
	RUN;
	
	proc append base=netmason.&table  data=Work.temp;
 	run;

	

	%end;

%mend ReadTxt;

%ReadTxt(dir = C:\MAStools\workspace\NetMason\outputs\sparrow\gathered\fitness, table = fitness);
%ReadTxt(dir = C:\MAStools\workspace\NetMason\outputs\sparrow\gathered\runs, table = runs);
%ReadTxt(dir = C:\MAStools\workspace\NetMason\outputs\sparrow\gathered\strategists, table = strategists);
%ReadTxt(dir = C:\MAStools\workspace\NetMason\outputs\sparrow\gathered\evaluations, table = evaluations );

/* Read data from a single node */

%macro ReadData(prefix = );

/* MANIPULATION THE STRING OF FOLDER PATH */
	
		DATA _null_;
			prefix = symget('prefix');

			strategistsF = compress('"'||"C:\MAStools\workspace\NetMason\"||prefix||"_strategists.txt"||'"');
			runF = compress('"'||"C:\MAStools\workspace\NetMason\"||prefix||"_run.txt"||'"');
			fitnessF = compress('"'||"C:\MAStools\workspace\NetMason\"||prefix||"_fitness.txt"||'"');
			evaluationsF = compress('"'||"C:\MAStools\workspace\NetMason\"||prefix||"_evaluations.txt"||'"');

			call symput('strategistsF', strategistsF);
			call symput('runsF', runsF);
			call symput('fitnessF', fitnessF);
		    call symput('evaluationsF', evaluationsF);
		RUN;


		PROC IMPORT OUT= NetMason.strategists 
		            DATAFILE=&strategistsF 
		            DBMS=DLM REPLACE;
		     DELIMITER='3B'x; 
		     GETNAMES=YES;
		     DATAROW=2; 
		RUN;

		PROC IMPORT OUT= NetMason.RUNS
		            DATAFILE=&runsF 
		            DBMS=DLM REPLACE;
		     DELIMITER='3B'x; 
		     GETNAMES=YES;
		     DATAROW=2; 
		RUN;
		
		PROC IMPORT OUT= Netmason.fitness
		            DATAFILE= &fitnessF
		            DBMS=DLM REPLACE;
		     DELIMITER='3B'x; 
		     GETNAMES=YES;
		     DATAROW=2; 
		RUN;


		PROC IMPORT OUT= Netmason.evaluations
		            DATAFILE= &evaluationsF
		            DBMS=DLM REPLACE;
		     DELIMITER='3B'x; 
		     GETNAMES=YES;
		     DATAROW=2; 
		RUN;

%mend ReadData;

%ReadData(prefix = null);
