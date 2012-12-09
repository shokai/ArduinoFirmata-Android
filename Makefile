all: build

compile:
	cd src &&\
	javac org/shokai/firmata/ArduinoFirmataException.java &&\
	javac org/shokai/firmata/*.java

build: compile
	cd src &&\
	jar cvf arduino-firmata.jar ./org &&\
	mv arduino-firmata.jar ../
