@echo off
echo Requesting administrative privileges...
net session >nul 2>&1
if %errorLevel% == 0 (
    echo Success: Administrative permissions confirmed.
) else (
    echo Failure: Current permissions inadequate.
    echo Please right-click this file and select "Run as Administrator".
    pause
    exit /b
)

echo Adding Firewall Rule for LanCast on Port 8000...
netsh advfirewall firewall add rule name="LanCast" dir=in action=allow protocol=TCP localport=8000
echo.
echo Rule added. You should now be able to connect from your phone.
echo IP to use: http://20.30.1.88:8000
pause
