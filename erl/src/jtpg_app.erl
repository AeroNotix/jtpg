-module(jtpg_app).

-behaviour(application).

%% Application callbacks
-export([start/2]).
-export([stop/1]).

-include("jtpg.hrl").

%% ===================================================================
%% Application callbacks
%% ===================================================================

start(_StartType, _StartArgs) ->
    [true, true, true, true]
        = [net_kernel:connect(N) || N <- ['jtpg@192.168.122.11',
                                          'jtpg@192.168.122.12',
                                          'jtpg@192.168.122.13',
                                          'jtpg@192.168.122.14',
                                          'jtpg@192.168.122.15'] -- [node()]],
    {ok, _} = cpg:start_link(?SCOPE),
    Routes = [{<<"/new_pid/:id">>, jtpg_http_handler, []}],
    Dispatch = cowboy_router:compile([{'_', Routes}]),
    {ok, _} = cowboy:start_http(jtpg_listener,
                                100, [{port, 8080}], %% Port shouldn't be hardcoded.
                                [
                                 {env, [{dispatch, Dispatch}]}
                                ]),
    {ok, _} = jtpg_sup:start_link().

stop(_State) ->
    ok.
