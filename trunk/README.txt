Building and Executing the Adapter and Codec Code

TOC
1. Necessary JARs
2. Build the source code
3. Run the Adapter and Codec 
4. Generate Javadoc

1. Necessary JARs
The source code is in: /otGEOServerJava/src
The tools used are the OpenTTCN SDK and Tester, HttpUnit 1.7 and dom4j 1.6.1. The files necessary for building and running the code are described below. Some of the tools use the same XML APIs; to avoid conflicts use only the jar files described below.
OpenTTCN3.jar   This must be in the OpenTTCN Tester libraries directory eg: C:\OpenTTCN\Tester3\lib
OTSDK.jar   This must be in the OpenTTCN Java SDK libraries directory eg: C:\OpenTTCN\JavaSDK\lib
OTSDK.dll   This must be in the OpenTTCN Java SDK libraries directory eg: C:\OpenTTCN\JavaSDK\lib 
--The following files must be placed in /otGEOServerJava/lib:
httpunit.jar 
dom4j-1.6.1.jar 
xercesImpl-2.6.1.jar 
xmlParserAPIs-2.6.1.jar 
activation-1.1.jar 
js-1.6R5.jar 
jtidy-4aug2000r7-dev.jar 
junit-3.8.1.jar 
mail-1.4.jar 
servlet-api-2.4.jar 
jaxen-1.1.1.jar

2. Build the source code
To build the Java source an Ant build file is used. The build file must be placed in the root directory of the Java Adapter and Codec project. The Ant task for building the code must be like this:
<javac srcdir="${src}" destdir="${classes}">
		<classpath>
			<pathelement location="${otSDKlib}/OTSDK.jar"/> 
			<pathelement location="lib/httpunit.jar"/>            	
			<pathelement location="lib/dom4j-1.6.1.jar"/>            	
		</classpath>
</javac>

3. Executing the Adapter and Codec code:
The qualipso.openttcn.gistest.Main class must be started is a separate command prompt window. 
The classpath in the command to execute the qualipso.openttcn.gistest.Main must include all the jar files mentioned above:
SET OT_LIB=C:\OpenTTCN\Tester3\lib
SET OT_SDK_LIB=C:\OpenTTCN\JavaSDK\lib
SET TESTS_LIB=C:\workspace\otGEOServerJava\lib
SET CLASS_DIR=C:\workspace\otGEOServerJava\classes
java -classpath %OT_LIB%\OpenTTCN3.jar;%OT_SDK_LIB%\OTSDK.jar;%TESTS_LIB%\httpunit.jar;%TESTS_LIB%\dom4j-1.6.1.jar;%TESTS_LIB%\xercesImpl-2.6.1.jar;%TESTS_LIB%\xmlParserAPIs-2.6.1.jar;%TESTS_LIB%\activation-1.1.jar;%TESTS_LIB%\js-1.6R5.jar;%TESTS_LIB%\jtidy-4aug2000r7-dev.jar;%TESTS_LIB%\junit-3.8.1.jar;%TESTS_LIB%\mail-1.4.jar;%TESTS_LIB%\servlet-api-2.4.jar;%TESTS_LIB%\jaxen-1.1.1.jar;%CLASS_DIR% -Djava.library.path=%OT_SDK_LIB% qualipso.openttcn.gistest.Main

4. Generating the Javadoc:
The following task can be added in the Ant build script:
<javadoc packagenames="qualipso.openttcn.gistest.*" 
					sourcepath="src"
					destdir="javadoc"/>


