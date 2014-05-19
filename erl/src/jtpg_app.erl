-module(jtpg_app).

-behaviour(application).

%% Application callbacks
-export([start/2]).
-export([stop/1]).

-include("jtpg.hrl").

%% ===================================================================
%% Application callbacks
%% ===================================================================

connect_to_all_nodes([]) -> ok;
connect_to_all_nodes([Node|Nodes] = All) ->
    case net_kernel:connect(Node) of
        true ->
            timer:sleep(1000),
            connect_to_all_nodes(Nodes);
        false ->
            connect_to_all_nodes(All)
    end.

start(_StartType, _StartArgs) ->
    AllNodes = ['jtpg@192.168.122.11',
                'jtpg@192.168.122.12',
                'jtpg@192.168.122.13',
                'jtpg@192.168.122.14',
                'jtpg@192.168.122.15'],
    ok = connect_to_all_nodes(AllNodes -- [node()]),
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
