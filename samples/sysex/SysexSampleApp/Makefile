all: clean build
setup:
	android update project --path `pwd` --name 'SysexSample'
build:
	ant debug
install:
	adb install -r bin/SysexSample-debug.apk
uninstall:
	adb uninstall org.shokai.firmata.sysexsample
logcat:
	adb logcat | grep firmata
clean:
	/bin/rm -r -f bin/classes
