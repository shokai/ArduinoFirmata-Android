all: build

init:
	cd FTDriver &&\
	git pull origin master

compile:
	cd src &&\
	javac org/shokai/firmata/ArduinoFirmataException.java &&\
	javac org/shokai/firmata/ArduinoFirmataEventHandler.java &&\
	javac org/shokai/firmata/ArduinoFirmata.java

build: compile
	cd src &&\
	jar cvf arduino-firmata.jar ./ &&\
	jar uvf arduino-firmata.jar ../README.md &&\
	jar uvf arduino-firmata.jar ../LICENSE.txt &&\
	jar uvf arduino-firmata.jar ../History.txt &&\
	mv arduino-firmata.jar ../
