-module(jtpg_read_handler).

-export([init/3, handle/2, terminate/3]).

-include("jtpg.hrl").

init(_Type, Req, _Opts) ->
    {ok, Req, no_state}.

handle(Req, State) ->
    StatusCode = 200,
    Groups = ?pg:which_groups(?SCOPE),
    AllPids = [case ?pg:get_members(?SCOPE, GroupName) of
                   {ok, GroupName, Pids} ->
                       {list_to_binary(GroupName), length(Pids)};
                   {error, {no_such_group, GroupName}} ->
                       undefined
               end || GroupName <- Groups],
    FilteredPids = lists:filter(fun(X) -> X =/= undefined end, AllPids),
    JSONEncoded = jsx:encode(FilteredPids),
    {ok, Req2} =
        cowboy_req:reply(StatusCode, ?HEADERS, JSONEncoded, Req),
    {ok, Req2, State}.

terminate(_Reason, _Req, _State) ->
    ok.
