CC=gcc
CFLAGS=-Wall -Wextra -Wpedantic -std=gnu99 -g

s: server.c ## Server
	$(CC) $(CFLAGS) $^ -o $@

k: klient.c ##Klient
	$(CC) $(CFLAGS) $^ -o $@

run_s: server ##Klient
	./server "a_doll's_house.job" 8756

run_k: klient
	./klient 127.0.0.1 8756

klient_d: CFLAGS+=-DDEBUG
klient_d: klient


server_d: CFLAGS+=-DDEBUG
server_d: server

clean:
	rm -f server klient
