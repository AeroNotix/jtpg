-module(jtpg_util).

-export([connect_all_nodes/1]).


%% This is pretty dumb since it doesn't deal with other nodes being
%% down but hey if they aren't up then the tests fail anyway. #yolo
connect_all_nodes([]) -> ok;
connect_all_nodes([Node|Nodes] = All) ->
    case net_kernel:connect(Node) of
        true ->
            connect_all_nodes(Nodes);
        false ->
            timer:sleep(1000),
            connect_all_nodes(All)
    end.

