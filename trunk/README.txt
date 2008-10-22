TOC
1. Prerequisites
2. Checkout from the repository
3. Necessary aditional libraries and configuration
4. Starting the Adapter
5. Configure the build.xml
6. Executing the tests

1. Prerequisites
a. Windows XP or "better"
b. Eclipse with TRex and a Subversion client
c. HttpUnit 1.7
d. dom4j 1.6.1

2. Checkout from the repository
Create a new repository with url:
svn+ssh://svn.berlios.de/svnroot/repos/otgisgeo/trunk
or
https://loginname@svn.berlios.de/svnroot/repos/otgisgeo/trunk

Checkout from SVN repository in a new TTCN-3 project the following:
/javasrc     folder with the Java sources for Adapter and CODEC
/ttcn3       folder with TTCN-3 source code 
build.xml    ant script for building the Java sources and creating a JavaDoc
README.txt   installation instructions

3. Necessary aditional libraries and configuration
In the root directory create a folder called lib and add the following files (making ):
httpunit.jar
dom4j-1.6.1.jar
xercesImpl-2.6.1.jar
xmlParserAPIs-2.6.1.jar
activation-1.1.jar
js-1.6R5.jar;
jtidy-4aug2000r7-dev.jar
junit-3.8.1.jar
mail-1.4.jar
servlet-api-2.4.jar
jaxen-1.1.1.jar

In the build.xml file update the "otSDK" property to the folder in the OpenTTCN tester installation that contains the OTSDK.jar
<property name="otSDK" value="C:\OpenTTCN\JavaSDK\lib"/> 

To build the Java sources run the 'all' target.
If the build is sucessfull a adapter.jar file must be created in the folder containing all the other classes. 

4. Starting the Adapter
To start the Adapter create a batch file that contains the following script (don't forger to update the path variables):
REM OT_LIB          The path of the OpenTTCN Tester installation
REM OT_SDK_LIB      The path of the OpenTTCN JavaSDK library installation
REM TESTS_LIB       The path for the project's libraries
REM COMP_CLASS      The path to the adapter.jar
SET OT_LIB=C:\OpenTTCN\Tester3\lib
SET OT_SDK_LIB=C:\OpenTTCN\JavaSDK\lib
SET TESTS_LIB=C:\workspace\otgisgeo\lib
SET COMP_CLASS=C:\workspace\otgisgeo\classes
java -classpath %OT_LIB%\OpenTTCN3.jar;%OT_SDK_LIB%\OTSDK.jar;%TESTS_LIB%\httpunit.jar;%TESTS_LIB%\dom4j-1.6.1.jar;%TESTS_LIB%\xercesImpl-2.6.1.jar;%TESTS_LIB%\xmlParserAPIs-2.6.1.jar;%TESTS_LIB%\activation-1.1.jar;%TESTS_LIB%\js-1.6R5.jar;%TESTS_LIB%\jtidy-4aug2000r7-dev.jar;%TESTS_LIB%\junit-3.8.1.jar;%TESTS_LIB%\mail-1.4.jar;%TESTS_LIB%\servlet-api-2.4.jar;%TESTS_LIB%\jaxen-1.1.1.jar;%COMP_CLASS% -Djava.library.path=%OT_SDK_LIB%  qualipso.openttcn.gistest.Main

5. build.xml
Target "all" compiles the Java source code, creates the /classes/adapter.jar and create the JavaDoc documentation.
Target "compile" compiles the Java sources
Target "createAdapterJar" compiles the Java sources and creates the adapter.jar that is used in the script of section 4.
Target "doc" generates the JavaDoc documentation  

Property otSDK must be set to point to the folder that has the OTSDK jar.
<property name="otSDK" value="C:\OpenTTCN\JavaSDK\lib"/> 

The generation of JavaDoc files needs to access the Internet in order to retrieve the package lists. 
Some proxy settings must be set up in order to do this.   
<property name="proxy.use" value="false"/>    
<property name="proxy.host" value=""/>
<property name="proxy.port" value=""/>

The address of the package lists is set in the following parameters. If the package list is available locally then set offline to false and set the location to the file in the local file system that contains the package list.
<property name="java.packageList.offline" value="false"/>
<property name="java.packageList.location" value="http://java.sun.com/j2se/1.5.0/docs/api/package-list"/> 

6. Executing the tests
To run the control part of the TTCN-3 abstract test cases the following steps must be taken:
a. Open a DOS command window
b. Start the OpenTTCN Test Server
>ot start

c. Create a test session called my_test_session
>session create my_test_session

d. Navigate to the /ttcn3 that has the *.ttcn3 files and type the following command to load the TTCN-3 test cases to the test session.
>importer3 load my_test_session  GeoServerMain.ttcn3 GISClientRequestTemplates.ttcn3

e. Open a NEW command window and start the script described in section 4.

f. To run the control part of the TTCN-3 main module type the following command:
>tester run my_test_session @control 
