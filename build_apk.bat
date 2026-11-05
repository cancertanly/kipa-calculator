@echo off
setlocal
echo ==============================================
echo Сборка нативного Android APK (Jetpack Compose)
echo ==============================================

if "%JAVA_HOME%"=="" (
    if exist "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot" (
        set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot"
    )
)
if defined JAVA_HOME (
    set "Path=%JAVA_HOME%\bin;%Path%"
)

cd /d "%~dp0android-app"
call gradlew.bat assembleDebug

if %ERRORLEVEL% equ 0 (
    copy /Y "app\build\outputs\apk\debug\app-debug.apk" "%~dp0KipaCalculator.apk"
    echo.
    echo ==============================================
    echo УСПЕШНО! APK файл готов:
    echo %~dp0KipaCalculator.apk
    echo ==============================================
) else (
    echo.
    echo Ошибка сборки APK!
)
pause
