@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  ALICE-Framework startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and ALICE_FRAMEWORK_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\ALICE-Framework.jar;%APP_HOME%\lib\Discord4J-3.1.0.jar;%APP_HOME%\lib\reflections-0.9.11.jar;%APP_HOME%\lib\discord4j-core-3.1.0.jar;%APP_HOME%\lib\discord4j-gateway-3.1.0.jar;%APP_HOME%\lib\discord4j-rest-3.1.0.jar;%APP_HOME%\lib\discord4j-voice-3.1.0.jar;%APP_HOME%\lib\discord4j-common-3.1.0.jar;%APP_HOME%\lib\guava-20.0.jar;%APP_HOME%\lib\javassist-3.21.0-GA.jar;%APP_HOME%\lib\stores-jdk-3.1.3.jar;%APP_HOME%\lib\stores-api-3.1.3.jar;%APP_HOME%\lib\discord-json-1.5.3.jar;%APP_HOME%\lib\reactor-netty-0.9.10.RELEASE.jar;%APP_HOME%\lib\reactor-extra-3.3.3.RELEASE.jar;%APP_HOME%\lib\discord-json-api-1.5.3.jar;%APP_HOME%\lib\jackson-datatype-jdk8-2.11.1.jar;%APP_HOME%\lib\jackson-databind-2.11.1.jar;%APP_HOME%\lib\simple-fsm-1.0.1.jar;%APP_HOME%\lib\netty-codec-http2-4.1.51.Final.jar;%APP_HOME%\lib\netty-handler-proxy-4.1.51.Final.jar;%APP_HOME%\lib\netty-codec-http-4.1.51.Final.jar;%APP_HOME%\lib\netty-handler-4.1.51.Final.jar;%APP_HOME%\lib\netty-transport-native-epoll-4.1.51.Final-linux-x86_64.jar;%APP_HOME%\lib\reactor-core-3.3.8.RELEASE.jar;%APP_HOME%\lib\jackson-annotations-2.11.1.jar;%APP_HOME%\lib\jackson-core-2.11.1.jar;%APP_HOME%\lib\netty-codec-socks-4.1.51.Final.jar;%APP_HOME%\lib\netty-codec-4.1.51.Final.jar;%APP_HOME%\lib\netty-transport-native-unix-common-4.1.51.Final.jar;%APP_HOME%\lib\netty-transport-4.1.51.Final.jar;%APP_HOME%\lib\netty-buffer-4.1.51.Final.jar;%APP_HOME%\lib\netty-resolver-4.1.51.Final.jar;%APP_HOME%\lib\netty-common-4.1.51.Final.jar;%APP_HOME%\lib\reactive-streams-1.0.3.jar;%APP_HOME%\lib\Servicer-1.0.3.jar


@rem Execute ALICE-Framework
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %ALICE_FRAMEWORK_OPTS%  -classpath "%CLASSPATH%" alice/framework/main/Brain %*

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable ALICE_FRAMEWORK_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%ALICE_FRAMEWORK_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
