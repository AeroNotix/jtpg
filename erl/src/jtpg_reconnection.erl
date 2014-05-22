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

-record(state, {nodes = [] :: [node()]}).

start_link() ->
    {ok, Nodes} = application:get_env(jtpg, nodes),
    gen_server:start_link({local, ?SERVER}, ?MODULE, [Nodes], []).

init([Nodes]) when is_list(Nodes) ->
    NodesExceptMe = Nodes -- [node()],
    Replies = lists:duplicate(length(NodesExceptMe), true),
    Replies = [monitor_node(Node, true) || Node <- NodesExceptMe],    
    {ok, #state{nodes = NodesExceptMe}}.

handle_call({nodedown, Node}, _From, State) ->
    Reply = true = jtpg_util:connect_node(Node),
    {reply, Reply, State}.

handle_cast(_Msg, State) ->
    {noreply, State}.

handle_info(_InfoMsg, State) ->
    {noreply, State}.

terminate(_Reason, _State) ->
    ok.

code_change(_OldVsn, State, _Extra) ->
    {ok, State}.
