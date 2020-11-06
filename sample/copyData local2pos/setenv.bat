@set SELJ_HOME=../..

@set SELJ_LIB=%SELJ_HOME%/lib
@set CLS=./;%SELJ_HOME%/bin
@set CLS=%CLS%;postgresql-8.2-504.jdbc2.jar
@set CLS=%CLS%;%SELJ_LIB%\h2\h2.jar
@set CLS=%CLS%;%SELJ_LIB%\s2\aopalliance-1.0.jar
@set CLS=%CLS%;%SELJ_LIB%\s2\commons-logging-1.1.jar
@set CLS=%CLS%;%SELJ_LIB%\s2\geronimo-jta_1.0.1B_spec-1.0.jar
rem @set CLS=%CLS%;%SELJ_LIB%\s2\hsqldb-1.8.0.1.jar
@set CLS=%CLS%;%SELJ_LIB%\s2\javassist-3.4.ga.jar
@set CLS=%CLS%;%SELJ_LIB%\s2\log4j-1.2.13.jar
@set CLS=%CLS%;%SELJ_LIB%\s2\ognl-2.6.7.jar
rem @set CLS=%CLS%;%SELJ_LIB%\s2\portlet-api-1.0.jar
@set CLS=%CLS%;%SELJ_LIB%\s2\s2-extension-2.4.7.jar
@set CLS=%CLS%;%SELJ_LIB%\s2\s2-framework-2.4.7.jar
@set CLS=%CLS%;%SELJ_LIB%\s2dao\s2-dao-1.0.43.jar
@set CLS=%CLS%;%SELJ_LIB%\s2dao_tiger\s2-dao-tiger-1.0.43.jar
