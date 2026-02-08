CC=gcc
PROC=proc
CFLAGS=-O2
BIN=bin/recon

all:
	mkdir -p build bin
	$(PROC) src/db.pc
	$(PROC) src/main.pc
	$(CC) $(CFLAGS) -c src/fileio.c src/rules.c
	$(CC) -o $(BIN) main.c db.c fileio.o rules.o -lclntsh

clean:
	rm -rf build bin *.o *.c
