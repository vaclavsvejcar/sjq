.PHONY: clean
clean:
	find . -name target -type d -exec rm -r {} \;

.PHONY: pretty
pretty:
	headroom run
	sbt scalafmtAll
