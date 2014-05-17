RELX_CONF=relx.config


all: release

clean:
	rm -rf ebin/
	rebar skip_deps=true clean

compile:
	rebar compile

deps:
	rebar get-deps

dialyze:
	@dialyzer -Wno_undefined_callbacks \
	   -r ebin/	

release: deps compile
	relx -c $(RELX_CONF)

.PHONY: all \
	clean \
	compile \
	deps \
	release
