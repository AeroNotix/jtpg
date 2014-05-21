-module(jtpg_read_handler).

-export([init/3, handle/2, terminate/3]).

-include("jtpg.hrl").

init(_Type, Req, _Opts) ->
    {ok, Req, no_state}.

handle(Req, State) ->
    StatusCode = 200,
    Groups = ?pg:which_groups(?SCOPE),
    AllPids = [begin
                   {ok, GroupName, Pids} =
                       ?pg:get_local_members(?SCOPE, GroupName),
                   {list_to_binary(GroupName), length(Pids)}
               end || GroupName <- Groups],
    JSONEncoded = jsx:encode(AllPids),
    {ok, Req2} =
        cowboy_req:reply(StatusCode, ?HEADERS, JSONEncoded, Req),
    {ok, Req2, State}.

terminate(_Reason, _Req, _State) ->
    ok.
