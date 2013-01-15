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
	cp -p README.md src/ &&\
	cp -p LICENSE.txt src/ &&\
	cp -p History.txt src/ &&\
	cp -p FTDriver/README.md src/jp/ksksue/ &&\
	cp -p FTDriver/NOTICE.txt src/jp/ksksue/ &&\
	cd src &&\
	jar cvf arduino-firmata.jar ./ &&\
	rm README.md &&\
	rm LICENSE.txt &&\
	rm History.txt &&\
	rm jp/ksksue/README.md &&\
	rm jp/ksksue/NOTICE.txt &&\
	mv arduino-firmata.jar ../
