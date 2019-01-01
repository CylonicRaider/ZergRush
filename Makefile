
SOURCES = $(shell find src -name '*.java')

.PHONY: run clean

zergrush.jar: $(SOURCES)
	find src -name '*.class' -exec rm {} +
	cd src && javac $(patsubst src/%,%,$(SOURCES))
	cd src && jar cfe ../zergrush.jar net.zergrush.Main .

run: zergrush.jar
	java -jar zergrush.jar

clean:
	rm -f zergrush.jar
