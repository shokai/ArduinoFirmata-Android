all: build

init:
	cd usb-serial-for-android &&\
	git pull origin master &&\
	cd ../ &&\
	cp -R usb-serial-for-android/UsbSerialLibrary/src/com src/

compile:
	cd src &&\
	javac org/shokai/firmata/ArduinoFirmataException.java &&\
	javac org/shokai/firmata/*.java

build: compile
	cd src &&\
	jar cvf arduino-firmata.jar ./org &&\
	mv arduino-firmata.jar ../
