-module(jtpg_app).

-behaviour(application).

%% Application callbacks
-export([start/2]).
-export([stop/1]).

-include("jtpg.hrl").

%% ===================================================================
%% Application callbacks
%% ===================================================================


%% This is pretty dumb since it doesn't deal with other nodes being
%% down but hey if they aren't up then the tests fail anyway. #yolo
connect_to_all_nodes([]) -> ok;
connect_to_all_nodes([Node|Nodes] = All) ->
    case net_kernel:connect(Node) of
        true ->
            connect_to_all_nodes(Nodes);
        false ->
            timer:sleep(1000),
            connect_to_all_nodes(All)
    end.

start(_StartType, _StartArgs) ->
    {ok, AllNodes} = application:get_env(jtpg, nodes),
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
