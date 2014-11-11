all: build

init:
	cd usb-serial-for-android &&\
	git submodule init &&\
	git submodule update &&\
	git pull origin master &&\
	cd ../ &&\
	cp -R usb-serial-for-android/UsbSerialLibrary/src/com src/

compile:
	cd src &&\
	find -name "*.java" > sources.txt &&\
	javac @sources.txt &&\
	rm sources.txt

buildfirmata: compile
	cd src &&\
	jar cvf arduino-firmata.jar ./org &&\
	jar uvf arduino-firmata.jar -C .. README.md &&\
	jar uvf arduino-firmata.jar -C .. LICENSE.txt &&\
	jar uvf arduino-firmata.jar -C .. History.txt &&\
	mv arduino-firmata.jar ../

buildusbserial: compile
	cd src &&\
	jar cvf usb-serial-for-android.jar ./com &&\
	jar uvf usb-serial-for-android.jar -C ../usb-serial-for-android README.md &&\
	jar uvf usb-serial-for-android.jar -C ../usb-serial-for-android LICENSE.txt &&\
	jar uvf usb-serial-for-android.jar -C ../usb-serial-for-android CHANGELOG.txt &&\
	mv usb-serial-for-android.jar ../

build: buildfirmata buildusbserial
