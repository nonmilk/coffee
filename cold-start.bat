:: clear previous submodule files
rm grinder
mkdir grinder

:: submodule initialization
cd grinder
git submodule init
git submodule update
cd ..

:: build and create exe
.\clean.bat
.\build.bat
.\gradlew.bat createExe

:: execute
cd app\build\launch4j
.\app.exe
