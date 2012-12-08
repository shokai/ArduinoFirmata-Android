all:
	ant debug
setup:
	android update project --path `pwd` --name 'FirmataSample'
install:
	adb install -r bin/FirmataSample-debug.apk
uninstall:
	adb uninstall org.shokai.firmata.sample
logcat:
	adb logcat | grep firmata
clean:
	/bin/rm -r -f bin/classes
