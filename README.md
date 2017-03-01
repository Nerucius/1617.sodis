# Poker5CardGame

### Abstract

NetBeans Java project for a distributed Poker Game system. A centralized Server can accept and manage several Clients which connect to the server using a specific [Protocol](https://ub-gei-sd.github.io/Practica1/Poquer.txt.txt).

### State of the Project:

Currently, the game can be played using a keyboard and typing the actual name of the Actions (not the PROTOCOL 4 letter command) up to the moment where the server has to BET, CALL or PASS on a given PASS or BET.

The KeyBoard source is smart enough to not allow illegal or malformed commands. The Game's Finite State Machine instance can detect invalid operations and act accordingly.

**Demo (User commands after '>'):**

```
> START
INIT -> START -> START R2:false
SERVER: ANTE_STAKES
ANTE: 100
STAKES: 1000 10000
START -> ANTE_STAKES -> ANTE R2:false
> ANTE_OK
ANTE -> ANTE_OK -> PLAY R2:false
SERVER: DEALER_HAND
DEALER:1
HAND:[8H, 6H, 9D, QC, 10D]
PLAY -> DEALER_HAND -> BETTING R2:false
> askdalnds
INVALID ACTION
> QUIT
--- ILLEGAL ACTION
--- BETTING DOES NOT ACCEPT QUIT
> BET 50
BETTING -> BET -> COUNTER R2:false
<INFINITE LOOP>
```

### Goals

By the end of the project, the following goals should be accomplished:
- Our Server should manage an arbitrary number of connections, each with it's own Game instance.
- Any client not responding after a certain timeout should be disconnected.
- Any malformed or illegal commands should not crash or otherwise maim the Server in any way.
- Unexpectedly closed connections should be handled gracefully.
- Clients' stakes should be saved and restored upon connecting again.
