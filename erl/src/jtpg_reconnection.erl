-module(jtpg_reconnection).

-behaviour(gen_server).

%% API
-export([start_link/0]).

%% gen_server callbacks
-export([code_change/3]).
-export([handle_call/3]).
-export([handle_cast/2]).
-export([handle_info/2]).
-export([init/1]).
-export([terminate/2]).


-define(SERVER, ?MODULE).
-define(TIMEOUT, 5000).

-record(state, {nodes = [] :: [node()]}).

start_link() ->
    {ok, Nodes} = application:get_env(jtpg, nodes),
    gen_server:start_link({local, ?SERVER}, ?MODULE, [Nodes], []).

init([Nodes]) when is_list(Nodes) ->
    NodesExceptMe = Nodes -- [node()],
    Replies = lists:duplicate(length(NodesExceptMe), true),
    Replies = [monitor_node(Node, true) || Node <- NodesExceptMe],    
    {ok, #state{nodes = NodesExceptMe}, ?TIMEOUT}.

handle_call(_Msg, _From, State) ->
    Reply = ok,
    {reply, Reply, State, ?TIMEOUT}.

handle_cast(_Msg, State) ->
    {noreply, State, ?TIMEOUT}.

handle_info(timeout, #state{nodes = Nodes} = State) ->
    case lists:sort(nodes()) =:= list:sort(Nodes) of
        true ->
            {noreply, State, ?TIMEOUT};
        false ->
            ok = jtpg_util:connect_all_nodes(Nodes),
            {noreply, State, ?TIMEOUT}
    end;
handle_info({nodedown, Node}, State) ->
    true = jtpg_util:connect_node(Node),
    true = monitor_node(Node, true),
    {noreply, State, ?TIMEOUT}.

terminate(_Reason, _State) ->
    ok.

code_change(_OldVsn, State, _Extra) ->
    {ok, State}.
