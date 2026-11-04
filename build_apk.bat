@echo off
echo ==============================================
echo Сборка нативного Android APK (Jetpack Compose)
echo ==============================================
set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot
set Path=%JAVA_HOME%\bin;%Path%

cd /d "c:\calculator\android-app"
call gradlew.bat assembleDebug

if %ERRORLEVEL% equ 0 (
    copy /Y "app\build\outputs\apk\debug\app-debug.apk" "c:\calculator\KipaCalculator.apk"
    echo.
    echo ==============================================
    echo УСПЕШНО! APK файл готов:
    echo c:\calculator\KipaCalculator.apk
    echo ==============================================
) else (
    echo.
    echo Ошибка сборки APK!
)
pause
