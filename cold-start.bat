:: clear previous files
rmdir /s /q grinder
rmdir /s /q lib
del app.exe

:: submodule initialization
mkdir grinder
cd grinder
git submodule init
git submodule update
cd ..

:: build and create exe
cmd /c .\clean.bat
cmd /c .\build.bat
cmd /c .\gradlew.bat createExe

:: setup and execute
robocopy app\build\launch4j . /e
.\app.exe
