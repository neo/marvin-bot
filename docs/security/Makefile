ALL_SVGS=$(patsubst %.seqdiag,%.svg,$(wildcard *.seqdiag))

default: all

%.svg: %.seqdiag
	seqdiag -T svg $<

all: $(ALL_SVGS)
