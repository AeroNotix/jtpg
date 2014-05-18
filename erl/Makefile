RELX_CONF=relx.config
RELX_MULTI_CONF=relx-multi.config


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

multi-release: n1 n2 n3 n4 n5

n1 n2 n3 n4 n5:
	relx -c $(RELX_MULTI_CONF) --overlay_vars files/vars/$@.config -o _$@


.PHONY: all \
	clean \
	compile \
	deps \
	release
