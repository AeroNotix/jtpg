-module(jtpg_http_handler).

-export([init/3, handle/2, terminate/3]).

-include("jtpg.hrl").

-define(pg, cpg).

init(_Type, Req, _Opts) ->
    {ok, Req, no_state}.

make_pid() ->
    spawn(fun() ->
                  receive
                      ok -> ok
                  end
          end).

handle(Req, State) ->
    {Id, Req2} = cowboy_req:binding(id, Req),
    Pid = make_pid(),
    JoinResp = ?pg:join(?SCOPE, binary_to_list(Id), Pid),
    {StatusCode, Body} = 
        case JoinResp of
            ok ->
                {200, [{<<"id">>, Id}]};
            Else ->
                io:format("Unhandled return: ~p~n", [Else]),
                {500, [{<<"error">>, <<"Unhandled">>}]}
        end,
    JSONEncoded = jsx:encode(Body),
    {ok, Req3} = cowboy_req:reply(StatusCode, [], JSONEncoded, Req2),
    {ok, Req3, State}.

terminate(_Reason, _Req, _State) ->
    ok.
