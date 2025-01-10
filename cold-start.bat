:: clear previous submodule files
rd /s /q grinder
mkdir grinder

:: submodule initialization
cd grinder
git submodule init
git submodule update
cd ..

:: build and create exe
cmd /c .\clean.bat
cmd /c .\build.bat
cmd /c .\gradlew.bat createExe

:: execute
cd app\build\launch4j
.\app.exe
