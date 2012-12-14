all: clean build
setup:
	android update project --path `pwd` --name 'FirmataSample'
build:
	ant debug
install:
	adb install -r bin/FirmataSample-debug.apk
uninstall:
	adb uninstall org.shokai.firmata.sample
logcat:
	adb logcat | grep firmata
clean:
	/bin/rm -r -f bin/classes
