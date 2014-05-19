-module(jtpg_util).

-export([connect_all_nodes/1]).
-export([connect_node/1]).


%% This is pretty dumb since it doesn't deal with other nodes being
%% down but hey if they aren't up then the tests fail anyway. #yolo
connect_node(Node) ->
    case net_kernel:connect(Node) of
        true ->
            ok;
        false ->
            timer:sleep(1000),
            connect_node(Node)
    end.

connect_all_nodes([]) -> ok;
connect_all_nodes(Nodes) when is_list(Nodes) ->
    Replies = lists:duplicate(length(Nodes), true),
    Replies = pmap:map(fun connect_node/1, Nodes),
    ok.
