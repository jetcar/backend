:: filepath: c:\repo\vscode\scripts\get_ssl_cert.bat
@echo off
set HOST=sid.demo.sk.ee
set PORT=443
set OUTPUT_FILE=sid_demo_sk_ee.crt

echo Fetching SSL certificate from %HOST%:%PORT%...
openssl s_client -connect %HOST%:%PORT% -showcerts <nul 2>nul | openssl x509 -outform PEM > %OUTPUT_FILE%

if %ERRORLEVEL% EQU 0 (
    echo SSL certificate saved to %OUTPUT_FILE%
) else (
    echo Failed to fetch SSL certificate
    exit /b 1
)