@echo off
set f=-%1
for /f "usebackq tokens=1*" %%i in (`echo %*`) DO @ set params=%%j

java -jar eme.jar %f% %params%
