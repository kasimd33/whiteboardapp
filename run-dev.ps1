# Run AntiGravityBoard with H2 (no PostgreSQL)
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

if (-not $env:JAVA_HOME) {
    $env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
}
if (-not (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    Write-Error "JAVA_HOME not set or invalid. Set it to your JDK 21 path."
}

Write-Host "Starting AntiGravityBoard (H2 dev profile)..."
Write-Host "Open http://localhost:8080 when ready.`n"

& "$env:JAVA_HOME\bin\java.exe" `
    -classpath ".mvn\wrapper\maven-wrapper.jar" `
    "-Dmaven.multiModuleProjectDirectory=$PWD" `
    org.apache.maven.wrapper.MavenWrapperMain `
    "spring-boot:run" "-Dspring-boot.run.profiles=dev"
