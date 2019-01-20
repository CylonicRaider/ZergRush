
JAVACFLAGS = -Xlint:all
SOURCES = $(shell find src -name '*.java')
RESOURCES = $(shell find src/res -type f)

.PHONY: run clean

zergrush.jar: .build.jar $(RESOURCES)
	cp $< $@
	cd src && jar uf ../$@ *

.build.jar: $(SOURCES)
	find src -name '*.class' -exec rm {} +
	cd src && javac $(JAVACFLAGS) $(patsubst src/%,%,$(SOURCES))
	cd src && jar cfe ../$@ net.zergrush.Main $$(find . -name '*.class')

run: zergrush.jar
	java -jar $<

clean:
	rm -f .build.jar zergrush.jar
