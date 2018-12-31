
SOURCES = $(shell find src -name '*.java')

.PHONY: run clean

zergrush.jar: $(SOURCES)
	cd src && javac $(patsubst src/%,%,$(SOURCES))
	cd src && jar cfe ../zergrush.jar net.zergrush.ZergRush .

run: zergrush.jar
	java -jar zergrush.jar

clean:
	rm -f zergrush.jar
