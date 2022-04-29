all: compile demo
compile:
	-chmod u+x ./*.sh
	./compile.sh
demo:
	-chmod u+x ./*.sh
	rm -rf output_acc
	mkdir output_acc
	@echo [DEMO] running Butterfly Accurate Ver.
	./runbutterfly_acc.sh ./data/itunes/itunes_acc.txt output_acc 70000 1
	@echo [DEMO] estimated global and local butterfly counts are saved in output
	@echo [DEMO] running Butterfly-Spot
	./runbutterfly_spot.sh ./data/itunes/itunes_timestamp.txt output_spot 2000 86000
	@echo [DEMO] estimated global butterfly counts and timestamp are saved in output
