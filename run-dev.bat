@echo off
cd /d "%~dp0"

if "%JAVA_HOME%"=="" (
    echo ERROR: JAVA_HOME is not set. Please set it to your JDK 21 path.
    echo Example: set JAVA_HOME=C:\Program Files\Java\jdk-21
    pause
    exit /b 1
)

echo Starting AntiGravityBoard with H2 (no PostgreSQL needed)...
echo Open http://localhost:8080 in your browser when ready.
echo.

"%JAVA_HOME%\bin\java.exe" -classpath ".mvn\wrapper\maven-wrapper.jar" "-Dmaven.multiModuleProjectDirectory=%CD%" org.apache.maven.wrapper.MavenWrapperMain spring-boot:run "-Dspring-boot.run.profiles=dev"

pause
