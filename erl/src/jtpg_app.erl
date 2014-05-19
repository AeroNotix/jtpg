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
    {ok, AllNodes} = application:get_env(jtpg, nodes),
    ok = jtpg_util:connect_all_nodes(AllNodes -- [node()]),
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
